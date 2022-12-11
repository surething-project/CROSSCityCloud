package pt.ulisboa.tecnico.cross;

import com.google.protobuf.ByteString;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass;
import pt.ulisboa.tecnico.cross.contract.User;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
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
import java.util.Random;

public class APIClient {
  private final RestTemplate restTemplate;
  private final HttpHeaders headers;


  public APIClient() {
    this.restTemplate = new RestTemplate(List.of(new ProtobufHttpMessageConverter()));
    HttpComponentsClientHttpRequestFactory factory = null;
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      X509Certificate x509Cert =
              (X509Certificate) cf.generateCertificate(APIClient.class.getResourceAsStream("/CA.crt"));

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
    } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | KeyManagementException | IOException e) {
      throw new RuntimeException("An error occurred during client creation: " + e.getMessage());
    }
    restTemplate.setRequestFactory(factory);

    this.headers = new HttpHeaders();
  }

  public String signin(String signinUrl, String userId) {
    // 128 bits session ID
    byte[] array = new byte[8];
    new Random().nextBytes(array);
    String randomSessionId = new String(array, StandardCharsets.UTF_8);
    User.Credentials credentials = User.Credentials.newBuilder().setUsername(userId).setPassword("123456").setCryptoIdentity(User.CryptoIdentity.newBuilder().setPublicKey(ByteString.EMPTY).setSessionId(randomSessionId).build()).build();
    User.Authorization auth = restTemplate.postForObject(signinUrl, credentials, User.Authorization.class);

    return auth.getJwt();
  }

  public ResponseEntity<TripOuterClass.GetTripsResponse> getTrips(String tripUrl, String jwt) {
    this.headers.set("Authorization", jwt);
    return restTemplate.exchange(tripUrl, HttpMethod.GET, new HttpEntity<>(headers), TripOuterClass.GetTripsResponse.class);
  }
}
