package pt.ulisboa.tecnico.cross.route;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.route.domain.Route;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.getDb;

@ApplicationScoped
public class RouteService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouteService.class);
  private final RouteRepository routeRepository = RouteRepository.get();

  public List<Route> getRoutes() {
    try (Connection con = getDb()) {
      return routeRepository.getRoutes(con);
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }

  public Route getRoute(String id) {
    try (Connection con = getDb()) {
      return routeRepository.getRoute(con, id);
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }
}
