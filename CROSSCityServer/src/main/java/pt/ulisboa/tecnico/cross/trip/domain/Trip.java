package pt.ulisboa.tecnico.cross.trip.domain;

import pt.ulisboa.tecnico.cross.contract.TripOuterClass;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Trip {

  private final String id;
  private final String routeId;
  private final String traveler;
  private final List<Visit> visits;
  private Boolean completed;

  public Trip(String id, String routeId, String traveler, Boolean completed) {
    this.id = id;
    this.routeId = routeId;
    this.traveler = traveler;
    this.visits = new ArrayList<>();
    this.completed = completed;
  }

  public Trip(String id, String routeId, String traveler) {
    this(id, routeId, traveler, null);
  }

  public String getId() {
    return id;
  }

  public String getRouteId() {
    return routeId;
  }

  public String getTraveler() {
    return traveler;
  }

  public List<Visit> getVisits() {
    return visits;
  }

  public void addVisit(Visit visit) {
    visits.add(visit);
  }

  public Boolean isCompleted() {
    return completed;
  }

  public void setCompleted(Boolean completed) {
    this.completed = completed;
  }

  public TripOuterClass.Trip toProtobuf() {
    return TripOuterClass.Trip.newBuilder()
        .setId(id)
        .setRouteId(routeId)
        .addAllVisits(visits.stream().map(Visit::toProtobuf).collect(Collectors.toList()))
        .setCompleted(completed != null ? completed : false)
        .build();
  }
}
