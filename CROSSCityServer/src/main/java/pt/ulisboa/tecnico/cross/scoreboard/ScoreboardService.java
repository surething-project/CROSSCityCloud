package pt.ulisboa.tecnico.cross.scoreboard;

import com.google.firebase.messaging.Notification;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.badging.BadgingRepository;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.scoreboard.domain.ScoreboardProfile;
import pt.ulisboa.tecnico.cross.trip.domain.PeerTestimony;
import pt.ulisboa.tecnico.cross.trip.domain.Trip;
import pt.ulisboa.tecnico.cross.trip.domain.Visit;
import pt.ulisboa.tecnico.cross.user.UserRepository;
import pt.ulisboa.tecnico.cross.utils.MessagingUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.PROPS;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.getDb;
import static pt.ulisboa.tecnico.cross.trip.domain.Visit.VERIFICATION_STATUS.OK;

@ApplicationScoped
public class ScoreboardService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScoreboardService.class);
  private final ScoreboardRepository scoreboardRepository = ScoreboardRepository.get();
  private final BadgingRepository badgingRepository = BadgingRepository.get();
  private final UserRepository userRepository = UserRepository.get();
  private final MessagingUtils messagingUtils = MessagingUtils.get();
  private final int ENDORSER_SCORE_INCREMENT =
      Integer.parseInt(PROPS.getProperty("ENDORSER_SCORE_INCREMENT"));
  private final int ENDORSER_GEMS_INCREMENT =
      Integer.parseInt(PROPS.getProperty("ENDORSER_GEMS_INCREMENT"));

  public Map<String, List<ScoreboardProfile>> getScoreboard(String username) {
    Map<String, List<ScoreboardProfile>> scoreboard = new HashMap<>();
    try (Connection con = getDb()) {
      scoreboard.put("All-Time", getScoreboardProfiles(con, username, ""));
      scoreboard.put("Seasonal", getScoreboardProfiles(con, username, "quarter"));
      scoreboard.put("Weekly", getScoreboardProfiles(con, username, "week"));
      return scoreboard;
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }

  private List<ScoreboardProfile> getScoreboardProfiles(
      Connection con, String username, String datepart) throws SQLException {
    List<ScoreboardProfile> scoreboard =
        scoreboardRepository.getScoreboard(con, username, datepart);
    for (ScoreboardProfile profile : scoreboard) {
      profile.addAllOwnedBadges(badgingRepository.getOwnedBadges(con, profile.getUsername()));
    }
    return scoreboard;
  }

  public void rewardWitnesses(Trip trip) {
    final Map<String, Integer> endorses = new HashMap<>();
    for (Visit visit : trip.getVisits()) {
      if (visit.getVerificationStatus().equals(OK)) {
        for (PeerTestimony peerTestimony : visit.getPeerTestimonies()) {
          endorses.put(
              peerTestimony.getWitness(), endorses.getOrDefault(peerTestimony.getWitness(), 0) + 1);
        }
      }
    }
    if (endorses.isEmpty()) return;

    try (Connection con = getDb()) {
      for (Map.Entry<String, Integer> e : endorses.entrySet()) {
        String endorser = e.getKey();
        int awardedScore = e.getValue() * ENDORSER_SCORE_INCREMENT;
        int awardedGems = e.getValue() * ENDORSER_GEMS_INCREMENT;
        scoreboardRepository.updateScore(con, endorser, awardedScore);
        userRepository.updateGems(con, endorser, awardedGems);

        messagingUtils.notify(
            con,
            endorser,
            Notification.builder()
                .setTitle("Endorsement Reward")
                .setBody(
                    String.format(
                        "Congratulations! You received +%d XP and +%d gems for endorsing someone's location.",
                        awardedScore, awardedGems))
                .build(),
            Collections.singletonMap("awardedGems", String.valueOf(awardedGems)));
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }
}
