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

// Prover
// https://www.overleaf.com/read/hsgrgwmcmhnr
public class EvidenceCollection {

  private static final Logger LOGGER = LoggerFactory.getLogger(EvidenceCollection.class);
  private final String prover;
  private final String proverSessionId;
  private final String witness;
  private final String poiId;
  private final CryptoManager cryptoManager;
  private final BitSet vA;
  private final BitSet vB;
  private SignedClaim signedClaim;
  private BitSet vZ;
  private int i; // Challenge iterator

  public EvidenceCollection(
      String prover,
      String proverSessionId,
      String witness,
      String poiId,
      CryptoManager cryptoManager) {
    this.prover = prover;
    this.proverSessionId = proverSessionId;
    this.witness = witness;
    this.poiId = poiId;
    this.cryptoManager = cryptoManager;
    vA = PeerHelper.get().randomValue();
    vB = PeerHelper.get().randomValue();
  }

  public byte[] getClaimSignature() {
    Instant instant = Instant.now();
    Claim claim =
        Claim.newBuilder()
            .setProverId(prover)
            .setProverSessionId(proverSessionId)
            .setPoiId(poiId)
            .setTimestamp(
                Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build())
            .setVA(ByteString.copyFrom(vA.toByteArray()))
            .setVB(ByteString.copyFrom(vB.toByteArray()))
            .build();

    byte[] claimSignature = cryptoManager.sign(claim.toByteArray());
    signedClaim =
        SignedClaim.newBuilder()
            .setClaim(claim)
            .setProverSignature(ByteString.copyFrom(claimSignature))
            .build();
    return claimSignature;
  }

  public void getReady(byte[] prepareBytes) {
    Prepare prepare;
    try {
      prepare = Prepare.parseFrom(prepareBytes);
    } catch (InvalidProtocolBufferException e) {
      LOGGER.error(witness + ": Prepare parsing failed.", e);
      return;
    }

    vZ = BitSet.valueOf(prepare.getVH().toByteArray());
    vZ.xor(vB);
    i = 0;
  }

  // Logs should be commented out as they introduce a millisecond delay.
  public byte[] getChallengeResponse(byte[] challengeBytes) {
    if (i >= PeerHelper.get().N_CHALLENGE_ITERATIONS) {
      // Challenge completed!
      return null;
    }
    Challenge challenge;
    try {
      challenge = Challenge.parseFrom(challengeBytes);
    } catch (InvalidProtocolBufferException e) {
      LOGGER.error(witness + ": Challenge parsing failed.", e);
      return null;
    }

    boolean vRi = (challenge.getVCi() ? vZ : vA).get(i++);
    ChallengeResponse challengeResponse = ChallengeResponse.newBuilder().setVRi(vRi).build();
    return challengeResponse.toByteArray();
  }

  public PeerEndorsement setEncryptedSignedEndorsement(byte[] encryptedSignedEndorsement) {
    return PeerEndorsement.newBuilder()
        .setSignedClaim(signedClaim)
        .setEncryptedSignedEndorsement(ByteString.copyFrom(encryptedSignedEndorsement))
        .build();
  }
}
