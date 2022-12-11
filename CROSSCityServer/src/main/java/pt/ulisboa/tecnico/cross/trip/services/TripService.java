package pt.ulisboa.tecnico.cross.trip.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.poi.POIRepository;
import pt.ulisboa.tecnico.cross.poi.domain.POI;
import pt.ulisboa.tecnico.cross.route.RouteRepository;
import pt.ulisboa.tecnico.cross.route.domain.Route;
import pt.ulisboa.tecnico.cross.route.domain.Waypoint;
import pt.ulisboa.tecnico.cross.scoreboard.ScoreboardRepository;
import pt.ulisboa.tecnico.cross.trip.TripRepository;
import pt.ulisboa.tecnico.cross.trip.domain.PeerTestimony;
import pt.ulisboa.tecnico.cross.trip.domain.Trip;
import pt.ulisboa.tecnico.cross.trip.domain.Visit;
import pt.ulisboa.tecnico.cross.trip.domain.WiFiAPEvidence;
import pt.ulisboa.tecnico.cross.user.UserRepository;
import pt.ulisboa.tecnico.surerepute.Report;
import pt.ulisboa.tecnico.surerepute.SureReputeClient;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.grum.geocalc.EarthCalc.haversine.distance;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.PROPS;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.getDb;
import static pt.ulisboa.tecnico.cross.trip.domain.Visit.VERIFICATION_STATUS.*;
import static pt.ulisboa.tecnico.surerepute.Report.*;

