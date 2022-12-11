package pt.ulisboa.tecnico.cross.peertopeer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.account.CryptoManager;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.*;

import java.time.Instant;
import java.util.BitSet;

// Witness
// https://www.overleaf.com/read/hsgrgwmcmhnr
public class EndorsementIssuance {

  private static final Logger LOGGER = LoggerFactory.getLogger(EndorsementIssuance.class);
  private final String prover;
  private final String witness;
  private final String witnessSessionId;
  private final String poiId;
  private final CryptoManager cryptoManager;
  private byte[] claimSignature;
  private BitSet vH, vC, vR;
  private int i; // Challenge iterator

  public EndorsementIssuance(
      String prover,
      String witness,
      String witnessSessionId,
      String poiId,
      CryptoManager cryptoManager) {
    this.prover = prover;
    this.witness = witness;
    this.witnessSessionId = witnessSessionId;
    this.poiId = poiId;
    this.cryptoManager = cryptoManager;
  }

  public byte[] getPrepare(byte[] claimSignature) {
    this.claimSignature = claimSignature;
    vH = PeerHelper.get().randomValue();
    vC = PeerHelper.get().randomValue();
    vR = new BitSet(PeerHelper.get().N_CHALLENGE_ITERATIONS);
    i = 0;

    Prepare prepare = Prepare.newBuilder().setVH(ByteString.copyFrom(vH.toByteArray())).build();
    return prepare.toByteArray();
  }

  public byte[] getChallenge() {
    Challenge challenge = Challenge.newBuilder().setVCi(vC.get(i++)).build();
    return challenge.toByteArray();
  }

  public boolean setChallengeResponse(byte[] challengeResponseBytes) {
    ChallengeResponse challengeResponse;
    try {
      challengeResponse = ChallengeResponse.parseFrom(challengeResponseBytes);
    } catch (InvalidProtocolBufferException e) {
      LOGGER.error(prover + ": Challenge response parsing failed.", e);
      return false;
    }

    vR.set(i - 1, challengeResponse.getVRi());
    return i == PeerHelper.get().N_CHALLENGE_ITERATIONS;
  }

  public byte[] getEndorsement() {
    Instant instant = Instant.now();
    Endorsement endorsement =
        Endorsement.newBuilder()
            .setWitnessId(witness)
            .setWitnessSessionId(witnessSessionId)
            .setPoiId(poiId)
            .setTimestamp(
                Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build())
            .setVH(ByteString.copyFrom(vH.toByteArray()))
            .setVC(ByteString.copyFrom(vC.toByteArray()))
            .setVR(ByteString.copyFrom(vR.toByteArray()))
            .build();

    byte[] endorsementSignature =
        cryptoManager.sign(
            ByteString.copyFrom(claimSignature).concat(endorsement.toByteString()).toByteArray());
    SignedEndorsement signedEndorsement =
        SignedEndorsement.newBuilder()
            .setEndorsement(endorsement)
            .setWitnessSignature(ByteString.copyFrom(endorsementSignature))
            .build();
    return cryptoManager.encrypt(signedEndorsement.toByteArray());
  }
}
