package pt.ulisboa.tecnico.cross.badging.services;

import com.google.firebase.messaging.Notification;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.badging.BadgingRepository;
import pt.ulisboa.tecnico.cross.badging.domain.Badge;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.trip.domain.PeerTestimony;
import pt.ulisboa.tecnico.cross.trip.domain.Trip;
import pt.ulisboa.tecnico.cross.trip.domain.Visit;
import pt.ulisboa.tecnico.cross.utils.MessagingUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.getDb;
import static pt.ulisboa.tecnico.cross.trip.domain.Visit.VERIFICATION_STATUS.OK;

@ApplicationScoped
public class BadgingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BadgingService.class);
  private final BadgingRepository badgingRepository = BadgingRepository.get();
  private final BadgeAttribution badgeAttribution = new BadgeAttribution();
  private final MessagingUtils messagingUtils = MessagingUtils.get();

  public List<Badge> getBadges() {
    try (Connection con = getDb()) {
      return badgingRepository.getBadges(con);
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }

  public List<String> awardBadges(String username) {
    try (Connection con = getDb()) {
      return awardBadges(con, username);
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }

  private List<String> awardBadges(Connection con, String username) throws SQLException {
    List<String> awardedBadges = badgeAttribution.review(con, username);
    for (String awardedBadge : awardedBadges) {
      badgingRepository.awardBadge(con, username, awardedBadge);
    }
    return awardedBadges;
  }

  public void rewardWitnesses(Trip trip) {
    Set<String> witnesses =
        trip.getVisits().stream()
            .filter(v -> v.getVerificationStatus().equals(OK))
            .map(Visit::getPeerTestimonies)
            .flatMap(Collection::stream)
            .map(PeerTestimony::getWitness)
            .collect(Collectors.toSet());
    if (witnesses.isEmpty()) return;

    try (Connection con = getDb()) {
      List<Badge> badges = badgingRepository.getBadges(con);
      for (String witness : witnesses) {
        List<String> awardedBadges = awardBadges(con, witness);
        for (String awardedBadge : awardedBadges) {
          Badge badge =
              badges.stream().filter(b -> b.getId().equals(awardedBadge)).findAny().orElse(null);
          if (badge == null) continue; // It never happens!

          messagingUtils.notify(
              con,
              witness,
              Notification.builder()
                  .setTitle("Badge Earning")
                  .setBody(
                      String.format(
                          "Congratulations! You've earned the '%s' badge, check it out!",
                          badge.getNames().get(badge.getMainLocale())))
                  .setImage(badge.getImageUrl())
                  .build(),
              Collections.singletonMap("awardedBadge", awardedBadge));
        }
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }
}
