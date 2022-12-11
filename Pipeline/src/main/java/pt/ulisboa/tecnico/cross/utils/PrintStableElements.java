package pt.ulisboa.tecnico.cross.utils;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.apache.beam.sdk.values.KV;
import org.joda.time.DateTimeFieldType;

import java.util.Map;

public class PrintStableElements extends DoFn<KV<String, Map<String, Integer>>, KV<String, Map<String, Integer>>> {
  @ProcessElement
  public void processElement(ProcessContext c, IntervalWindow window) {
    System.out.println("STABLE SET FOR WINDOW HOUR: " + window.start().get(DateTimeFieldType.hourOfDay()));
    System.out.println(c.element().toString());
    c.output(c.element());
  }
}
