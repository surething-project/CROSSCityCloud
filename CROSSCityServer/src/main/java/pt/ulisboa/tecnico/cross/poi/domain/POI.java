package pt.ulisboa.tecnico.cross.poi.domain;

import com.grum.geocalc.Coordinate;
import com.grum.geocalc.Point;
import pt.ulisboa.tecnico.cross.contract.POIOuterClass;
import pt.ulisboa.tecnico.cross.wifiap.domain.WiFiAP;

import java.util.HashMap;
import java.util.Map;

public class POI {

  private final String id;
  private final Point location;
  private final String webUrl;
  private final String imageUrl;
  private final String mainLocale;
  private final Map<String, String> names;
  private final Map<String, String> descriptions;
  private final Map<String, WiFiAP> wiFiAPs;

  public POI(
      String id,
      double latitude,
      double longitude,
      String webUrl,
      String imageUrl,
      String mainLocale) {
    this.id = id;
    this.location = Point.at(Coordinate.fromDegrees(latitude), Coordinate.fromDegrees(longitude));
    this.webUrl = webUrl;
    this.imageUrl = imageUrl;
    this.mainLocale = mainLocale;
    this.names = new HashMap<>();
    this.descriptions = new HashMap<>();
    this.wiFiAPs = new HashMap<>();
  }

  public String getId() {
    return id;
  }

  public Point getLocation() {
    return location;
  }

  public String getWebUrl() {
    return webUrl;
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

  public Map<String, WiFiAP> getWiFiAPs() {
    return wiFiAPs;
  }

  public void putName(String locale, String name) {
    names.put(locale, name);
  }

  public void putDescription(String locale, String description) {
    descriptions.put(locale, description);
  }

  public void putWiFiAP(String bssid, WiFiAP wiFiAP) {
    wiFiAPs.put(bssid, wiFiAP);
  }

  public void filterUntriggeredWiFiAP() {
    wiFiAPs.entrySet().removeIf(wiFiAP -> !wiFiAP.getValue().getTrigger());
  }

  public POIOuterClass.POI toProtobuf() {
    return POIOuterClass.POI
        .newBuilder()
        .setId(id)
        .addWorldCoord(location.latitude)
        .addWorldCoord(location.longitude)
        .setWebURL(webUrl != null ? webUrl : "")
        .setImageURL(imageUrl)
        .setMainLocale(mainLocale)
        .putAllNames(names)
        .putAllDescriptions(descriptions)
        .build();
  }
}
