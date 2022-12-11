package pt.ulisboa.tecnico.cross.route;

import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.route.domain.Route;
import pt.ulisboa.tecnico.cross.route.domain.Waypoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

public class RouteRepository {

  public static RouteRepository get() {
    return RouteRepositoryHolder.INSTANCE;
  }

  private RouteRepository() {}

  public Route createRouteFromResultSet(ResultSet rs) throws SQLException {
    Route route =
        new Route(
            rs.getString("id"),
            rs.getInt("position"),
            rs.getString("image_url"),
            rs.getString("main_locale"));

    while (!rs.isAfterLast() && route.getId().equals(rs.getString("id"))) {
      String lang = rs.getString("lang");
      String name = rs.getString("name");
      String description = rs.getString("description");
      String waypointId = rs.getString("waypoint_id");

      if (!route.getNames().containsKey(lang)) {
        route.putName(lang, name);
      }
      if (!route.getDescriptions().containsKey(lang)) {
        route.putDescription(lang, description);
      }
      if (!route.getWaypoints().containsKey(waypointId))
        route.putWaypoint(
            new Waypoint(
                waypointId,
                rs.getInt("rw_position"),
                rs.getString("poi_id"),
                rs.getLong("stay_for_seconds"),
                rs.getInt("confidence_threshold")));
      rs.next();
    }
    return route;
  }

  public Route getRoute(Connection con, String id) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT r.*, rl.lang, rl.name, rl.description, rw.waypoint_id, rw.position AS rw_position, w.poi_id, w.confidence_threshold, "
                + "EXTRACT(epoch FROM w.stay_for) AS stay_for_seconds "
                + "FROM route r "
                + "INNER JOIN route_lang rl ON r.id = rl.route_id "
                + "INNER JOIN route_waypoint rw ON r.id = rw.route_id "
                + "INNER JOIN waypoint w ON rw.waypoint_id = w.id "
                + "WHERE r.id = ?");
    stmt.setString(1, id);

    ResultSet rs = stmt.executeQuery();
    if (!rs.next()) {
      throw new APIException(NOT_FOUND, String.format("Route with ID %s not found.", id));
    }
    Route route = createRouteFromResultSet(rs);
    stmt.close();
    return route;
  }

  public List<Route> getRoutes(Connection con) throws SQLException {
    List<Route> routes = new ArrayList<>();

    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT r.*, rl.lang, rl.name, rl.description, rw.waypoint_id, rw.position AS rw_position, w.poi_id, w.confidence_threshold, "
                + "EXTRACT(epoch FROM w.stay_for) AS stay_for_seconds "
                + "FROM route r "
                + "INNER JOIN route_lang rl ON r.id = rl.route_id "
                + "INNER JOIN route_waypoint rw ON r.id = rw.route_id "
                + "INNER JOIN waypoint w ON rw.waypoint_id = w.id "
                + "ORDER BY r.id");

    ResultSet rs = stmt.executeQuery();
    if (!rs.next()) return routes;
    while (!rs.isAfterLast()) routes.add(createRouteFromResultSet(rs));
    stmt.close();
    return routes;
  }

  private static class RouteRepositoryHolder {
    private static final RouteRepository INSTANCE = new RouteRepository();
  }
}
