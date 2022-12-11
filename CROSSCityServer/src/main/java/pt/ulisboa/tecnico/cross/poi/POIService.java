package pt.ulisboa.tecnico.cross.poi;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.poi.domain.POI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.getDb;

@ApplicationScoped
public class POIService {

  private static final Logger LOGGER = LoggerFactory.getLogger(POIService.class);
  private final POIRepository poiRepository = POIRepository.get();

  public List<POI> getPOIs() {
    try (Connection con = getDb()) {
      List<POI> pois = poiRepository.getPOIs(con);
      for (POI poi : pois) poi.filterUntriggeredWiFiAP();
      return pois;
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }

  public POI getPOI(String id) {
    try (Connection con = getDb()) {
      POI poi = poiRepository.getPOI(con, id);
      poi.filterUntriggeredWiFiAP();
      return poi;
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }
}
