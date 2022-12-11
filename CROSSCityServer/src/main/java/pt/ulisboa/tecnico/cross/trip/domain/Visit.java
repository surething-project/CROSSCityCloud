package pt.ulisboa.tecnico.cross.trip.domain;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import eu.surething_project.core.*;
import org.jetbrains.annotations.NotNull;
import pt.ulisboa.tecnico.cross.contract.Evidence;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass;
import pt.ulisboa.tecnico.cross.error.APIException;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

public class Visit implements Comparable<Visit> {

  private final String id;
  private final String poiId;
  private final Instant entryTime;
  private final Instant leaveTime;
  private final Set<WiFiAPEvidence> wiFiAPEvidences;
  private Set<PeerTestimony> peerTestimonies;
  private VERIFICATION_STATUS verificationStatus;

  public Visit(String id, String poiId, Instant entryTime, Instant leaveTime) {
    this.id = id;
    this.poiId = poiId;
    this.entryTime = entryTime;
    this.leaveTime = leaveTime;
    this.wiFiAPEvidences = new HashSet<>();
    this.peerTestimonies = new HashSet<>();
    this.verificationStatus = VERIFICATION_STATUS.OK;
  }

  public String getId() {
    return id;
  }

  public String getPoiId() {
    return poiId;
  }

  public Instant getEntryTime() {
    return entryTime;
  }

  public Instant getLeaveTime() {
    return leaveTime;
  }

  public Set<WiFiAPEvidence> getWiFiAPEvidences() {
    return wiFiAPEvidences;
  }

  public Set<PeerTestimony> getPeerTestimonies() {
    return peerTestimonies;
  }

  public void setPeerTestimonies(Set<PeerTestimony> peerTestimonies) {
    this.peerTestimonies = peerTestimonies;
  }

  public VERIFICATION_STATUS getVerificationStatus() {
    return verificationStatus;
  }

  public void setVerificationStatus(VERIFICATION_STATUS verificationStatus) {
    this.verificationStatus = verificationStatus;
  }

  public void addWiFiAPEvidence(WiFiAPEvidence wiFiAPEvidence) {
    wiFiAPEvidences.add(wiFiAPEvidence);
  }

  public void addPeerTestimony(PeerTestimony peerTestimony) {
    peerTestimonies.add(peerTestimony);
  }

  public long getDurationSeconds() {
    return leaveTime.getEpochSecond() - entryTime.getEpochSecond();
  }

  public TripOuterClass.VisitVerificationStatus getProtobufStatus() {
    switch (verificationStatus) {
      case OK:
        return TripOuterClass.VisitVerificationStatus.OK;
      case NOT_ENOUGH_CONFIDENCE:
        return TripOuterClass.VisitVerificationStatus.NOT_ENOUGH_CONFIDENCE;
      case SHORT_DURATION:
        return TripOuterClass.VisitVerificationStatus.SHORT_DURATION;
      default:
        return null;
    }
  }

  public static Visit fromProtobuf(TripOuterClass.Visit visit) {

    Instant entryTime =
        Instant.ofEpochSecond(visit.getEntryTime().getSeconds(), visit.getEntryTime().getNanos());
    Instant leaveTime =
        Instant.ofEpochSecond(visit.getLeaveTime().getSeconds(), visit.getLeaveTime().getNanos());

    if (leaveTime.isBefore(entryTime)) {
      throw new APIException(
          BAD_REQUEST,
          String.format(
              "The visit with ID %s reports that the visit started after it ended.",
              visit.getId()));
    }

    return new Visit(visit.getId(), visit.getPoiId(), entryTime, leaveTime);
  }

  public TripOuterClass.Visit toProtobuf() {

    TripOuterClass.Visit.Builder visitBuilder = TripOuterClass.Visit.newBuilder();
    visitBuilder.setId(id);
    visitBuilder.setPoiId(poiId);

    visitBuilder.setEntryTime(
        Timestamp.newBuilder()
            .setSeconds(entryTime.getEpochSecond())
            .setNanos(entryTime.getNano())
            .build());
    visitBuilder.setLeaveTime(
        Timestamp.newBuilder()
            .setSeconds(leaveTime.getEpochSecond())
            .setNanos(leaveTime.getNano()));

    // Evidence is not defined as it does not need to be sent back to the client.
    return visitBuilder.build();
  }

  public LocationClaim toLocationClaimProtobuf() {

    PoI.Builder poiBuilder = PoI.newBuilder();
    poiBuilder.setId(poiId);

    TimeInterval.Builder timeIntervalBuilder = TimeInterval.newBuilder();
    timeIntervalBuilder.setBegin(
        Timestamp.newBuilder()
            .setSeconds(entryTime.getEpochSecond())
            .setNanos(entryTime.getNano())
            .build());
    timeIntervalBuilder.setEnd(
        Timestamp.newBuilder()
            .setSeconds(leaveTime.getEpochSecond())
            .setNanos(leaveTime.getNano())
            .build());

    Evidence.VisitEvidences.Builder visitEvidencesBuilder = Evidence.VisitEvidences.newBuilder();
    wiFiAPEvidences.forEach(
        wiFiAPEvidence ->
            visitEvidencesBuilder.addEvidences(
                Evidence.VisitEvidence.newBuilder()
                    .setWiFiAPEvidence(wiFiAPEvidence.toProtobuf())
                    .build()));
    peerTestimonies.forEach(
        peerTestimony ->
            visitEvidencesBuilder.addEvidences(
                Evidence.VisitEvidence.newBuilder()
                    .setPeerEndorsement(peerTestimony.getPeerEndorsement())
                    .build()));

    return LocationClaim.newBuilder()
        .setClaimId(id)
        .setLocation(Location.newBuilder().setPoi(poiBuilder.build()).build())
        .setTime(Time.newBuilder().setInterval(timeIntervalBuilder.build()).build())
        .setEvidence(Any.pack(visitEvidencesBuilder.build()))
        .build();
  }

  public String wiFiAPEvidencesToString() {
    return wiFiAPEvidences.stream().map(WiFiAPEvidence::toString).collect(Collectors.joining("\n"));
  }

  public String peerTestimoniesToString() {
    return peerTestimonies.stream().map(PeerTestimony::toString).collect(Collectors.joining("\n"));
  }

  @Override
  public int compareTo(@NotNull Visit o) {
    return entryTime.compareTo(o.entryTime);
  }

  @Override
  public String toString() {
    return String.format(
        "Visit{\nID='%s',\nPOI='%s',\nEntryTime='%s',\nLeaveTime='%s',\nWiFiAPEvidences=%s,\nPeerTestimonies=%s,\nVerificationStatus='%s'}",
        id,
        poiId,
        entryTime,
        leaveTime,
        wiFiAPEvidencesToString(),
        peerTestimoniesToString(),
        verificationStatus.name());
  }

  public enum VERIFICATION_STATUS {
    OK,
    SHORT_DURATION,
    NOT_ENOUGH_CONFIDENCE
  }
}
