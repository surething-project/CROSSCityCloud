package pt.ulisboa.tecnico.cross.trip;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.badging.services.BadgingService;
import pt.ulisboa.tecnico.cross.contract.Evidence;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.scavenger.ScavengerService;
import pt.ulisboa.tecnico.cross.scoreboard.ScoreboardService;
import pt.ulisboa.tecnico.cross.trip.domain.PeerTestimony;
import pt.ulisboa.tecnico.cross.trip.domain.Trip;
import pt.ulisboa.tecnico.cross.trip.domain.Visit;
import pt.ulisboa.tecnico.cross.trip.domain.WiFiAPEvidence;
import pt.ulisboa.tecnico.cross.trip.services.TripService;
import pt.ulisboa.tecnico.cross.utils.LCTService;
import pt.ulisboa.tecnico.cross.utils.MediaType;
import pt.ulisboa.tecnico.cross.wifiap.WiFiAPService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.PROPS;

@Path("/trip")
public class TripController {

  private static final Logger LOGGER = LoggerFactory.getLogger(TripController.class);
  private final long WIFI_SCAN_PERIOD_SECONDS =
      Long.parseLong(PROPS.getProperty("WIFI_SCAN_PERIOD_SECONDS"));
  @Inject private TripService tripService;
  @Inject private ScoreboardService scoreboardService;
  @Inject private BadgingService badgingService;
  @Inject private WiFiAPService wiFiAPService;
  @Inject private LCTService lctService;
  @Inject private ScavengerService scavengerService;

  @GET
  @Path("/{id}")
  @RolesAllowed("USER")
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public TripOuterClass.Trip getTrip(
      @Context ContainerRequestContext requestContext, @PathParam("id") String id) {
    String username = (String) requestContext.getProperty("username");

    // Evidence is not defined as it does not need to be sent back to the client.
    return tripService.getTrip(username, id).toProtobuf();
  }

  @GET
  @RolesAllowed("USER")
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public TripOuterClass.GetTripsResponse getTrips(@Context ContainerRequestContext requestContext) {
    String username = (String) requestContext.getProperty("username");

    // Evidence is not defined as it does not need to be sent back to the client.
    return TripOuterClass.GetTripsResponse.newBuilder()
        .addAllTrips(
            tripService.getTrips(username).stream()
                .map(Trip::toProtobuf)
                .collect(Collectors.toList()))
        .build();
  }

  @POST
  @RolesAllowed("USER")
  @Consumes(MediaType.APPLICATION_PROTOBUF)
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public TripOuterClass.CreateOrUpdateTripResponse createOrUpdateTrip(
      @Context ContainerRequestContext requestContext, TripOuterClass.Trip tripMessage) {
    if (tripMessage.getVisitsCount() == 0) {
      throw new APIException(
          BAD_REQUEST,
          String.format("No visits were recorded on the trip with ID %s.", tripMessage.getId()));
    }
    String username = (String) requestContext.getProperty("username");
    Map<String, Set<WiFiAPEvidence>> unknownWiFiAPEvidences = new HashMap<>();
    Map<String, List<WiFiAPEvidence>> allWiFiAPEvidences = new HashMap<>();
    Trip trip = parseTrip(tripMessage, username, unknownWiFiAPEvidences, allWiFiAPEvidences);

    Map<String, Integer> rewards = tripService.createOrUpdateTrip(trip, unknownWiFiAPEvidences);
    List<String> awardedBadges = badgingService.awardBadges(username);

    new Thread(
            () -> {
              // Reward witnesses with XP and gems.
              scoreboardService.rewardWitnesses(trip);
              // Review whether witnesses have earned badges.
              badgingService.rewardWitnesses(trip);
            })
        .start();

    // Invocation of the Scavenger.
    if (Boolean.parseBoolean(PROPS.getProperty("SCAVENGER"))) {
      new Thread(() -> submitWiFiAPObservations(trip, allWiFiAPEvidences)).start();
    }

    // Invocation of the LCT.
    if (trip.isCompleted() && Boolean.parseBoolean(PROPS.getProperty("LCT"))) {
      new Thread(() -> lctService.submitLCertificates(trip)).start();
    }

    return TripOuterClass.CreateOrUpdateTripResponse.newBuilder()
        .setCompletedTrip(trip.isCompleted())
        .putAllVisitVerificationStatus(
            trip.getVisits().stream()
                .collect(Collectors.toMap(Visit::getId, Visit::getProtobufStatus)))
        .setAwardedScore(rewards.get("awardedScore"))
        .setAwardedGems(rewards.get("awardedGems"))
        .addAllAwardedBadges(awardedBadges)
        .build();
  }

