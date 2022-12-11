package pt.ulisboa.tecnico.cross.route.domain;

import pt.ulisboa.tecnico.cross.contract.RouteOuterClass;

public class Waypoint {

  private final String id;
  private final int position;
  private final String poiId;
  private final long stayForSeconds;
  private final int confidenceThreshold;

  public Waypoint(
      String id, int position, String poiId, long stayForSeconds, int confidenceThreshold) {
    this.id = id;
    this.position = position;
    this.poiId = poiId;
    this.stayForSeconds = stayForSeconds;
    this.confidenceThreshold = confidenceThreshold;
  }

  public String getId() {
    return this.id;
  }

  public String getPoiId() {
    return poiId;
  }

  public long getStayForSeconds() {
    return stayForSeconds;
  }

  public int getConfidenceThreshold() {
    return confidenceThreshold;
  }

  public RouteOuterClass.Waypoint toProtobuf() {
    return RouteOuterClass.Waypoint.newBuilder()
        .setId(id)
        .setPosition(position)
        .setPoiId(poiId)
        .setStayForSeconds(stayForSeconds)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Waypoint && id.equals(((Waypoint) o).id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
