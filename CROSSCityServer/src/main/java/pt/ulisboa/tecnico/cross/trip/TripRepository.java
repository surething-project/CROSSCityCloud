package pt.ulisboa.tecnico.cross.trip;

import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.trip.domain.PeerTestimony;
import pt.ulisboa.tecnico.cross.trip.domain.Trip;
import pt.ulisboa.tecnico.cross.trip.domain.Visit;
import pt.ulisboa.tecnico.cross.trip.domain.WiFiAPEvidence;

import java.sql.*;
import java.time.Instant;
import java.util.*;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.PROPS;
import static pt.ulisboa.tecnico.cross.trip.domain.Visit.VERIFICATION_STATUS.OK;

public class TripRepository {

  private final String TEST_ROUTE_ID = PROPS.getProperty("TEST_ROUTE_ID");
  private final String SCAVENGER_TEST_ROUTE_ID = PROPS.getProperty("SCAVENGER_TEST_ROUTE_ID");

  public static TripRepository get() {
    return TripRepositoryHolder.INSTANCE;
  }

  private TripRepository() {}

  private Trip createTripFromResultSet(Connection con, ResultSet rs) throws SQLException {
    Trip trip =
        new Trip(
            rs.getString("id"),
            rs.getString("route_id"),
            rs.getString("traveler"),
            isCompleted(con, rs.getString("id")));
    Visit lastVisit = null;

    while (!rs.isAfterLast() && trip.getId().equals(rs.getString("id"))) {
      String visitId = rs.getString("visit_id");
      if (lastVisit == null || !Objects.equals(lastVisit.getId(), visitId)) {
        lastVisit =
            new Visit(
                visitId,
                rs.getString("poi_id"),
                rs.getTimestamp("entry_time").toInstant(),
                rs.getTimestamp("leave_time").toInstant());
        trip.addVisit(lastVisit);
      }
      lastVisit.addWiFiAPEvidence(
          new WiFiAPEvidence(
              rs.getString("bssid"), rs.getString("ssid"), rs.getLong("sighting_millis")));
      rs.next();
    }
    return trip;
  }

