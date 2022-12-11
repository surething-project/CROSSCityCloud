package pt.ulisboa.tecnico.cross.scavenger;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.contract.Evidence;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.trip.domain.Trip;
import pt.ulisboa.tecnico.cross.trip.domain.Visit;
import pt.ulisboa.tecnico.cross.trip.domain.WiFiAPEvidence;
import pt.ulisboa.tecnico.cross.utils.MediaType;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

// ** CONTROLLER USED ONLY FOR TEMPORARY SCAVENGER RELATED TESTING
@Path("/scavenger")
public class ScavengerController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ScavengerController.class);

  @Inject private ScavengerService scavengerService;

  @POST
  @RolesAllowed("USER")
  @Consumes(MediaType.APPLICATION_PROTOBUF)
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public TripOuterClass.CreateOrUpdateTripResponse testTripStableVolatileSet(
      @Context ContainerRequestContext requestContext, TripOuterClass.Trip tripMessage) {
    if (tripMessage.getVisitsCount() == 0) {
      throw new APIException(
          BAD_REQUEST,
          String.format(
              "No location claims were recorded on the trip with ID %s.", tripMessage.getId()));
    }
    String username = (String) requestContext.getProperty("username");
    Trip trip = parseVisits(tripMessage, username);

    scavengerService.testStableSetForVisit(trip.getVisits().get(0), 50.0);
    scavengerService.testVolatileSetForVisit(trip.getVisits().get(0), 50.0);

    return TripOuterClass.CreateOrUpdateTripResponse.newBuilder()
        .setCompletedTrip(false)
        .putAllVisitVerificationStatus(
            trip.getVisits().stream()
                .collect(Collectors.toMap(Visit::getId, Visit::getProtobufStatus)))
        .build();
  }

  @POST
  @Path("/obs")
  @PermitAll
  @Consumes(MediaType.APPLICATION_PROTOBUF)
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public WiFiAPObservationOuterClass.WiFiAPObservationList publishWiFiApObs(
      @Context ContainerRequestContext requestContext,
      WiFiAPObservationOuterClass.WiFiAPObservationList observations) {
    List<WiFiAPEvidence> evidences = new ArrayList<>();
    observations
        .getObservationsList()
        .forEach(
            obs -> evidences.add(new WiFiAPEvidence(obs.getBssid(), "", obs.getSightingMillis())));

    try {
      scavengerService.publishWiFiAPObservations(
          observations.getPoiId(), observations.getTraveler(), evidences, true);
    } catch (ExecutionException | InterruptedException | IOException | NoSuchAlgorithmException e) {
      LOGGER.error("Failed to submit Wi-Fi AP observation.");
    }

    return observations;
  }

  @GET
  @PermitAll
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public String createStableSetTable(@Context ContainerRequestContext requestContext) {
    String username = (String) requestContext.getProperty("username");

    scavengerService.createStableSetTable(
        "gulbenkian", Instant.ofEpochSecond(1564358400), Instant.ofEpochSecond(1564876800));
    return username;
  }

  public Trip parseVisits(TripOuterClass.Trip tripMessage, String username) {

    Trip trip = new Trip(tripMessage.getId(), tripMessage.getRouteId(), username);
    for (TripOuterClass.Visit visitMessage : tripMessage.getVisitsList()) {
      Visit visit = Visit.fromProtobuf(visitMessage);

      parseVisitEvidences(visitMessage, visit);
      trip.addVisit(visit);
    }
    return trip;
  }

  private void parseVisitEvidences(TripOuterClass.Visit visitMessage, Visit visit) {

    for (Evidence.VisitEvidence visitEvidence : visitMessage.getVisitEvidencesList()) {
      switch (visitEvidence.getEvidenceCase()) {
        case WIFIAPEVIDENCE:
          WiFiAPEvidence wiFiAPEvidence =
              WiFiAPEvidence.fromProtobuf(visitEvidence.getWiFiAPEvidence());
          // Always add the Wi-Fi AP collected observations to the trip as evidences *FOR TESTING
          visit.addWiFiAPEvidence(wiFiAPEvidence);
          break;
        case PEERENDORSEMENT:
          break;
      }
    }
    if (visit.getWiFiAPEvidences().isEmpty()) {
      throw new APIException(
          BAD_REQUEST,
          String.format(
              "The location claim with ID %s has no known evidence to support it.", visit.getId()));
    }
  }
}
