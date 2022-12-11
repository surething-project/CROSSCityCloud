package pt.ulisboa.tecnico.cross.utils;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.apache.beam.sdk.values.KV;
import org.joda.time.DateTimeFieldType;

import java.util.Map;

public class PrintVolatileElements extends DoFn<KV<String, Map<String, Integer>>, KV<String, Map<String, Integer>>> {
  @ProcessElement
  public void processElement(ProcessContext c, IntervalWindow window) {
    System.out.println("WINDOW START: " + window.start().get(DateTimeFieldType.hourOfDay()) + ":" + window.start().get(DateTimeFieldType.minuteOfHour()));
    System.out.println("WINDOW END: " + window.end().get(DateTimeFieldType.hourOfDay()) + ":" + window.end().get(DateTimeFieldType.minuteOfHour()));
    System.out.println(c.element().toString());
    c.output(c.element());
  }
}