  /***************************************************************
   * {@link TripController#createOrUpdateTrip} auxiliary methods *
   ***************************************************************/

  public Trip parseTrip(
      TripOuterClass.Trip tripMessage,
      String username,
      Map<String, Set<WiFiAPEvidence>> unknownWiFiAPEvidences,
      Map<String, List<WiFiAPEvidence>> allWiFiAPEvidences) {

    Trip trip = new Trip(tripMessage.getId(), tripMessage.getRouteId(), username);
    for (TripOuterClass.Visit visitMessage : tripMessage.getVisitsList()) {
      Visit visit = parseVisit(visitMessage, unknownWiFiAPEvidences, allWiFiAPEvidences);
      trip.addVisit(visit);
    }
    return trip;
  }

  private Visit parseVisit(
      TripOuterClass.Visit visitMessage,
      Map<String, Set<WiFiAPEvidence>> unknownWiFiAPEvidences,
      Map<String, List<WiFiAPEvidence>> allWiFiAPEvidences) {

    Visit visit = Visit.fromProtobuf(visitMessage);
    allWiFiAPEvidences.putIfAbsent(visit.getId(), new ArrayList<>());
    unknownWiFiAPEvidences.putIfAbsent(visit.getId(), new HashSet<>());

    Map<String, Integer> occurrencesOfWiFiAPEvidences = new HashMap<>();
    long maxOccurrences = visit.getDurationSeconds() / WIFI_SCAN_PERIOD_SECONDS + 1;

    for (Evidence.VisitEvidence visitEvidence : visitMessage.getVisitEvidencesList()) {
      switch (visitEvidence.getEvidenceCase()) {
        case WIFIAPEVIDENCE:
          WiFiAPEvidence wiFiAPEvidence =
              WiFiAPEvidence.fromProtobuf(visitEvidence.getWiFiAPEvidence());
          String bssid = wiFiAPEvidence.getBssid();
          String ssid = wiFiAPEvidence.getSsid();
          allWiFiAPEvidences.get(visit.getId()).add(wiFiAPEvidence);

          if (wiFiAPService.hasWiFiAP(bssid)) {
            visit.addWiFiAPEvidence(wiFiAPEvidence);
          } else {
            unknownWiFiAPEvidences.get(visit.getId()).add(wiFiAPEvidence);
          }

          int occurrences = occurrencesOfWiFiAPEvidences.getOrDefault(bssid + ssid, 0) + 1;
          if (occurrences > maxOccurrences) {
            throw new APIException(
                BAD_REQUEST,
                String.format(
                    "The visit with ID %s exceeds the maximum number of possible occurrences for a single WiFi AP within the visit duration.",
                    visit.getId()));
          }
          occurrencesOfWiFiAPEvidences.put(bssid + ssid, occurrences);
          break;
        case PEERENDORSEMENT:
          visit.addPeerTestimony(PeerTestimony.fromProtobuf(visitEvidence.getPeerEndorsement()));
          break;
      }
    }
    return visit;
  }

  /**********************************************
   * {@link ScavengerService} auxiliary methods *
   **********************************************/

  private void submitWiFiAPObservations(
      Trip trip, Map<String, List<WiFiAPEvidence>> allWiFiAPEvidences) {

    for (Visit visit : trip.getVisits()) {
      if (allWiFiAPEvidences.get(visit.getId()).isEmpty()) continue;

      try {
        scavengerService.publishWiFiAPObservations(
            visit.getPoiId(), trip.getTraveler(), allWiFiAPEvidences.get(visit.getId()), true);
      } catch (ExecutionException
          | InterruptedException
          | IOException
          | NoSuchAlgorithmException e) {
        LOGGER.error("Failed to submit Wi-Fi AP observations to visit with ID {}.", visit.getId());
      }
    }
  }
}
