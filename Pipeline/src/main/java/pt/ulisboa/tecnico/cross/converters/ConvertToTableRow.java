package pt.ulisboa.tecnico.cross.converters;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.apache.beam.sdk.values.KV;

public class ConvertToTableRow extends DoFn<KV<String, Long>, TableRow> {
  @ProcessElement
  public void processElement(ProcessContext c, IntervalWindow window) {
    String[] poiIdBssid = c.element().getKey().split(",");
    TableRow row = new TableRow().set("poi_id", poiIdBssid[0])
            .set("bssid", poiIdBssid[1])
            .set("start_time", window.start().toString())
            .set("end_time", window.end().toString())
            .set("count", c.element().getValue());

    c.output(row);
  }
}
