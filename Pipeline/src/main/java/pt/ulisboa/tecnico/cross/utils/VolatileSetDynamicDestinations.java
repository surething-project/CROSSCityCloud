package pt.ulisboa.tecnico.cross.utils;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import org.apache.beam.sdk.io.gcp.bigquery.DynamicDestinations;
import org.apache.beam.sdk.io.gcp.bigquery.TableDestination;
import org.apache.beam.sdk.values.ValueInSingleWindow;
import org.apache.beam.vendor.grpc.v1p43p2.com.google.common.collect.ImmutableList;
import pt.ulisboa.tecnico.cross.ScavengerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class VolatileSetDynamicDestinations extends DynamicDestinations<TableRow, String> {
  private final String project;
  private final String dataset;
  private final String tablePrefix;

  public VolatileSetDynamicDestinations(ScavengerOptions options) {
    this.project = options.getProject();
    this.dataset = options.getBigQueryCrossDataset();
    this.tablePrefix = options.getBigQueryVolatileSetTablePrefix();
  }

  @Override
  public String getDestination(ValueInSingleWindow<TableRow> elem) {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    return elem.getValue().get("poi_id") + "_" + dateFormat.format(elem.getTimestamp().toDate());
  }

  @Override
  public TableDestination getTable(String destination) {
    return new TableDestination(
            new TableReference()
                    .setProjectId(this.project)
                    .setDatasetId(this.dataset)
                    .setTableId(this.tablePrefix + destination),
            "Volatile Set table for poiId and date " + destination);
  }

  @Override
  public TableSchema getSchema(String destination) {
    return new TableSchema()
            .setFields(
                    ImmutableList.of(
                            new TableFieldSchema()
                                    .setName("poi_id")
                                    .setType("STRING")
                                    .setMode("REQUIRED"),
                            new TableFieldSchema()
                                    .setName("bssid")
                                    .setType("STRING")
                                    .setMode("REQUIRED"),
                            new TableFieldSchema()
                                    .setName("start_time")
                                    .setType("TIMESTAMP")
                                    .setMode("REQUIRED"),
                            new TableFieldSchema()
                                    .setName("end_time")
                                    .setType("TIMESTAMP")
                                    .setMode("REQUIRED"),
                            new TableFieldSchema()
                                    .setName("count")
                                    .setType("INTEGER")
                                    .setMode("REQUIRED")));
  }
}
