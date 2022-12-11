package pt.ulisboa.tecnico.cross;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import pt.ulisboa.tecnico.cross.converters.ConvertClaroObsToTableRow;
import pt.ulisboa.tecnico.cross.scavenger.WiFiAPObservationOuterClass;
import pt.ulisboa.tecnico.cross.utils.ClaroObsDynamicDestinations;

import java.io.IOException;

public class ClaroRunner {

  public static void main(String[] args) throws IOException {
    ClaroOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
            .as(ClaroOptions.class);
    // BigQueryOptions - Deterministic record ids and STORAGE_WRITE_API methods are needed to achieve exactly-once semantics (deterministic sink)
    options.setUseStorageWriteApi(true);
    options.setStorageWriteApiTriggeringFrequencySec(30);
    options.setNumStorageWriteApiStreams(2);

    Pipeline pipeline = Pipeline.create(options);

    PCollection<WiFiAPObservationOuterClass.WiFiAPObservation> pubSubMessages = pipeline.apply("Read WiFi AP Obs Proto from Pub/Sub",
            PubsubIO.readProtos(WiFiAPObservationOuterClass.WiFiAPObservation.class)
                    .fromTopic(String.format("projects/%s/topics/%s", options.getProject(), options.getPubSubTopic()))
                    .withIdAttribute("obsId")
                    .withTimestampAttribute("sightingMillis")
    );

    PCollection<TableRow> tableRows = pubSubMessages.apply("Parse WiFi AP Obs Proto and Convert to Table Row", ParDo.of(new ConvertClaroObsToTableRow()));

    tableRows.apply(
            "Write Observation TableRows to BigQuery",
            BigQueryIO.writeTableRows()
                    .to(new ClaroObsDynamicDestinations(options))
                    .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                    .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));

    pipeline.run().waitUntilFinish();
  }
}
