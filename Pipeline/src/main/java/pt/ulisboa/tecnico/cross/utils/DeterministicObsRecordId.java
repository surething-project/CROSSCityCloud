package pt.ulisboa.tecnico.cross.utils;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.SerializableFunction;

import java.util.Objects;

public class DeterministicObsRecordId implements SerializableFunction<TableRow, String> {
  @Override
  public String apply(TableRow input) {
    return String.valueOf(Objects.hash(input.get("poi_id"), input.get("start_time"), input.get("end_time"), input.get("count")));
  }
}
