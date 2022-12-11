package pt.ulisboa.tecnico.cross.trip.domain;

import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.PeerEndorsement;

import java.util.Objects;

public class PeerTestimony {

  private final String witness;
  private final PeerEndorsement peerEndorsement;

  public PeerTestimony(String witness, PeerEndorsement peerEndorsement) {
    this.witness = witness;
    this.peerEndorsement = peerEndorsement;
  }

  public String getWitness() {
    return witness;
  }

  public PeerEndorsement getPeerEndorsement() {
    return peerEndorsement;
  }

  public static PeerTestimony fromProtobuf(PeerEndorsement peerEndorsement) {
    return new PeerTestimony(null, peerEndorsement);
  }

  @Override
  public boolean equals(Object o) {
    if (witness == null) return super.equals(o);
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PeerTestimony that = (PeerTestimony) o;
    return Objects.equals(witness, that.witness);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(witness);
  }

  @Override
  public String toString() {
    return String.format(
        "PeerTestimony{POI='%s', Prover='%s', Witness='%s'}",
        peerEndorsement.getSignedClaim().getClaim().getPoiId(),
        peerEndorsement.getSignedClaim().getClaim().getProverId(),
        witness);
  }
}
