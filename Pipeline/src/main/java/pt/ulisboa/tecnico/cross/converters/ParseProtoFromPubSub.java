package pt.ulisboa.tecnico.cross.converters;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.joda.time.Instant;
import pt.ulisboa.tecnico.cross.WifiAPObservation;
import pt.ulisboa.tecnico.cross.scavenger.WiFiAPObservationOuterClass;

public class ParseProtoFromPubSub extends DoFn<WiFiAPObservationOuterClass.WiFiAPObservation, KV<String, WifiAPObservation>> {
  @ProcessElement
  public void processElement(@Element WiFiAPObservationOuterClass.WiFiAPObservation element, OutputReceiver<KV<String, WifiAPObservation>> receiver) throws InvalidProtocolBufferException {
    WifiAPObservation obs = new WifiAPObservation(
            element.getTraveler(),
            element.getPoiId(),
            element.getBssid(),
            element.getSightingMillis()
    );

    Instant instant = new Instant(obs.ts);

    receiver.outputWithTimestamp(KV.of(obs.poi + "," + obs.bssid, obs), instant);
  }
}
