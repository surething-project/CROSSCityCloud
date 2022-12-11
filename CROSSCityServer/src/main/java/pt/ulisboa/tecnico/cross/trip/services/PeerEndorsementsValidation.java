package pt.ulisboa.tecnico.cross.trip.services;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.Claim;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.Endorsement;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.PeerEndorsement;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.SignedEndorsement;
import pt.ulisboa.tecnico.cross.trip.TripRepository;
import pt.ulisboa.tecnico.cross.trip.domain.PeerTestimony;
import pt.ulisboa.tecnico.cross.trip.domain.Trip;
import pt.ulisboa.tecnico.cross.trip.domain.Visit;
import pt.ulisboa.tecnico.cross.user.UserRepository;
import pt.ulisboa.tecnico.cross.utils.Keychain;
import pt.ulisboa.tecnico.surerepute.SureReputeClient;

import java.security.PublicKey;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.PROPS;
import static pt.ulisboa.tecnico.surerepute.Report.INTENTIONALLY_MALICIOUS;

public class PeerEndorsementsValidation {

  private static final Logger LOGGER = LoggerFactory.getLogger(PeerEndorsementsValidation.class);
  private final long MAX_CLOCK_SKEW_SECONDS =
      Long.parseLong(PROPS.getProperty("MAX_CLOCK_SKEW_SECONDS"));
  private final long MAX_PROTOCOL_DURATION_SECONDS =
      Long.parseLong(PROPS.getProperty("MAX_PROTOCOL_DURATION_SECONDS"));
  private final int N_CHALLENGE_ITERATIONS =
      Integer.parseInt(PROPS.getProperty("N_CHALLENGE_ITERATIONS"));
  private final float ENDORSEMENT_WEIGHT_TARGET =
      Float.parseFloat(PROPS.getProperty("ENDORSEMENT_WEIGHT_TARGET"));
  private final UserRepository userRepository = UserRepository.get();
  private final TripRepository tripRepository = TripRepository.get();
  private final Keychain keychain = Keychain.get();
  private final SureReputeClient sureReputeClient;

  PeerEndorsementsValidation(SureReputeClient sureReputeClient) {
    this.sureReputeClient = sureReputeClient;
  }

  void validatePeerTestimonies(Connection con, Trip trip, Visit visit) {

    List<PeerEndorsement> peerEndorsements =
        visit.getPeerTestimonies().stream()
            .map(PeerTestimony::getPeerEndorsement)
            .collect(Collectors.toList());

    Set<PeerTestimony> peerTestimonies = new HashSet<>();
    PublicKey proverPublicKey = null;

    for (PeerEndorsement peerEndorsement : peerEndorsements) {

      // Obtaining the claim.
      Claim claim = peerEndorsement.getSignedClaim().getClaim();
      ByteString proverSignature = peerEndorsement.getSignedClaim().getProverSignature();
      Instant claimInstant =
          Instant.ofEpochSecond(claim.getTimestamp().getSeconds(), claim.getTimestamp().getNanos());

      // Validation of the claim identification fields.
      if (!claim.getProverId().equals(trip.getTraveler())) {
        LOGGER.error(
            "The '{}' claimant is not the '{}' traveler.", claim.getProverId(), trip.getTraveler());
        sureReputeClient.reportBehaviorNonBlocking(trip.getTraveler(), INTENTIONALLY_MALICIOUS);
        throw new APIException(BAD_REQUEST, "Fraud detection in a claim.");
      }
      if (!claim.getPoiId().equals(visit.getPoiId())) {
        LOGGER.error(
            "The claim is directed to the '{}' point of interest, but the one being visited is the '{}'.",
            claim.getPoiId(),
            visit.getPoiId());
        sureReputeClient.reportBehaviorNonBlocking(trip.getTraveler(), INTENTIONALLY_MALICIOUS);
        throw new APIException(BAD_REQUEST, "Fraud detection in a claim.");
      }
      if (claimInstant.isBefore(visit.getEntryTime())
          || claimInstant.isAfter(visit.getLeaveTime())) {
        LOGGER.error(
            "The claim was not generated ({}) during the visit [{}, {}].",
            claimInstant,
            visit.getEntryTime(),
            visit.getLeaveTime());
        sureReputeClient.reportBehaviorNonBlocking(trip.getTraveler(), INTENTIONALLY_MALICIOUS);
        throw new APIException(BAD_REQUEST, "Fraud detection in a claim.");
      }
      if (proverPublicKey == null) {
        proverPublicKey = getPublicKey(con, claim.getProverId(), claim.getProverSessionId());
        if (proverPublicKey == null) {
          sureReputeClient.reportBehaviorNonBlocking(trip.getTraveler(), INTENTIONALLY_MALICIOUS);
          throw new APIException(BAD_REQUEST, "Fraud detection in a claim.");
        }
      }
      if (!keychain.verify(proverPublicKey, claim.toByteArray(), proverSignature.toByteArray())) {
        LOGGER.error("The claim signature is invalid.");
        sureReputeClient.reportBehaviorNonBlocking(trip.getTraveler(), INTENTIONALLY_MALICIOUS);
        throw new APIException(BAD_REQUEST, "Fraud detection in a claim.");
      }

      // Obtaining the endorsement.
      byte[] signedEndorsementBytes =
          keychain.decrypt(peerEndorsement.getEncryptedSignedEndorsement().toByteArray());
      if (signedEndorsementBytes == null) {
        LOGGER.error("The endorsement decryption was not successful.");
        continue;
      }
      SignedEndorsement signedEndorsement;
      try {
        signedEndorsement = SignedEndorsement.parseFrom(signedEndorsementBytes);
      } catch (InvalidProtocolBufferException e) {
        LOGGER.error("The endorsement format is invalid.");
        continue;
      }
      Endorsement endorsement = signedEndorsement.getEndorsement();
      ByteString witnessSignature = signedEndorsement.getWitnessSignature();
      Instant endorsementInstant =
          Instant.ofEpochSecond(
              endorsement.getTimestamp().getSeconds(), endorsement.getTimestamp().getNanos());
      PeerTestimony peerTestimony = new PeerTestimony(endorsement.getWitnessId(), peerEndorsement);

      // Validation of the endorsement identification fields.
      if (endorsement.getWitnessId().equals(claim.getProverId())) {
        LOGGER.error(
            "The witness and claimant are the same user '{}'.", endorsement.getWitnessId());
        continue;
      }
      if (peerTestimonies.contains(peerTestimony)) {
        LOGGER.error(
            "The witness '{}' has already testified for this visit.", endorsement.getWitnessId());
        continue;
      }
      if (!endorsement.getPoiId().equals(visit.getPoiId())) {
        LOGGER.error(
            "The endorsement is directed to the '{}' point of interest, but the one being visited is the '{}'.",
            claim.getPoiId(),
            visit.getPoiId());
        continue;
      }
      if (endorsementInstant.isBefore(claimInstant.minusSeconds(MAX_CLOCK_SKEW_SECONDS))
          || endorsementInstant.isAfter(
              claimInstant.plusSeconds(MAX_CLOCK_SKEW_SECONDS + MAX_PROTOCOL_DURATION_SECONDS))) {
        LOGGER.error(
            "The timestamp that the endorsement ({}) was generated and the claim made ({}) do not match.",
            endorsementInstant,
            claimInstant);
        continue;
      }
      PublicKey witnessPublicKey =
          getPublicKey(con, endorsement.getWitnessId(), endorsement.getWitnessSessionId());
      if (witnessPublicKey == null) continue;
      if (!keychain.verify(
          witnessPublicKey,
          proverSignature.concat(endorsement.toByteString()).toByteArray(),
          witnessSignature.toByteArray())) {
        LOGGER.error("The endorsement signature is invalid.");
        continue;
      }
      if (!validateChallengeResponses(claim, endorsement)) continue;

      // Valid endorsement!
      peerTestimonies.add(peerTestimony);
    }
    visit.setPeerTestimonies(peerTestimonies);
  }

