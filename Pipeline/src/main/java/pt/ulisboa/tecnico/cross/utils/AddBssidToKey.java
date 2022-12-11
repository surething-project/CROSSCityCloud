package pt.ulisboa.tecnico.cross.utils;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import pt.ulisboa.tecnico.cross.WifiAPObservation;

public class AddBssidToKey extends DoFn<KV<String, WifiAPObservation>, KV<String, WifiAPObservation>> {
  @ProcessElement
  public void processElement(@Element KV<String, WifiAPObservation> element, OutputReceiver<KV<String, WifiAPObservation>> receiver) {
    receiver.output(KV.of(element.getKey() + "," + element.getValue().getBssid(), element.getValue()));
  }
}
