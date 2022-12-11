package pt.ulisboa.tecnico.cross.scoreboard;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.contract.gamification.Scoreboard;
import pt.ulisboa.tecnico.cross.scoreboard.domain.ScoreboardProfile;
import pt.ulisboa.tecnico.cross.utils.MediaType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/scoreboard")
public class ScoreboardController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScoreboardController.class);
  @Inject private ScoreboardService scoreboardService;

  @GET
  @RolesAllowed("USER")
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public Scoreboard.GetScoreboardResponse getScoreboard(
      @Context ContainerRequestContext requestContext) {
    String username = (String) requestContext.getProperty("username");
    Map<String, List<ScoreboardProfile>> scoreboard = scoreboardService.getScoreboard(username);

    return Scoreboard.GetScoreboardResponse.newBuilder()
        .addAllAllTime(
            scoreboard.get("All-Time").stream()
                .map(ScoreboardProfile::toProtobuf)
                .collect(Collectors.toList()))
        .addAllSeasonal(
            scoreboard.get("Seasonal").stream()
                .map(ScoreboardProfile::toProtobuf)
                .collect(Collectors.toList()))
        .addAllWeekly(
            scoreboard.get("Weekly").stream()
                .map(ScoreboardProfile::toProtobuf)
                .collect(Collectors.toList()))
        .build();
  }
}
