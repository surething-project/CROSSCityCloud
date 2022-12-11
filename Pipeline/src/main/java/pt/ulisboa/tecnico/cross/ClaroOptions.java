package pt.ulisboa.tecnico.cross;

import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryOptions;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.Validation;

public interface ClaroOptions extends GcpOptions, BigQueryOptions {
  @Description("BigQuery CROSS WiFi observations dataset name")
  @Validation.Required
  String getBigQueryCrossDataset();
  void setBigQueryCrossDataset(String dataset);

  @Description("BigQuery WiFi AP Obs table prefix")
  @Validation.Required
  String getBigQueryObsTablePrefix();
  void setBigQueryObsTablePrefix(String tablePrefix);

  @Description("PubSub CROSS WiFi observations topic name")
  @Validation.Required
  String getPubSubTopic();
  void setPubSubTopic(String topic);
}