  public Trip getTrip(Connection con, String id, String username) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT t.*, v.id AS visit_id, v.poi_id, v.entry_time, v.leave_time, we.* "
                + "FROM trip t "
                + "INNER JOIN visit v ON t.id = v.trip_id "
                + "INNER JOIN wifiap_evidence we ON v.id = we.visit_id "
                + "WHERE t.id = ? AND t.traveler = ?");
    stmt.setString(1, id);
    stmt.setString(2, username);
    ResultSet rs = stmt.executeQuery();
    if (!rs.next()) {
      throw new APIException(NOT_FOUND, String.format("Trip with ID %s not found.", id));
    }
    Trip trip = createTripFromResultSet(con, rs);
    stmt.close();
    return trip;
  }

  public List<Trip> getTrips(Connection con, String username) throws SQLException {
    List<Trip> trips = new ArrayList<>();
    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT t.*, v.id AS visit_id, v.poi_id, v.entry_time, v.leave_time, we.* "
                + "FROM trip t "
                + "INNER JOIN visit v ON t.id = v.trip_id "
                + "INNER JOIN wifiap_evidence we ON v.id = we.visit_id "
                + "WHERE t.traveler = ? "
                + "ORDER BY t.id, v.entry_time, v.leave_time");
    stmt.setString(1, username);
    ResultSet rs = stmt.executeQuery();
    if (!rs.next()) return trips;
    while (!rs.isAfterLast()) trips.add(createTripFromResultSet(con, rs));
    stmt.close();
    return trips;
  }

  public List<Visit> getVisits(Connection con, String id) throws SQLException {
    List<Visit> visits = new ArrayList<>();
    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT v.id AS visit_id, v.poi_id, v.entry_time, v.leave_time "
                + "FROM trip t "
                + "INNER JOIN visit v ON t.id = v.trip_id "
                + "WHERE t.id = ? ORDER BY entry_time");
    stmt.setString(1, id);
    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
      visits.add(
          new Visit(
              rs.getString("visit_id"),
              rs.getString("poi_id"),
              rs.getTimestamp("entry_time").toInstant(),
              rs.getTimestamp("leave_time").toInstant()));
    }
    stmt.close();
    return visits;
  }

  public boolean isCoherent(Connection con, Trip trip) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement("SELECT id FROM trip WHERE route_id = ? AND traveler = ?");
    stmt.setString(1, trip.getRouteId());
    stmt.setString(2, trip.getTraveler());
    ResultSet rs = stmt.executeQuery();
    boolean isTripCoherent =
        !rs.next()
            || rs.getString("id").equals(trip.getId())
            || trip.getRouteId().equals(TEST_ROUTE_ID)
            || trip.getRouteId().equals(SCAVENGER_TEST_ROUTE_ID);
    stmt.close();
    return isTripCoherent;
  }

  public boolean isCompleted(Connection con, String tripId) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement("SELECT submit_time FROM completed_trip WHERE trip_id = ?");
    stmt.setString(1, tripId);
    ResultSet rs = stmt.executeQuery();
    boolean isCompleted = rs.next();
    stmt.close();
    return isCompleted;
  }

  public void createTrip(
      Connection con, Trip trip, Map<String, Set<WiFiAPEvidence>> unknownWiFiAPEvidences)
      throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement(
            "INSERT INTO trip (id, route_id, traveler) VALUES (?, ?, ?) ON CONFLICT DO NOTHING");
    stmt.setString(1, trip.getId());
    stmt.setString(2, trip.getRouteId());
    stmt.setString(3, trip.getTraveler());
    stmt.executeUpdate();
    stmt.close();
    createVisits(con, trip.getId(), trip.getVisits(), unknownWiFiAPEvidences);
    if (trip.isCompleted()) createCompleteTrip(con, trip.getId());
  }

  public void createVisits(
      Connection con,
      String tripId,
      List<Visit> visits,
      Map<String, Set<WiFiAPEvidence>> unknownWiFiAPEvidences)
      throws SQLException {
    for (Visit visit : visits) {
      if (visit.getVerificationStatus().equals(OK)) {
        PreparedStatement stmt =
            con.prepareStatement(
                "INSERT INTO visit (id, trip_id, poi_id, entry_time, leave_time) VALUES (?, ?, ?, ?, ?)");
        stmt.setString(1, visit.getId());
        stmt.setString(2, tripId);
        stmt.setString(3, visit.getPoiId());
        stmt.setTimestamp(4, Timestamp.from(visit.getEntryTime()));
        stmt.setTimestamp(5, Timestamp.from(visit.getLeaveTime()));
        stmt.executeUpdate();
        stmt.close();
        createWiFiAPEvidences(con, visit.getId(), visit.getWiFiAPEvidences());
        if (unknownWiFiAPEvidences.containsKey(visit.getId())) {
          createUnknownWiFiAPEvidences(
              con, visit.getId(), unknownWiFiAPEvidences.get(visit.getId()));
        }
        createPeerTestimonies(con, visit.getId(), visit.getPeerTestimonies());
      }
    }
  }

  public void createWiFiAPEvidences(
      Connection con, String visitId, Set<WiFiAPEvidence> wiFiAPEvidences) throws SQLException {
    for (WiFiAPEvidence wiFiAPEvidence : wiFiAPEvidences) {
      PreparedStatement stmt =
          con.prepareStatement(
              "INSERT INTO wifiap_evidence (visit_id, bssid, ssid, sighting_millis) VALUES (?, ?, ?, ?)");
      stmt.setString(1, visitId);
      stmt.setString(2, wiFiAPEvidence.getBssid());
      stmt.setString(3, wiFiAPEvidence.getSsid());
      stmt.setLong(4, wiFiAPEvidence.getSightingMillis());
      stmt.executeUpdate();
      stmt.close();
    }
  }

  public void createUnknownWiFiAPEvidences(
      Connection con, String visitId, Set<WiFiAPEvidence> unknownWiFiAPEvidences)
      throws SQLException {
    for (WiFiAPEvidence wiFiAPEvidence : unknownWiFiAPEvidences) {
      PreparedStatement stmt =
          con.prepareStatement(
              "INSERT INTO unknown_wifiap_evidence (visit_id, bssid, ssid, sighting_millis) VALUES (?, ?, ?, ?)");
      stmt.setString(1, visitId);
      stmt.setString(2, wiFiAPEvidence.getBssid());
      stmt.setString(3, wiFiAPEvidence.getSsid());
      stmt.setLong(4, wiFiAPEvidence.getSightingMillis());
      stmt.executeUpdate();
      stmt.close();
    }
  }

  public void createPeerTestimonies(
      Connection con, String visitId, Set<PeerTestimony> peerTestimonies) throws SQLException {
    for (PeerTestimony peerTestimony : peerTestimonies) {
      PreparedStatement stmt =
          con.prepareStatement(
              "INSERT INTO peer_testimony (visit_id, witness, peer_endorsement) VALUES (?, ?, ?)");
      stmt.setString(1, visitId);
      stmt.setString(2, peerTestimony.getWitness());
      stmt.setBytes(3, peerTestimony.getPeerEndorsement().toByteArray());
      stmt.executeUpdate();
      stmt.close();
    }
  }

  public void createCompleteTrip(Connection con, String tripId) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement("INSERT INTO completed_trip (trip_id, submit_time) VALUES (?, ?)");
    stmt.setString(1, tripId);
    stmt.setTimestamp(2, Timestamp.from(Instant.now()));
    stmt.executeUpdate();
    stmt.close();
  }

  public int getNumberOfTestimonies(Connection con, String prover, String witness)
      throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT COUNT(DISTINCT t.id) "
                + "FROM trip t "
                + "INNER JOIN visit v ON t.id = v.trip_id "
                + "INNER JOIN peer_testimony pt ON v.id = pt.visit_id "
                + "WHERE t.traveler = ? AND pt.witness = ?");
    stmt.setString(1, prover);
    stmt.setString(2, witness);

    ResultSet rs = stmt.executeQuery();
    int numberOfTestimonies = rs.next() ? rs.getInt("count") : 0;
    stmt.close();
    return numberOfTestimonies;
  }

  public int getNumberOfTestimonies(Connection con, String witness) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement("SELECT COUNT(*) FROM peer_testimony WHERE witness = ?");
    stmt.setString(1, witness);

    ResultSet rs = stmt.executeQuery();
    int numberOfTestimonies = rs.next() ? rs.getInt("count") : 0;
    stmt.close();
    return numberOfTestimonies;
  }

  public int getNumberOfTraveledVisits(Connection con, String username) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT COUNT(*) "
                + "FROM trip t "
                + "INNER JOIN visit v ON t.id = v.trip_id "
                + "WHERE t.traveler = ?");
    stmt.setString(1, username);

    ResultSet rs = stmt.executeQuery();
    int numberOfVisits = rs.next() ? rs.getInt("count") : 0;
    stmt.close();
    return numberOfVisits;
  }

  public int getNumberOfTraveledRoutes(Connection con, String username) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT COUNT(*) "
                + "FROM completed_trip ct "
                + "INNER JOIN trip t ON ct.trip_id = t.id "
                + "WHERE t.traveler = ?");
    stmt.setString(1, username);

    ResultSet rs = stmt.executeQuery();
    int numberOfRoutes = rs.next() ? rs.getInt("count") : 0;
    stmt.close();
    return numberOfRoutes;
  }

  private static class TripRepositoryHolder {
    private static final TripRepository INSTANCE = new TripRepository();
  }
}
