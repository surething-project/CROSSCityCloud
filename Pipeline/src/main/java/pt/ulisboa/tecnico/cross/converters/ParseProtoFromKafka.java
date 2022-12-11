package pt.ulisboa.tecnico.cross.converters;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.beam.sdk.io.kafka.KafkaRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.joda.time.Instant;
import pt.ulisboa.tecnico.cross.WifiAPObservation;
import pt.ulisboa.tecnico.cross.scavenger.WiFiAPObservationOuterClass;

public class ParseProtoFromKafka extends DoFn<KafkaRecord<String, byte[]>, KV<String, WifiAPObservation>>  {
  @ProcessElement
  public void processElement(@Element KafkaRecord<String, byte[]> element, OutputReceiver<KV<String, WifiAPObservation>> receiver) throws InvalidProtocolBufferException {
    WiFiAPObservationOuterClass.WiFiAPObservation obsMessage = WiFiAPObservationOuterClass.WiFiAPObservation.parseFrom(element.getKV().getValue());
    WifiAPObservation obs = new WifiAPObservation(
            obsMessage.getTraveler(),
            obsMessage.getPoiId(),
            obsMessage.getBssid(),
            obsMessage.getSightingMillis()
    );

    Instant instant = new Instant(obs.ts);

    receiver.outputWithTimestamp(KV.of(obs.poi, obs), instant);
  }
}
