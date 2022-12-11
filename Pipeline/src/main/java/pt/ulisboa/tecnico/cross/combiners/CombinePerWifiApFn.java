package pt.ulisboa.tecnico.cross.combiners;

import org.apache.beam.sdk.transforms.Combine;
import pt.ulisboa.tecnico.cross.WifiAPObservation;

import java.util.HashMap;
import java.util.Map;

public class CombinePerWifiApFn extends Combine.CombineFn<WifiAPObservation, Map<String, Integer>, Map<String, Integer>> {
  @Override
  public Map<String, Integer> createAccumulator() {
    return new HashMap<>();
  }

  @Override
  public Map<String, Integer> addInput(Map<String, Integer> mutableAccumulator, WifiAPObservation input) {
    String bssid = input.bssid;

    mutableAccumulator.putIfAbsent(bssid, 0);
    int currentSumOfObs = mutableAccumulator.get(bssid);
    mutableAccumulator.put(bssid, currentSumOfObs + 1);
    return mutableAccumulator;
  }

  @Override
  public Map<String, Integer> mergeAccumulators(Iterable<Map<String, Integer>> accumulators) {
    Map<String, Integer> merged = new HashMap<>();

    for (Map<String, Integer> accumulator : accumulators) {
      for (Map.Entry<String, Integer> entry : accumulator.entrySet()) {
        String bssid = entry.getKey();
        Integer numOfObs = entry.getValue();

        merged.putIfAbsent(bssid, 0);
        int currentSumOfObs = merged.get(bssid);
        merged.put(bssid, currentSumOfObs + numOfObs);
      }
    }

    return merged;
  }

  @Override
  public Map<String, Integer> extractOutput(Map<String, Integer> accumulator) {
    return accumulator;
  }
}
