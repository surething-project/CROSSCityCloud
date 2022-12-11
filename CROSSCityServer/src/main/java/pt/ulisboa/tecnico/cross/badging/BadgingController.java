package pt.ulisboa.tecnico.cross.badging;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.badging.domain.Badge;
import pt.ulisboa.tecnico.cross.badging.services.BadgingService;
import pt.ulisboa.tecnico.cross.contract.gamification.Badging;
import pt.ulisboa.tecnico.cross.utils.MediaType;

import java.util.List;
import java.util.stream.Collectors;

@Path("/badging")
public class BadgingController {

  private static final Logger LOGGER = LoggerFactory.getLogger(BadgingController.class);
  @Inject private BadgingService badgingService;

  @GET
  @PermitAll
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public Badging.GetBadgesResponse getBadges() {
    List<Badge> badges = badgingService.getBadges();
    return Badging.GetBadgesResponse.newBuilder()
        .addAllBadges(badges.stream().map(Badge::toProtobuf).collect(Collectors.toList()))
        .build();
  }
}
