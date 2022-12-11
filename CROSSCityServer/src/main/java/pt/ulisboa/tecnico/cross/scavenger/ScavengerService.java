package pt.ulisboa.tecnico.cross.scavenger;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import pt.ulisboa.tecnico.cross.trip.domain.Visit;
import pt.ulisboa.tecnico.cross.trip.domain.WiFiAPEvidence;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.PROPS;

@ApplicationScoped
public class ScavengerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScavengerService.class);

  private final ScavengerRepository scavengerRepository;

  private final TopicName topicName;
  private final RetrySettings retrySettings;
  private final long requestBytesThreshold = 5000; // 5000 Bytes
  private final Duration publishDelayThreshold = Duration.ofMillis(100); // 100 ms
  private final Duration initialRetryDelay = Duration.ofMillis(100); // 100 ms
  private final double retryDelayMultiplier = 2.0;
  private final Duration maxRetryDelay = Duration.ofSeconds(60); // 60 s
  private final Duration initialRpcTimeout = Duration.ofSeconds(1); // 1 s
  private final double rpcTimeoutMultiplier = 1.0;
  private final Duration maxRpcTimeout = Duration.ofSeconds(600); // 600 s
  private final Duration totalTimeout = Duration.ofSeconds(600); // 600 s

  public ScavengerService() throws IOException {
    Properties crossProps = new Properties();
    crossProps.load(ScavengerService.class.getResourceAsStream("/CROSSCityServer.properties"));

    if (Boolean.parseBoolean(crossProps.getProperty("SCAVENGER"))) {
      this.scavengerRepository = ScavengerRepository.get();
      this.topicName =
          TopicName.of(PROPS.getProperty("PROJECT_ID"), PROPS.getProperty("WIFI_AP_OBS_TOPIC_ID"));
      this.retrySettings =
          RetrySettings.newBuilder()
              .setInitialRetryDelay(initialRetryDelay)
              .setRetryDelayMultiplier(retryDelayMultiplier)
              .setMaxRetryDelay(maxRetryDelay)
              .setInitialRpcTimeout(initialRpcTimeout)
              .setRpcTimeoutMultiplier(rpcTimeoutMultiplier)
              .setMaxRpcTimeout(maxRpcTimeout)
              .setTotalTimeout(totalTimeout)
              .build();
    } else {
      this.scavengerRepository = null;
      this.topicName = null;
      this.retrySettings = null;
    }
  }

  private String getWiFiAPObservationUniqueId(
      MessageDigest md, String poiId, String traveler, WiFiAPEvidence evidence) {
    byte[] digest =
        md.digest(
            (poiId + traveler + evidence.getBssid() + evidence.getSightingMillis()).getBytes());
    return Base64.getEncoder().encodeToString(digest);
  }

  public void publishWiFiAPObservations(
      String poiId, String traveler, List<WiFiAPEvidence> evidences, boolean batch)
      throws ExecutionException, InterruptedException, IOException, NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    WiFiAPObservationOuterClass.WiFiAPObservation.Builder obsBuilder =
        WiFiAPObservationOuterClass.WiFiAPObservation.newBuilder()
            .setPoiId(poiId)
            .setTraveler(traveler);
    Publisher publisher = null;
    List<ApiFuture<String>> messageIdFutures = new ArrayList<>();

    try {
      long messageCountBatchSize = evidences.size();
      BatchingSettings batchingSettings =
          batch
              ? BatchingSettings.newBuilder()
                  .setElementCountThreshold(messageCountBatchSize)
                  .setRequestByteThreshold(requestBytesThreshold)
                  .setDelayThreshold(publishDelayThreshold)
                  .build()
              : BatchingSettings.newBuilder().build();
      publisher = Publisher.newBuilder(topicName).setBatchingSettings(batchingSettings).build();

      for (WiFiAPEvidence evidence : evidences) {
        WiFiAPObservationOuterClass.WiFiAPObservation obs =
            obsBuilder
                .setBssid(evidence.getBssid())
                .setSightingMillis(evidence.getSightingMillis())
                .build();
        String uniqueId = getWiFiAPObservationUniqueId(md, poiId, traveler, evidence);
        PubsubMessage.Builder message =
            PubsubMessage.newBuilder()
                .putAttributes("obsId", uniqueId)
                .putAttributes("sightingMillis", String.valueOf(obs.getSightingMillis()))
                .setPublishTime(fromMillis(obs.getSightingMillis()))
                .setData(obs.toByteString());

        ApiFuture<String> messageIdFuture = publisher.publish(message.build());
        messageIdFutures.add(messageIdFuture);
      }
    } finally {
      List<String> messageIds = ApiFutures.allAsList(messageIdFutures).get();

      LOGGER.info("Published " + messageIds.size() + " messages with batch settings.");

      if (publisher != null) {
        publisher.shutdown();
        publisher.awaitTermination(1, TimeUnit.MINUTES);
      }
    }
  }

  public void testStableSetForVisit(Visit visit, double matchThreshold) {
    int matches = 0;
    Set<String> evidenceBssids =
        visit.getWiFiAPEvidences().stream()
            .map(WiFiAPEvidence::getBssid)
            .collect(Collectors.toSet());

    LOGGER.info("Testing Stable Set for Visit with ID: " + visit.getId());

    Set<String> stableSet = scavengerRepository.getStableSetForVisit(visit);
    for (String evidenceBssid : evidenceBssids) {
      if (stableSet.contains(evidenceBssid)) {
        LOGGER.info("Match in Stable Set with BSSID: " + evidenceBssid);
        matches++;
      }
    }

    double matchPercentage = ((double) matches / stableSet.size()) * 100.0;

    LOGGER.info(
        "Number of matches with Stable Set: "
            + matches
            + "\nPercentage of match: "
            + matchPercentage
            + "%");

    if (matchPercentage >= matchThreshold) {
      LOGGER.info("Successful match above set threshold of " + matchThreshold + "%");
    }
  }

  public void testVolatileSetForVisit(Visit visit, double matchThreshold) {
    // 15, 10 and 5 minute deltas (5 min delta creates a 10-min span)
    long[] deltas = {900, 600, 300};
    Set<String> evidenceBssids =
        visit.getWiFiAPEvidences().stream()
            .map(WiFiAPEvidence::getBssid)
            .collect(Collectors.toSet());

    LOGGER.info("Testing Volatile Set for Visit with ID: " + visit.getId());

    for (long delta : deltas) {
      int matches = 0;
      LOGGER.info(delta / 60 + " min delta");

      // min 1 observation per AP
      Set<String> volatileSet = scavengerRepository.getVolatileSetForVisit(visit, delta, 1);
      for (String evidenceBssid : evidenceBssids) {
        if (volatileSet.contains(evidenceBssid)) {
          LOGGER.info("Match in Volatile Set with BSSID: " + evidenceBssid);
          matches++;
        }
      }

      double matchPercentage = ((double) matches / volatileSet.size()) * 100.0;

      LOGGER.info(
          "Number of matches with Volatile Set: "
              + matches
              + "\nPercentage of match: "
              + matchPercentage
              + "%");

      if (matchPercentage >= matchThreshold) {
        LOGGER.info("Successful match above set threshold of " + matchThreshold + "%");
      }
    }
  }

  public void createStableSetTable(String poiId, Instant epochLowerBound, Instant epochUpperBound) {
    scavengerRepository.createStableSetTable(poiId, epochLowerBound, epochUpperBound);
  }
}
