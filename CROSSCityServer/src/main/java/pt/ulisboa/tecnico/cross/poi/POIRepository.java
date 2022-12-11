package pt.ulisboa.tecnico.cross.poi;

import org.postgresql.geometric.PGpoint;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.poi.domain.POI;
import pt.ulisboa.tecnico.cross.wifiap.domain.WiFiAP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

public class POIRepository {

  public static POIRepository get() {
    return POIRepositoryHolder.INSTANCE;
  }

  private POIRepository() {}

  private POI createPOIFromResultSet(ResultSet rs) throws SQLException {
    PGpoint location = (PGpoint) rs.getObject("world_coord");
    POI poi =
        new POI(
            rs.getString("id"),
            location.x,
            location.y,
            rs.getString("web_url"),
            rs.getString("image_url"),
            rs.getString("main_locale"));
    while (!rs.isAfterLast() && poi.getId().equals(rs.getString("id"))) {
      String locale = rs.getString("lang");
      String wiFiAPBSSID = rs.getString("wifiap_bssid");

      if (!poi.getNames().containsKey(locale)) {
        poi.putName(locale, rs.getString("name"));
      }
      if (!poi.getDescriptions().containsKey(locale)) {
        poi.putDescription(locale, rs.getString("description"));
      }
      if (wiFiAPBSSID != null && !poi.getWiFiAPs().containsKey(wiFiAPBSSID)) {
        poi.putWiFiAP(
            wiFiAPBSSID,
            new WiFiAP(
                wiFiAPBSSID,
                WiFiAP.WiFiAPType.valueOf(rs.getString("wifiap_type").toUpperCase()),
                rs.getBoolean("trigger"),
                rs.getString("totp_secret"),
                rs.getString("totp_regexp")));
      }
      rs.next();
    }
    return poi;
  }

  public POI getPOI(Connection con, String id) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT p.*, pl.lang, pl.name, pl.description, pw.wifiap_bssid, w.*, w.type AS wifiap_type "
                + "FROM poi p "
                + "INNER JOIN poi_lang pl ON p.id = pl.poi_id "
                + "LEFT JOIN poi_wifiap pw ON p.id = pw.poi_id "
                + "LEFT JOIN wifiap w ON pw.wifiap_bssid = w.bssid "
                + "WHERE p.id = ?");
    stmt.setString(1, id);
    ResultSet rs = stmt.executeQuery();
    if (!rs.next()) {
      throw new APIException(NOT_FOUND, String.format("POI with ID %s not found.", id));
    }
    POI poi = createPOIFromResultSet(rs);
    stmt.close();
    return poi;
  }

  public List<POI> getPOIs(Connection con) throws SQLException {
    List<POI> pois = new ArrayList<>();
    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT p.*, pl.lang, pl.name, pl.description, pw.wifiap_bssid, w.*, w.type AS wifiap_type "
                + "FROM poi p "
                + "INNER JOIN poi_lang pl ON p.id = pl.poi_id "
                + "LEFT JOIN poi_wifiap pw ON p.id = pw.poi_id "
                + "LEFT JOIN wifiap w ON pw.wifiap_bssid = w.bssid "
                + "ORDER BY p.id");

    ResultSet rs = stmt.executeQuery();
    rs.next();
    while (!rs.isAfterLast()) pois.add(createPOIFromResultSet(rs));
    stmt.close();
    return pois;
  }

  private static class POIRepositoryHolder {
    private static final POIRepository INSTANCE = new POIRepository();
  }
}
