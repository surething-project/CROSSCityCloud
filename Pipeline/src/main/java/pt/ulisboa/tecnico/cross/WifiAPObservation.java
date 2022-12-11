package pt.ulisboa.tecnico.cross;

import java.io.Serializable;
import java.util.Objects;

public class WifiAPObservation implements Serializable {
    public final String userId;
    public final String poi;
    public final String bssid;
    public final long ts;

    public WifiAPObservation(String userId, String poi, String bssid, long ts) {
        this.userId = userId;
        this.poi = poi;
        this.bssid = bssid;
        this.ts = ts;
    }

    public String getBssid() {
        return bssid;
    }

    public String getUserIdPoiBssid() {
        return userId + poi + bssid;
    }

    @Override
    public String toString() {
        return "WifiObservation{" +
                "userId='" + userId + '\'' +
                ", poi='" + poi + '\'' +
                ", bssid='" + bssid + '\'' +
                ", ts=" + ts +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WifiAPObservation that = (WifiAPObservation) o;
        return ts == that.ts &&
                userId.equals(that.userId) &&
                poi.equals(that.poi) &&
                bssid.equals(that.bssid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, poi, bssid, ts);
    }
}
