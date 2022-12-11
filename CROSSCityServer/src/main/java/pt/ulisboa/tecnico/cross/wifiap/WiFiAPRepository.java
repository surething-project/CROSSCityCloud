package pt.ulisboa.tecnico.cross.wifiap;

import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.wifiap.domain.WiFiAP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

public class WiFiAPRepository {

  public static WiFiAPRepository get() {
    return WiFiAPRepositoryHolder.INSTANCE;
  }

  private WiFiAPRepository() {}

  private WiFiAP createWiFiAPFromResultSet(ResultSet rs) throws SQLException {
    return new WiFiAP(
        rs.getString("bssid"),
        WiFiAP.WiFiAPType.valueOf(rs.getString("type").toUpperCase()),
        rs.getBoolean("trigger"),
        rs.getString("totp_secret"),
        rs.getString("totp_regexp"));
  }

  public WiFiAP getWiFiAP(Connection con, String bssid) throws SQLException {
    PreparedStatement stmt = con.prepareStatement("SELECT * FROM wifiap WHERE bssid = ?");
    stmt.setString(1, bssid);
    ResultSet rs = stmt.executeQuery();
    if (!rs.next()) {
      throw new APIException(NOT_FOUND, String.format("Wi-Fi AP with BSSID %s not found.", bssid));
    }
    WiFiAP wiFiAP = createWiFiAPFromResultSet(rs);
    stmt.close();
    return wiFiAP;
  }

  public List<WiFiAP> getWiFiAPs(Connection con) throws SQLException {
    List<WiFiAP> wiFiAPs = new ArrayList<>();
    PreparedStatement stmt = con.prepareStatement("SELECT * FROM wifiap");
    ResultSet rs = stmt.executeQuery();

    while (rs.next()) wiFiAPs.add(createWiFiAPFromResultSet(rs));
    stmt.close();
    return wiFiAPs;
  }

  public boolean hasWiFiAP(Connection con, String bssid) throws SQLException {
    PreparedStatement stmt = con.prepareStatement("SELECT * FROM wifiap WHERE bssid = ?");
    stmt.setString(1, bssid);
    boolean hasWiFiAP = stmt.executeQuery().next();
    stmt.close();
    return hasWiFiAP;
  }

  private static class WiFiAPRepositoryHolder {
    private static final WiFiAPRepository INSTANCE = new WiFiAPRepository();
  }
}
