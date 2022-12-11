package pt.ulisboa.tecnico.cross.wifiap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.wifiap.domain.WiFiAP;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.getDb;

public class WiFiAPService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WiFiAPService.class);
  private final WiFiAPRepository wiFiAPrepository = WiFiAPRepository.get();

  public List<WiFiAP> getTriggerWiFiAPs() {
    try (Connection con = getDb()) {
      List<WiFiAP> wiFiAPList = wiFiAPrepository.getWiFiAPs(con);
      wiFiAPList.removeIf(wiFiAP -> !wiFiAP.getTrigger());
      return wiFiAPList;
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }

  public WiFiAP getTriggerWiFiAP(String bssid) {
    try (Connection con = getDb()) {
      WiFiAP wiFiAP = wiFiAPrepository.getWiFiAP(con, bssid);
      if (!wiFiAP.getTrigger()) {
        throw new APIException(
            NOT_FOUND,
            String.format("Wi-Fi AP with BSSID %s which is a trigger was not found.", bssid));
      }
      return wiFiAP;
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }

  public boolean hasWiFiAP(String bssid) {
    try (Connection con = getDb()) {
      return wiFiAPrepository.hasWiFiAP(con, bssid);
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }
}
