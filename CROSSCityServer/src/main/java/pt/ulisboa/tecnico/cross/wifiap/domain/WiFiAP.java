package pt.ulisboa.tecnico.cross.wifiap.domain;

import pt.ulisboa.tecnico.cross.contract.WiFiAPOuterClass;

public class WiFiAP {

  private final String bssid;
  private final WiFiAPType type;
  private final boolean trigger;
  private final String totpSecret;
  private final String totpRegexp;

  public WiFiAP(
      String bssid, WiFiAPType type, boolean trigger, String totpSecret, String totpRegexp) {
    this.bssid = bssid;
    this.type = type;
    this.trigger = trigger;
    this.totpSecret = totpSecret;
    this.totpRegexp = totpRegexp;
  }

  public String getBssid() {
    return bssid;
  }

  public WiFiAPType getType() {
    return type;
  }

  public boolean getTrigger() {
    return trigger;
  }

  public String getTOTPSecret() {
    return totpSecret;
  }

  public String getTOTPRegexp() {
    return totpRegexp;
  }

  public WiFiAPOuterClass.WiFiAP toProtobuf() {
    return WiFiAPOuterClass.WiFiAP.newBuilder()
        .setBssid(bssid)
        .setType(type.name().toLowerCase())
        .build();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof WiFiAP && bssid.equals(((WiFiAP) o).bssid);
  }

  @Override
  public int hashCode() {
    return bssid.hashCode();
  }

  public enum WiFiAPType {
    UNTRUSTED,
    TOTP
  }
}
