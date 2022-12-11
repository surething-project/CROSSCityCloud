package pt.ulisboa.tecnico.cross.utils;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import org.apache.beam.sdk.io.gcp.bigquery.DynamicDestinations;
import org.apache.beam.sdk.io.gcp.bigquery.TableDestination;
import org.apache.beam.sdk.values.ValueInSingleWindow;
import org.apache.beam.vendor.grpc.v1p43p2.com.google.common.collect.ImmutableList;
import pt.ulisboa.tecnico.cross.ClaroOptions;

public class ClaroObsDynamicDestinations extends DynamicDestinations<TableRow, String> {
  private final String project;
  private final String dataset;
  private final String tablePrefix;

  public ClaroObsDynamicDestinations(ClaroOptions options) {
    this.project = options.getProject();
    this.dataset = options.getBigQueryCrossDataset();
    this.tablePrefix = options.getBigQueryObsTablePrefix();
  }

  @Override
  public String getDestination(ValueInSingleWindow<TableRow> elem) {
    return "";
  }

  @Override
  public TableDestination getTable(String destination) {
    return new TableDestination(
            new TableReference()
                    .setProjectId(this.project)
                    .setDatasetId(this.dataset)
                    .setTableId(this.tablePrefix),
            "WiFi AP Observations Table");
  }

  @Override
  public TableSchema getSchema(String destination) {
    return new TableSchema()
            .setFields(
                    ImmutableList.of(
                            new TableFieldSchema()
                                    .setName("id")
                                    .setType("STRING")
                                    .setMode("REQUIRED"),
                            new TableFieldSchema()
                                    .setName("obs_time")
                                    .setType("TIMESTAMP")
                                    .setMode("REQUIRED"),
                            new TableFieldSchema()
                                    .setName("poi_id")
                                    .setType("STRING")
                                    .setMode("REQUIRED"),
                            new TableFieldSchema()
                                    .setName("device")
                                    .setType("STRING")
                                    .setMode("REQUIRED"),
                            new TableFieldSchema()
                                    .setName("signal_type")
                                    .setType("STRING")
                                    .setMode("REQUIRED"),
                            new TableFieldSchema()
                                    .setName("transmitter_id")
                                    .setType("STRING")
                                    .setMode("REQUIRED")));
  }
}
