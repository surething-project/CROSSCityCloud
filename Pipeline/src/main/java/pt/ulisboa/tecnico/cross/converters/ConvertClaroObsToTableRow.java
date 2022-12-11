package pt.ulisboa.tecnico.cross.converters;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.joda.time.Instant;
import pt.ulisboa.tecnico.cross.scavenger.WiFiAPObservationOuterClass;

public class ConvertClaroObsToTableRow extends DoFn<WiFiAPObservationOuterClass.WiFiAPObservation, TableRow> {
  @ProcessElement
  public void processElement(@Element WiFiAPObservationOuterClass.WiFiAPObservation element, OutputReceiver<TableRow> receiver) {
    String userId = element.getTraveler();
    String obsTime = new Instant(element.getSightingMillis()).toString();
    String poiId = element.getPoiId();
    String device = userId.equals("alice") ? "A" : userId.equals("bob") ? "B" : "C";
    String signalType = "WiFi";
    String transmitterId = element.getBssid();
    String id = poiId + userId + obsTime + transmitterId;

    TableRow row = new TableRow().set("id", id)
            .set("obs_time", obsTime)
            .set("poi_id", poiId)
            .set("device", device)
            .set("signal_type", signalType)
            .set("transmitter_id", transmitterId);

    receiver.output(row);
  }
}