  double calcConfidence(
      Connection con, Trip trip, Visit visit, Map<String, Double> userReputations) {
    double weight = 0;

    for (PeerTestimony peerTestimony : visit.getPeerTestimonies()) {
      double witnessReputation = userReputations.get(peerTestimony.getWitness());
      LOGGER.info("Witness '{}' reputation: {}", peerTestimony.getWitness(), witnessReputation);

      double endorsementWeight =
          witnessReputation / (getNPW(con, trip.getTraveler(), peerTestimony.getWitness()) + 1);
      LOGGER.info(
          "The '{}' witness endorsement weight: {}", peerTestimony.getWitness(), endorsementWeight);
      weight += endorsementWeight;
    }

    LOGGER.info("The sum of the weights of the witness endorsements: " + weight);
    return Math.min(weight / ENDORSEMENT_WEIGHT_TARGET, 1);
  }

  private boolean validateChallengeResponses(Claim claim, Endorsement endorsement) {

    BitSet vA = BitSet.valueOf(claim.getVA().toByteArray());
    BitSet vB = BitSet.valueOf(claim.getVB().toByteArray());
    BitSet vZ = BitSet.valueOf(endorsement.getVH().toByteArray());
    vZ.xor(vB);

    BitSet vC = BitSet.valueOf(endorsement.getVC().toByteArray());
    BitSet vR = BitSet.valueOf(endorsement.getVR().toByteArray());

    for (int i = 0; i < N_CHALLENGE_ITERATIONS; i++) {
      if ((vC.get(i) ? vZ : vA).get(i) != vR.get(i)) {
        LOGGER.error("Challenge with index {} is incorrectly answered.", i);
        return false;
      }
    }
    return true;
  }

  /*********************
   * Auxiliary methods *
   *********************/

  private PublicKey getPublicKey(Connection con, String username, String sessionId) {
    byte[] encodedPublicKey;
    try {
      encodedPublicKey = userRepository.getPublicKey(con, username, sessionId);
    } catch (SQLException e) {
      LOGGER.error(
          "Failed to fetch the public key of user '{}' with session ID '{}' from the database: {}",
          username,
          sessionId,
          e);
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
    if (encodedPublicKey == null) {
      LOGGER.error(
          "The user's '{}' public key does not exist for the session ID '{}'.",
          username,
          sessionId);
      return null;
    }
    PublicKey publicKey = keychain.getPublicKey(encodedPublicKey);
    if (publicKey == null) {
      LOGGER.error(
          "The user's '{}' public key is invalid for the session ID '{}'.", username, sessionId);
    }
    return publicKey;
  }

  private int getNPW(Connection con, String prover, String witness) {
    try {
      int numberOfTestimonies = tripRepository.getNumberOfTestimonies(con, prover, witness);
      LOGGER.info("Number of testimonies: {}", numberOfTestimonies);
      return numberOfTestimonies;
    } catch (SQLException e) {
      LOGGER.error("Failed to fetch the number of testimonies.", e);
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }
}
