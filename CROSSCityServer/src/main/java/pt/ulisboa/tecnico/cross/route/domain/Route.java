package pt.ulisboa.tecnico.cross.route.domain;

import pt.ulisboa.tecnico.cross.contract.RouteOuterClass;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Route {

  private final String id;
  private final int position;
  private final String imageUrl;
  private final String mainLocale;
  private final Map<String, String> names;
  private final Map<String, String> descriptions;
  private final Map<String, Waypoint> waypoints;

  public Route(String id, int position, String imageUrl, String mainLocale) {
    this.id = id;
    this.position = position;
    this.imageUrl = imageUrl;
    this.mainLocale = mainLocale;
    this.names = new HashMap<>();
    this.descriptions = new HashMap<>();
    this.waypoints = new HashMap<>();
  }

  public String getId() {
    return id;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getMainLocale() {
    return mainLocale;
  }

  public Map<String, String> getNames() {
    return names;
  }

  public Map<String, String> getDescriptions() {
    return descriptions;
  }

  public Map<String, Waypoint> getWaypoints() {
    return waypoints;
  }

  public void putName(String lang, String name) {
    names.put(lang, name);
  }

  public void putDescription(String lang, String description) {
    descriptions.put(lang, description);
  }

  public void putWaypoint(Waypoint waypoint) {
    waypoints.put(waypoint.getId(), waypoint);
  }

  public RouteOuterClass.Route toProtobuf() {
    return RouteOuterClass.Route.newBuilder()
        .setId(id)
        .setPosition(position)
        .setImageURL(imageUrl)
        .setMainLocale(mainLocale)
        .putAllNames(names)
        .putAllDescriptions(descriptions)
        .addAllWaypoints(
            waypoints.values().stream().map(Waypoint::toProtobuf).collect(Collectors.toList()))
        .build();
  }
}
