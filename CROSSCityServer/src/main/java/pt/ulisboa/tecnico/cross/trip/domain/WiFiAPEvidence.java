package pt.ulisboa.tecnico.cross.trip.domain;

import pt.ulisboa.tecnico.cross.contract.Evidence;

import java.util.Objects;

public class WiFiAPEvidence {

  private final String bssid;
  private final String ssid;
  private final long sightingMillis;

  public WiFiAPEvidence(String bssid, String ssid, long sightingMillis) {
    this.bssid = bssid;
    this.ssid = ssid;
    this.sightingMillis = sightingMillis;
  }

  public String getBssid() {
    return bssid;
  }

  public String getSsid() {
    return ssid;
  }

  public long getSightingMillis() {
    return sightingMillis;
  }

  public static WiFiAPEvidence fromProtobuf(Evidence.WiFiAPEvidence wiFiAPEvidence) {
    return new WiFiAPEvidence(
        wiFiAPEvidence.getBssid(), wiFiAPEvidence.getSsid(), wiFiAPEvidence.getSightingMillis());
  }

  public Evidence.WiFiAPEvidence toProtobuf() {
    return Evidence.WiFiAPEvidence.newBuilder()
        .setBssid(bssid)
        .setSsid(ssid)
        .setSightingMillis(sightingMillis)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WiFiAPEvidence that = (WiFiAPEvidence) o;
    return bssid.equals(that.bssid) && ssid.equals(that.ssid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bssid, ssid);
  }

  @Override
  public String toString() {
    return String.format(
        "WiFiAPEvidence{BSSID='%s', SSID='%s', sightingMillis=%s}", bssid, ssid, sightingMillis);
  }
}
