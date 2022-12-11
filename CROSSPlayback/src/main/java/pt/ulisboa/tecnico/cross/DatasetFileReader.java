package pt.ulisboa.tecnico.cross;

import com.google.protobuf.ByteString;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import pt.ulisboa.tecnico.cross.contract.Evidence;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass;
import pt.ulisboa.tecnico.cross.contract.User;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static com.google.protobuf.util.Timestamps.fromSeconds;

public class DatasetFileReader {
  private final Properties PROPS = new Properties();
  private final RestTemplate restTemplate;
  private final String poiArg;
  private final HttpHeaders headers;
  private String url;
  private long minSecondsDiff;

  public DatasetFileReader(String poiArg, String userId, long minSecondsDiff, CROSSClient.Mode mode) throws IOException {
    PROPS.load(DatasetFileReader.class.getResourceAsStream("/application.properties"));
    this.poiArg = poiArg;
    this.minSecondsDiff = minSecondsDiff;

    this.initializeUrl(mode);

    this.restTemplate = new RestTemplate(List.of(new ProtobufHttpMessageConverter()));
    HttpComponentsClientHttpRequestFactory factory = null;
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      X509Certificate x509Cert =
              (X509Certificate) cf.generateCertificate(DatasetFileReader.class.getResourceAsStream("/CA.crt"));

      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(null);
      ks.setCertificateEntry("CROSS_CA", x509Cert);
      TrustManagerFactory tmf =
              TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ks);

      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, tmf.getTrustManagers(), null);
      SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);

      CloseableHttpClient httpClient = HttpClients.custom()
              .setSSLSocketFactory(socketFactory)
              .build();

      factory = new HttpComponentsClientHttpRequestFactory(httpClient);
    } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException("An error occurred during client creation: " + e.getMessage());
    }
    restTemplate.setRequestFactory(factory);
    this.headers = new HttpHeaders();

    // 128 bits session ID
    byte[] array = new byte[8];
    new Random().nextBytes(array);
    String randomSessionId = new String(array, StandardCharsets.UTF_8);
    User.Credentials credentials = User.Credentials.newBuilder().setUsername(userId).setPassword("123456").setCryptoIdentity(User.CryptoIdentity.newBuilder().setPublicKey(ByteString.EMPTY).setSessionId(randomSessionId).build()).build();
    User.Authorization auth = restTemplate.postForObject(PROPS.getProperty("USER_URL"), credentials, User.Authorization.class);
    this.headers.set("Authorization", auth.getJwt());
  }

  private void initializeUrl(CROSSClient.Mode mode) {
    switch (mode) {
      case VOLATILE:
        this.url = PROPS.getProperty("VOLATILE_URL");
        break;
      default:
        this.url = PROPS.getProperty("TRIP_URL");
    }
  }

  protected void playbackDatasetFile(String pathToDatasetFile) throws IOException {
    File file = new File(pathToDatasetFile);
    FileReader fr = new FileReader(file);
    BufferedReader br = new BufferedReader(fr);
    String line = "";

    TripOuterClass.Trip.Builder tripBuilder = TripOuterClass.Trip.newBuilder().setId("tripId").setRouteId("route_4").setCompleted(false);
    TripOuterClass.Visit.Builder visitBuilder = TripOuterClass.Visit.newBuilder().setId("visitId").setPoiId(poiArg.toLowerCase());

    String unixTimestamp = "";
    boolean gotEntryTime = false;
    boolean gotLeaveTime = false;
    while ((line = br.readLine()) != null) {
      if (line.equals("") && gotLeaveTime) {
        tripBuilder.addVisits(visitBuilder.build());

        TripOuterClass.Trip tripPayload = tripBuilder.build();
        TripOuterClass.CreateOrUpdateTripResponse tripResponse;
        try {
          HttpEntity<TripOuterClass.Trip> entity = new HttpEntity<>(tripPayload, headers);
          tripResponse =
                  restTemplate.postForObject(this.url, entity, TripOuterClass.CreateOrUpdateTripResponse.class);
        } catch (ResponseStatusException e) {
          throw new RuntimeException("An error occurred during trip submission: " + e.getMessage());
        } catch (RestClientException e) {
          System.out.println("Caught RestClientException when submitting trip. " + e.getMessage());
        } finally {
          visitBuilder.clearVisitEvidences();
          tripBuilder.clearVisits();

          gotLeaveTime = false;
          gotEntryTime = false;
        }
      }

      String[] items = line.strip().split(",");
      //System.out.println("Processing: " + line.strip());

      if (items[items.length - 1].equals("\"\"")) {
        unixTimestamp = items[0];
        long seconds = Long.parseLong(unixTimestamp);

        // Build Time
        if (!gotEntryTime) {
          visitBuilder.setEntryTime(fromSeconds(Long.parseLong(unixTimestamp)));
          gotEntryTime = true;
        } else if (gotEntryTime && seconds - visitBuilder.getEntryTime().getSeconds() >= minSecondsDiff) {
          visitBuilder.setLeaveTime(fromSeconds(Long.parseLong(unixTimestamp)));
          gotLeaveTime = true;
        }
      } else if (items[0].startsWith("\"")) {
        String bssid = items[1];
        String ssid = "";
        long sightingMillis = Long.parseLong(unixTimestamp) * 1000;

        String[] ssidSplit = items[0].split("\"");
        if (ssidSplit.length >= 2) {
          ssid = items[0].split("\"")[1];
        }

        Evidence.WiFiAPEvidence wiFiAPEvidence = Evidence.WiFiAPEvidence.newBuilder().setBssid(bssid).setSightingMillis(sightingMillis).setSsid(ssid).build();
        Evidence.VisitEvidence visitEvidence = Evidence.VisitEvidence.newBuilder().setWiFiAPEvidence(wiFiAPEvidence).build();
        visitBuilder.addVisitEvidences(visitEvidence);
      }
    }
  }
}
