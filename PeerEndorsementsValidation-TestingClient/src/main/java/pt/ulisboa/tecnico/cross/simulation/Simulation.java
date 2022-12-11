package pt.ulisboa.tecnico.cross.simulation;

import com.google.protobuf.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.api.APIManager;
import pt.ulisboa.tecnico.cross.contract.Evidence.VisitEvidence;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass.Trip;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass.Visit;
import pt.ulisboa.tecnico.cross.peertopeer.EndorsementIssuance;
import pt.ulisboa.tecnico.cross.peertopeer.EvidenceCollection;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.PeerEndorsement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Simulation {

  private static final Logger LOGGER = LoggerFactory.getLogger(Simulation.class);

  public static Simulation get() {
    return SimulationHolder.INSTANCE;
  }

  public long simulate(
      User prover,
      List<User> witnesses,
      String routeId,
      String poiId,
      String tripId,
      String visitId,
      boolean forged) {
    List<PeerEndorsement> endorsements = Collections.synchronizedList(new ArrayList<>());
    List<Thread> evidenceCollectionThreads =
        witnesses.stream()
            .map(
                witness ->
                    new Thread(() -> collectEvidence(prover, witness, poiId, endorsements, forged)))
            .collect(Collectors.toList());

    Instant entryInstant = Instant.now().minusSeconds(TimeUnit.HOURS.toSeconds(1));
    evidenceCollectionThreads.forEach(Thread::start);
    for (Thread evidenceCollectionThread : evidenceCollectionThreads) {
      try {
        evidenceCollectionThread.join();
      } catch (InterruptedException e) {
        LOGGER.error("An evidence collection thread was interrupted.");
        return -1;
      }
    }
    Instant leaveInstant = Instant.now();

    return submit(
        prover, entryInstant, leaveInstant, routeId, poiId, tripId, visitId, endorsements);
  }

  private void collectEvidence(
      User prover, User witness, String poiId, List<PeerEndorsement> endorsements, boolean forged) {
    prover.authenticate();
    if (!forged) witness.authenticate();

    EvidenceCollection evidenceCollection =
        new EvidenceCollection(
            prover.getUsername(),
            prover.getSessionId(),
            witness.getUsername(),
            poiId,
            prover.getCryptoManager());
    EndorsementIssuance endorsementIssuance =
        new EndorsementIssuance(
            prover.getUsername(),
            witness.getUsername(),
            witness.getSessionId(),
            poiId,
            forged ? prover.getCryptoManager() : witness.getCryptoManager());

    evidenceCollection.getReady(
        endorsementIssuance.getPrepare(evidenceCollection.getClaimSignature()));

    byte[] challengeResponse;
    do {
      challengeResponse =
          evidenceCollection.getChallengeResponse(endorsementIssuance.getChallenge());
    } while (!endorsementIssuance.setChallengeResponse(challengeResponse));

    PeerEndorsement endorsement =
        evidenceCollection.setEncryptedSignedEndorsement(endorsementIssuance.getEndorsement());
    endorsements.add(endorsement);
  }

  private long submit(
      User prover,
      Instant entryInstant,
      Instant leaveInstant,
      String routeId,
      String poiId,
      String tripId,
      String visitId,
      List<PeerEndorsement> endorsements) {
    Trip trip =
        Trip.newBuilder()
            .setId(tripId)
            .setRouteId(routeId)
            .addVisits(
                Visit.newBuilder()
                    .setId(visitId)
                    .setPoiId(poiId)
                    .setEntryTime(
                        Timestamp.newBuilder()
                            .setSeconds(entryInstant.getEpochSecond())
                            .setNanos(entryInstant.getNano())
                            .build())
                    .setLeaveTime(
                        Timestamp.newBuilder()
                            .setSeconds(leaveInstant.getEpochSecond())
                            .setNanos(leaveInstant.getNano())
                            .build())
                    .addAllVisitEvidences(
                        endorsements.stream()
                            .map(
                                endorsement ->
                                    VisitEvidence.newBuilder()
                                        .setPeerEndorsement(endorsement)
                                        .build())
                            .collect(Collectors.toList()))
                    .build())
            .build();
    return APIManager.get().getTripAPI().createOrUpdateTrip(prover.getJwt(), trip);
  }

  private static class SimulationHolder {
    private static final Simulation INSTANCE = new Simulation();
  }
}
