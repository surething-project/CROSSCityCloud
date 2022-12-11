package pt.ulisboa.tecnico.cross;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;
import pt.ulisboa.tecnico.cross.converters.ConvertToTableRow;
import pt.ulisboa.tecnico.cross.converters.ParseProtoFromPubSub;
import pt.ulisboa.tecnico.cross.scavenger.WiFiAPObservationOuterClass;
import pt.ulisboa.tecnico.cross.utils.StableSetDynamicDestinations;
import pt.ulisboa.tecnico.cross.utils.VolatileSetDynamicDestinations;

import java.io.IOException;

public class WifiRunner {

  public static void main(String[] args) throws IOException {
    ScavengerOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
            .as(ScavengerOptions.class);
    // BigQueryOptions - Deterministic record ids and STORAGE_WRITE_API methods are needed to achieve exactly-once semantics (deterministic sink)
    options.setUseStorageWriteApi(true);
    options.setStorageWriteApiTriggeringFrequencySec(30);
    options.setNumStorageWriteApiStreams(2);

    boolean isRealTimeProcessing = options.getIsRealTime();
    long stableSetWindowSeconds = options.getStableSetWindowSeconds();
    long volatileSetWindowSeconds = options.getVolatileSetWindowSeconds();

    Pipeline pipeline = Pipeline.create(options);

    PCollection<WiFiAPObservationOuterClass.WiFiAPObservation> pubSubMessages = pipeline.apply("Read WiFi AP Obs Proto from Pub/Sub",
            PubsubIO.readProtos(WiFiAPObservationOuterClass.WiFiAPObservation.class)
                    .fromTopic(String.format("projects/%s/topics/%s", options.getProject(), options.getPubSubTopic()))
                    .withIdAttribute("obsId")
                    .withTimestampAttribute("sightingMillis")
    );

    PCollection<KV<String, WifiAPObservation>> keyValueTimestampedObs = pubSubMessages.apply("Parse WiFi AP Obs Proto", ParDo.of(new ParseProtoFromPubSub()));

    PCollection<KV<String, WifiAPObservation>> windowedVolatileObs;
    PCollection<KV<String, WifiAPObservation>> windowedStableObs;
    if (isRealTimeProcessing) {
      // 5 minute (default) fixed windows, triggering after the watermark passes the end of the window and any time late data arrives after a 1 minute delay, discarding previous panes, max 1 minute late data
      windowedVolatileObs = keyValueTimestampedObs.apply("Window Volatile Set Obs", Window.<KV<String, WifiAPObservation>>into(FixedWindows.of(Duration.standardSeconds(volatileSetWindowSeconds)))
              .triggering(AfterWatermark.pastEndOfWindow().withLateFirings(AfterProcessingTime.pastFirstElementInPane().plusDelayOf(Duration.standardMinutes(1))))
              .discardingFiredPanes()
              .withAllowedLateness(Duration.standardMinutes(1))
      );

      // 1 day (default) fixed windows, triggering after the watermark passes the end of the window and any time late data arrives after a 1 minute delay, discarding previous panes, max 1 minute late data
      windowedStableObs = keyValueTimestampedObs.apply("Window Stable Set Obs", Window.<KV<String, WifiAPObservation>>into(FixedWindows.of(Duration.standardSeconds(stableSetWindowSeconds)))
              .triggering(AfterWatermark.pastEndOfWindow().withLateFirings(AfterProcessingTime.pastFirstElementInPane().plusDelayOf(Duration.standardMinutes(1))))
              .discardingFiredPanes()
              .withAllowedLateness(Duration.standardMinutes(1))
      );
    } else {
      // 5 minute (default) fixed windows, triggering periodic 40 second updates discarding previous panes
      windowedVolatileObs = keyValueTimestampedObs.apply("Window Volatile Set Obs", Window.<KV<String, WifiAPObservation>>into(FixedWindows.of(Duration.standardSeconds(volatileSetWindowSeconds)))
              .triggering(Repeatedly.forever(AfterProcessingTime.pastFirstElementInPane().plusDelayOf(Duration.standardSeconds(40))))
              .discardingFiredPanes()
              .withAllowedLateness(Duration.standardMinutes(10))
      );

      // 1 day (default) fixed windows, triggering periodic 1 minute updates discarding previous panes
      windowedStableObs = keyValueTimestampedObs.apply("Window Stable Set Obs", Window.<KV<String, WifiAPObservation>>into(FixedWindows.of(Duration.standardSeconds(stableSetWindowSeconds)))
              .triggering(Repeatedly.forever(AfterProcessingTime.pastFirstElementInPane().plusDelayOf(Duration.standardMinutes(1))))
              .discardingFiredPanes()
              .withAllowedLateness(Duration.standardMinutes(10))
      );
    }

    PCollection<KV<String, Long>> sumOfVolatileObs = windowedVolatileObs.setCoder(KvCoder.of(StringUtf8Coder.of(), SerializableCoder.of(WifiAPObservation.class))).apply("Sum Volatile Obs per POI and BSSID", Count.perKey());

    PCollection<TableRow> tableRowsVolatile = sumOfVolatileObs.apply("Convert Volatile Obs to Table Row", ParDo.of(new ConvertToTableRow()));

    tableRowsVolatile.apply(
            "Write Volatile Set Intermediate Results to BigQuery",
            BigQueryIO.writeTableRows()
                    .to(new VolatileSetDynamicDestinations(options))
                    .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                    .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));

    PCollection<KV<String, Long>> sumOfStableObs = windowedStableObs.setCoder(KvCoder.of(StringUtf8Coder.of(), SerializableCoder.of(WifiAPObservation.class))).apply("Sum Stable Obs per POI and BSSID", Count.perKey());

    PCollection<TableRow> tableRowsStable = sumOfStableObs.apply("Convert Stable Obs to Table Row", ParDo.of(new ConvertToTableRow()));

    tableRowsStable.apply(
            "Write Stable Set Intermediate Results to BigQuery",
            BigQueryIO.writeTableRows()
                    .to(new StableSetDynamicDestinations(options))
                    .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                    .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));

    pipeline.run().waitUntilFinish();
  }
}
