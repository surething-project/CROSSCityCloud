package pt.ulisboa.tecnico.cross;

import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryOptions;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.Validation.Required;

public interface ScavengerOptions extends GcpOptions, BigQueryOptions {
  @Description("CROSS Scavenger pipeline execution in real time or historic processing")
  @Required
  boolean getIsRealTime();
  void setIsRealTime(boolean isRealTime);

  // Default value: 1 day
  @Description("Stable set intermediate result window seconds size")
  @Default.Long(86400)
  long getStableSetWindowSeconds();
  void setStableSetWindowSeconds(long stableSetWindowSeconds);

  // Default value: 5 minutes
  @Description("Volatile set intermediate result window seconds size")
  @Default.Long(300)
  long getVolatileSetWindowSeconds();
  void setVolatileSetWindowSeconds(long volatileSetWindowSeconds);

  @Description("BigQuery CROSS WiFi observations dataset name")
  @Required
  String getBigQueryCrossDataset();
  void setBigQueryCrossDataset(String dataset);

  @Description("BigQuery stable set table prefix")
  @Required
  String getBigQueryStableSetTablePrefix();
  void setBigQueryStableSetTablePrefix(String tablePrefix);

  @Description("BigQuery volatile set table prefix")
  @Required
  String getBigQueryVolatileSetTablePrefix();
  void setBigQueryVolatileSetTablePrefix(String tablePrefix);

  @Description("PubSub CROSS WiFi observations topic name")
  @Required
  String getPubSubTopic();
  void setPubSubTopic(String topic);
}