@ApplicationScoped
public class TripService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TripService.class);
  private final int MAX_EXPECTED_TRAVEL_SPEED =
      Integer.parseInt(PROPS.getProperty("MAX_EXPECTED_TRAVEL_SPEED")); // km/h
  private final int MAX_ADMISSIBLE_TRAVEL_SPEED =
      Integer.parseInt(PROPS.getProperty("MAX_ADMISSIBLE_TRAVEL_SPEED")); // km/h
  private final int TRAVELER_SCORE_INCREMENT =
      Integer.parseInt(PROPS.getProperty("TRAVELER_SCORE_INCREMENT"));
  private final int TRAVELER_GEMS_INCREMENT =
      Integer.parseInt(PROPS.getProperty("TRAVELER_GEMS_INCREMENT"));
  private final TripRepository tripRepository = TripRepository.get();
  private final ScoreboardRepository scoreboardRepository = ScoreboardRepository.get();
  private final RouteRepository routeRepository = RouteRepository.get();
  private final POIRepository poiRepository = POIRepository.get();
  private final UserRepository userRepository = UserRepository.get();
  private final SureReputeClient sureReputeClient = SureReputeClient.get();
  private final WiFiAPEvidenceValidation wiFiAPEvidenceValidation =
      new WiFiAPEvidenceValidation(sureReputeClient);
  private final PeerEndorsementsValidation peerEndorsementsValidation =
      new PeerEndorsementsValidation(sureReputeClient);

  public List<Trip> getTrips(String username) {
    try (Connection con = getDb()) {
      return tripRepository.getTrips(con, username);
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }

  public Trip getTrip(String username, String id) {
    try (Connection con = getDb()) {
      return tripRepository.getTrip(con, username, id);
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }

  public Map<String, Integer> createOrUpdateTrip(
      Trip trip, Map<String, Set<WiFiAPEvidence>> unknownWiFiAPEvidences) {
    Map<String, Integer> rewards = new HashMap<>();

    try (Connection con = getDb()) {
      con.setAutoCommit(false);

      if (!tripRepository.isCoherent(con, trip)) {
        sureReputeClient.reportBehaviorNonBlocking(
            trip.getTraveler(), INTENTIONALLY_MALICIOUS_CRITICAL);
        throw new APIException(
            BAD_REQUEST,
            String.format(
                "The trip with the ID %s is not consistent with the stored data.", trip.getId()));
      }
      Route route = routeRepository.getRoute(con, trip.getRouteId());

      List<Visit> previousVisits = tripRepository.getVisits(con, trip.getId());
      Visit lastVisit =
          !previousVisits.isEmpty() ? previousVisits.get(previousVisits.size() - 1) : null;
      Set<String> visitedPOIs =
          previousVisits.stream().map(Visit::getPoiId).collect(Collectors.toSet());

      verifyVisits(con, trip, route, lastVisit, visitedPOIs);
      int numberOfVerifiedVisits =
          (int)
              trip.getVisits().stream()
                  .map(Visit::getVerificationStatus)
                  .filter(verificationStatus -> verificationStatus == OK)
                  .count();

      trip.setCompleted(
          previousVisits.size() + numberOfVerifiedVisits == route.getWaypoints().size());
      tripRepository.createTrip(con, trip, unknownWiFiAPEvidences);

      int awardedScore =
          calculateReward(TRAVELER_SCORE_INCREMENT, previousVisits.size(), numberOfVerifiedVisits);
      int awardedGems =
          calculateReward(TRAVELER_GEMS_INCREMENT, previousVisits.size(), numberOfVerifiedVisits);
      if (awardedScore > 0) scoreboardRepository.updateScore(con, trip.getTraveler(), awardedScore);
      if (awardedGems > 0) userRepository.updateGems(con, trip.getTraveler(), awardedGems);

      con.commit();
      rewards.put("awardedScore", awardedScore);
      rewards.put("awardedGems", awardedGems);
      return rewards;
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }

  private void verifyVisits(
      Connection con, Trip trip, Route route, Visit lastVisit, Set<String> visitedPOIs)
      throws SQLException {
    Map<String, Waypoint> poisWaypoints =
        route.getWaypoints().values().stream()
            .collect(Collectors.toMap(Waypoint::getPoiId, Function.identity()));

    for (Visit visit : trip.getVisits()) {
      Waypoint waypoint = poisWaypoints.getOrDefault(visit.getPoiId(), null);

      if (waypoint == null) {
        sureReputeClient.reportBehaviorNonBlocking(
            trip.getTraveler(), INTENTIONALLY_MALICIOUS_CRITICAL);
        throw new APIException(
            BAD_REQUEST,
            String.format("The POI with ID %s does not exist in this route.", visit.getPoiId()));
      }
      if (visitedPOIs.contains(visit.getPoiId())) {
        sureReputeClient.reportBehaviorNonBlocking(
            trip.getTraveler(), INTENTIONALLY_MALICIOUS_CRITICAL);
        throw new APIException(
            BAD_REQUEST,
            String.format(
                "The POI with ID %s has already been visited in this route.", visit.getPoiId()));
      }
    }

    POI visitedPOI;
    POI lastVisitedPOI = getPOI(con, lastVisit);

    Collections.sort(trip.getVisits());
    for (Visit visit : trip.getVisits()) {
      Waypoint waypoint = poisWaypoints.get(visit.getPoiId());

      if (visit.getDurationSeconds() < waypoint.getStayForSeconds()) {
        visit.setVerificationStatus(SHORT_DURATION);
        LOGGER.error("The visit did not last for {} seconds.", waypoint.getStayForSeconds());
        continue;
      }

      // Validation of peer endorsements, to extract the witnesses whose reputation has to be
      // requested.
      peerEndorsementsValidation.validatePeerTestimonies(con, trip, visit);

      List<String> users = new ArrayList<>();
      users.add(trip.getTraveler());
      users.addAll(
          visit.getPeerTestimonies().stream()
              .map(PeerTestimony::getWitness)
              .collect(Collectors.toList()));
      Map<String, Double> userReputations = sureReputeClient.getReputationScores(users);

      double travelerReputation = userReputations.get(trip.getTraveler());
      LOGGER.info("The traveler '{}' has reputation {}.", trip.getTraveler(), travelerReputation);

      // Confidence calculation

      visitedPOI = getPOI(con, visit);
      double confidence =
          calcConfidence(con, trip, visit, lastVisit, visitedPOI, lastVisitedPOI, userReputations)
              * 100;
      LOGGER.info("Combined confidence acquired: " + confidence);

      double confidenceThreshold =
          travelerReputation < 0.5
              ? 100 - (100 - waypoint.getConfidenceThreshold()) / 0.5 * travelerReputation
              : waypoint.getConfidenceThreshold();
      LOGGER.info("Confidence threshold: " + confidenceThreshold);

      if (confidence < confidenceThreshold) {
        visit.setVerificationStatus(NOT_ENOUGH_CONFIDENCE);
        LOGGER.error("The confidence of {} was not acquired.", confidenceThreshold);
        continue;
      }

      lastVisitedPOI = visitedPOI;
      lastVisit = visit;
    }

    for (Visit visit : trip.getVisits()) {
      Map<String, Report> behaviors = new HashMap<>();
      behaviors.put(
          trip.getTraveler(),
          visit.getVerificationStatus() == OK ? WELL_BEHAVED : ACCIDENTALLY_MALICIOUS);
      if (visit.getVerificationStatus() == OK) {
        visit.getPeerTestimonies().stream()
            .map(PeerTestimony::getWitness)
            .forEach(witness -> behaviors.put(witness, WELL_BEHAVED));
      }
      sureReputeClient.reportBehaviorsNonBlocking(behaviors);
    }
  }

  private double calcConfidence(
      Connection con,
      Trip trip,
      Visit visit,
      Visit lastVisit,
      POI visitedPOI,
      POI lastVisitedPOI,
      Map<String, Double> userReputations) {

    double displacementConfidenceMultiplier =
        calcDisplacementConfidenceMultiplier(visit, lastVisit, visitedPOI, lastVisitedPOI);
    LOGGER.info("Displacement confidence: " + displacementConfidenceMultiplier);
    if (displacementConfidenceMultiplier == 0) {
      sureReputeClient.reportBehaviorNonBlocking(trip.getTraveler(), INTENTIONALLY_MALICIOUS);
      throw new APIException(BAD_REQUEST, "Displacement between reported visits is impossible.");
    }

    double wiFiAPsConfidence = wiFiAPEvidenceValidation.calcConfidence(trip, visit, visitedPOI);
    LOGGER.info("Confidence gained in the Wi-Fi APs: " + wiFiAPsConfidence);

    double peerEndorsementsConfidence =
        peerEndorsementsValidation.calcConfidence(con, trip, visit, userReputations);
    LOGGER.info("Confidence gained in the peer endorsements: " + peerEndorsementsConfidence);

    return Math.min(
        displacementConfidenceMultiplier * (wiFiAPsConfidence + peerEndorsementsConfidence), 1);
  }

  private double calcDisplacementConfidenceMultiplier(
      Visit visit, Visit lastVisit, POI visitedPOI, POI lastVisitedPOI) {
    if (lastVisit == null) return 1;

    double travelDistance = distance(lastVisitedPOI.getLocation(), visitedPOI.getLocation());
    double travelTime =
        visit.getEntryTime().getEpochSecond() - lastVisit.getLeaveTime().getEpochSecond();
    if (travelTime <= 0) return 0;

    double travelSpeed = (travelDistance / 1000) / (travelTime / 3600); // km/h
    if (travelSpeed <= MAX_EXPECTED_TRAVEL_SPEED) return 1;
    if (travelSpeed >= MAX_ADMISSIBLE_TRAVEL_SPEED) return 0;
    return 1
        - Math.pow(
            (travelSpeed - MAX_EXPECTED_TRAVEL_SPEED)
                / (MAX_ADMISSIBLE_TRAVEL_SPEED - MAX_EXPECTED_TRAVEL_SPEED),
            2);
  }

  /*********************
   * Auxiliary methods *
   *********************/

  private POI getPOI(Connection con, Visit visit) throws SQLException {
    return visit != null ? poiRepository.getPOI(con, visit.getPoiId()) : null;
  }

  private int calculateReward(
      int incrementValue, int numberOfPreviousVisits, int numberOfVerifiedVisits) {
    // Σ_k={1...numberOfVerifiedVisits} [(numberOfPreviousVisits + k) * incrementValue]
    // So that the reward is more generous as the number of verified visits on the route in
    // question, encouraging users to complete the routes already initiated.
    return incrementValue
        * numberOfVerifiedVisits
        * (numberOfPreviousVisits + (numberOfVerifiedVisits + 1) / 2);
  }
}
