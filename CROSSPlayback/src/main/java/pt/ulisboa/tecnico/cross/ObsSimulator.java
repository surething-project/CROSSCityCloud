package pt.ulisboa.tecnico.cross;

import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import pt.ulisboa.tecnico.cross.scavenger.WiFiAPObservationOuterClass;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

import static java.util.Map.entry;

public class ObsSimulator {
  private final Properties PROPS = new Properties();
  private final RestTemplate restTemplate;
  private final HttpHeaders headers;
  private String url;

  public ObsSimulator() throws IOException {
    PROPS.load(DatasetFileReader.class.getResourceAsStream("/application.properties"));
    this.url = PROPS.getProperty("OBS_URL");
    int maxConnPool = Integer.parseInt(PROPS.getProperty("CONNECTION_POOL"));

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

      Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
              .<ConnectionSocketFactory>create()
              .register("https", socketFactory)
              .build();

      PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
      poolingConnManager.setMaxTotal(maxConnPool);
      poolingConnManager.setDefaultMaxPerRoute(maxConnPool);

      CloseableHttpClient httpClient = HttpClients.custom()
              .setSSLSocketFactory(socketFactory)
              .setConnectionManager(poolingConnManager)
              .build();

      factory = new HttpComponentsClientHttpRequestFactory(httpClient);
    } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException("An error occurred during client creation: " + e.getMessage());
    }
    restTemplate.setRequestFactory(factory);
    this.headers = new HttpHeaders();
  }

  private String randomMACAddress() {
    Random rand = new Random();
    byte[] macAddr = new byte[6];
    rand.nextBytes(macAddr);

    macAddr[0] = (byte) (macAddr[0] & (byte) 254);

    StringBuilder sb = new StringBuilder(18);
    for (byte b : macAddr) {
      if (sb.length() > 0)
        sb.append(":");

      sb.append(String.format("%02x", b));
    }

    return sb.toString();
  }

  public void simulate(String poiId, String traveler, int numberOfAps) {
    List<String> bssids = new ArrayList<>();
    ZipfDistribution distribution = new ZipfDistribution(295, 1.177358);
    Map<String, Double> aps = Map.ofEntries(
            entry("82:2a:a8:f7:f8:04",	0.9799891833423),
            entry("80:2a:a8:f7:f8:04",	0.9751216873986),
            entry("c4:ea:1d:6a:85:c7",	0.9691725256896),
            entry("b4:86:55:4d:7d:fb",	0.8734451054624),
            entry("00:1f:9f:4c:b2:73",	0.5521903731747),
            entry("fc:ec:da:3e:92:1f",	0.4829637641969),
            entry("fe:ec:da:3f:92:1f",	0.4407787993510),
            entry("10:fe:ed:c3:62:e4",	0.2833964305030),
            entry("9c:97:26:8e:63:69",	0.2558139534884),
            entry("b8:94:36:a8:ba:03",	0.2287723093564),
            entry("30:91:8f:00:8b:2d",	0.2276906435911),
            entry("30:74:96:97:aa:94",	0.1952406706328),
            entry("30:74:96:97:a7:83",	0.1725256895619),
            entry("30:74:96:97:aa:ee",	0.1611681990265),
            entry("88:ce:fa:4d:83:8f",	0.1519740400216),
            entry("f8:32:e4:45:86:00",	0.1281773931855),
            entry("58:98:35:f3:a8:a7",	0.1249323958897),
            entry("76:99:63:ea:f2:38",	0.1141157382369),
            entry("be:a5:8b:c8:e2:b4",	0.0930232558140),
            entry("c4:06:83:31:a2:b1",	0.0914007571660),
            entry("e0:e6:2e:a5:dc:94",	0.0805840995133),
            entry("58:98:35:bd:42:8c",	0.0767982693348),
            entry("d6:d9:19:98:11:60",	0.0708491076257),
            entry("3c:46:d8:84:59:df",	0.0600324499730),
            entry("00:24:17:be:1f:8e",	0.0600324499730),
            entry("30:89:d3:ec:fd:d0",	0.0497566252028),
            entry("fe:08:0f:5e:2a:cc",	0.0454299621417),
            entry("10:30:47:95:15:9b",	0.0432666306111),
            entry("04:f0:21:45:86:c8",	0.0373174689021),
            entry("9c:97:26:41:e8:53",	0.0367766360195),
            entry("9e:97:26:7d:25:94",	0.0351541373716),
            entry("9c:97:26:7d:25:93",	0.0297458085452),
            entry("e0:e6:2e:ef:98:1d",	0.0297458085452),
            entry("04:f0:21:45:77:24",	0.0292049756625),
            entry("06:41:69:a6:2d:45",	0.0281233098972),
            entry("04:f0:21:45:77:3b",	0.0254191454840),
            entry("f6:dd:9e:7e:d4:59",	0.0254191454840),
            entry("90:2b:d2:31:60:3e",	0.0248783126014),
            entry("d4:6e:0e:4e:c1:62",	0.0243374797188),
            entry("04:f0:21:45:85:ec",	0.0237966468361),
            entry("82:20:21:98:60:88",	0.0232558139535),
            entry("04:f0:21:45:89:b3",	0.0227149810708),
            entry("04:f0:21:45:77:19",	0.0221741481882),
            entry("9c:97:26:05:41:d0",	0.0216333153056),
            entry("f8:c3:9e:93:0b:7e",	0.0216333153056),
            entry("04:f0:21:45:f5:95",	0.0205516495403),
            entry("00:1c:df:e4:8c:83",	0.0205516495403),
            entry("04:f0:21:46:04:c4",	0.0194699837750),
            entry("f6:dd:9e:e1:2a:ae",	0.0183883180097),
            entry("04:f0:21:45:78:0a",	0.0178474851271),
            entry("04:f0:21:45:88:7f",	0.0178474851271),
            entry("d4:28:d5:2e:0a:6c",	0.0178474851271),
            entry("e2:5f:45:81:fb:44",	0.0178474851271),
            entry("04:f0:21:45:77:3f",	0.0167658193618),
            entry("06:41:69:b3:40:41",	0.0167658193618),
            entry("30:89:d4:7c:1c:e0",	0.0162249864792),
            entry("6e:e8:5c:3e:a8:b8",	0.0162249864792),
            entry("88:bf:e4:12:07:1b",	0.0162249864792),
            entry("04:f0:21:45:77:39",	0.0151433207139),
            entry("04:f0:21:45:86:df",	0.0151433207139),
            entry("04:f0:21:46:03:e4",	0.0151433207139),
            entry("70:8a:09:4a:70:3a",	0.0151433207139),
            entry("76:b5:87:84:d1:1c",	0.0151433207139),
            entry("ba:41:a4:5a:0b:44",	0.0151433207139),
            entry("de:f7:56:b2:d5:ca",	0.0146024878313),
            entry("fc:87:43:c2:88:0b",	0.0146024878313),
            entry("04:f0:21:45:f5:6b",	0.0140616549486),
            entry("04:f0:21:45:77:23",	0.0135208220660),
            entry("04:f0:21:45:fd:73",	0.0135208220660),
            entry("04:f0:21:45:88:99",	0.0129799891833),
            entry("04:f0:21:45:77:ff",	0.0124391563007),
            entry("04:f0:21:45:fd:e5",	0.0124391563007),
            entry("04:f0:21:46:01:68",	0.0124391563007),
            entry("f2:d2:b0:0c:4f:19",	0.0124391563007),
            entry("90:2b:d2:2b:6a:94",	0.0124391563007),
            entry("04:f0:21:46:06:17",	0.0118983234181),
            entry("32:c3:d9:46:43:fe",	0.0118983234181),
            entry("00:50:18:68:83:02",	0.0113574905354),
            entry("04:f0:21:2c:ec:bd",	0.0113574905354),
            entry("04:f0:21:45:77:11",	0.0113574905354),
            entry("aa:db:03:46:59:79",	0.0113574905354),
            entry("58:98:35:3a:4c:15",	0.0102758247701),
            entry("f4:b8:5e:df:84:3a",	0.0102758247701),
            entry("00:50:18:68:82:36",	0.0097349918875),
            entry("68:c9:0b:01:20:de",	0.0097349918875),
            entry("30:a1:fa:f6:36:d5",	0.0097349918875),
            entry("e4:e1:30:d5:89:73",	0.0097349918875),
            entry("04:f0:21:45:85:f0",	0.0091941590049),
            entry("04:f0:21:45:f5:8a",	0.0091941590049),
            entry("04:f0:21:46:03:ea",	0.0091941590049),
            entry("30:89:d4:4a:55:30",	0.0091941590049),
            entry("a2:b0:3d:77:61:81",	0.0091941590049),
            entry("04:f0:21:45:77:51",	0.0086533261222),
            entry("62:f1:89:6a:62:31",	0.0086533261222),
            entry("aa:db:03:0e:82:b7",	0.0086533261222),
            entry("b4:9d:0b:68:ff:52",	0.0086533261222),
            entry("e4:e1:30:c7:86:92",	0.0086533261222),
            entry("58:98:35:ba:66:b3",	0.0086533261222),
            entry("9c:97:26:dd:45:17",	0.0086533261222),
            entry("04:f0:21:45:77:4a",	0.0081124932396),
            entry("04:f0:21:45:86:e1",	0.0081124932396),
            entry("04:f0:21:45:fd:75",	0.0081124932396),
            entry("04:f0:21:46:06:1f",	0.0081124932396),
            entry("7c:76:68:a5:05:4c",	0.0081124932396),
            entry("e4:e1:30:7c:96:56",	0.0081124932396),
            entry("00:06:91:40:ae:d2",	0.0075716603569),
            entry("00:11:6b:73:f4:fd",	0.0075716603569),
            entry("04:f0:21:2c:f1:22",	0.0075716603569),
            entry("04:f0:21:45:76:f9",	0.0075716603569),
            entry("28:56:c1:71:df:32",	0.0075716603569),
            entry("04:f0:21:45:f5:70",	0.0070308274743),
            entry("04:f0:21:46:02:87",	0.0070308274743),
            entry("28:83:35:3b:a4:dd",	0.0070308274743),
            entry("e6:2b:34:37:a7:8f",	0.0070308274743),
            entry("a8:a1:98:13:bb:5c",	0.0064899945917),
            entry("04:f0:21:45:89:93",	0.0064899945917),
            entry("fa:bb:2c:e4:ed:81",	0.0064899945917),
            entry("04:f0:21:46:03:e0",	0.0064899945917),
            entry("60:12:8b:eb:5a:7e",	0.0059491617090),
            entry("e4:a7:c5:6d:5d:59",	0.0059491617090),
            entry("e4:e1:30:9d:db:4f",	0.0059491617090),
            entry("9c:97:26:12:3d:36",	0.0059491617090),
            entry("04:f0:21:45:77:3a",	0.0054083288264),
            entry("04:f0:21:45:f5:84",	0.0054083288264),
            entry("5c:a8:6a:82:c6:d0",	0.0054083288264),
            entry("00:06:91:40:ae:d0",	0.0054083288264),
            entry("58:98:35:90:32:8d",	0.0054083288264),
            entry("b0:c1:9e:7a:36:59",	0.0054083288264),
            entry("00:24:17:6e:2e:eb",	0.0054083288264),
            entry("a0:57:e3:ff:08:37",	0.0054083288264),
            entry("04:f0:21:45:86:d1",	0.0048674959438),
            entry("04:f0:21:45:86:de",	0.0048674959438),
            entry("cc:4b:73:19:9f:3b",	0.0048674959438),
            entry("e0:e6:2e:5f:4f:b7",	0.0048674959438),
            entry("f4:60:e2:cb:f9:4e",	0.0048674959438),
            entry("6e:74:bf:16:36:4e",	0.0048674959438),
            entry("04:f0:21:45:77:58",	0.0043266630611),
            entry("04:f0:21:45:fe:18",	0.0043266630611),
            entry("0c:8f:ff:ae:0d:54",	0.0043266630611),
            entry("62:98:ad:84:1a:f0",	0.0043266630611),
            entry("94:27:90:5f:ed:80",	0.0043266630611),
            entry("9c:4f:cf:5b:7a:d2",	0.0043266630611),
            entry("e0:e6:2e:51:6d:71",	0.0043266630611),
            entry("e0:e6:2e:d7:2f:6a",	0.0043266630611),
            entry("ec:89:14:9e:d4:cf",	0.0043266630611),
            entry("fe:2d:5e:43:d7:f6",	0.0043266630611),
            entry("5c:77:76:cd:7c:54",	0.0043266630611),
            entry("9c:4f:cf:c3:bf:51",	0.0043266630611),
            entry("5c:77:76:ac:32:37",	0.0043266630611),
            entry("00:50:18:68:81:a2",	0.0037858301785),
            entry("04:f0:21:46:05:f9",	0.0037858301785),
            entry("42:3f:8c:1f:83:71",	0.0037858301785),
            entry("c6:9a:02:8c:53:0d",	0.0037858301785),
            entry("fe:18:3c:71:2a:fa",	0.0037858301785),
            entry("46:d9:e7:43:47:e2",	0.0037858301785),
            entry("04:f0:21:45:77:0c",	0.0032449972958),
            entry("04:f0:21:45:86:c7",	0.0032449972958),
            entry("04:f0:21:45:f5:89",	0.0032449972958),
            entry("04:f0:21:45:f5:a0",	0.0032449972958),
            entry("04:f0:21:46:05:f5",	0.0032449972958),
            entry("14:6b:9c:0c:27:69",	0.0032449972958),
            entry("30:89:d4:03:b1:80",	0.0032449972958),
            entry("9c:4f:cf:2d:4d:e2",	0.0032449972958),
            entry("f4:bf:80:01:cc:b2",	0.0032449972958),
            entry("f6:c2:48:05:99:04",	0.0032449972958),
            entry("04:f0:21:45:86:b4",	0.0027041644132),
            entry("38:37:8b:27:f7:34",	0.0027041644132),
            entry("5c:77:76:05:c5:34",	0.0027041644132),
            entry("5c:77:76:6a:45:d7",	0.0027041644132),
            entry("5c:77:76:8f:36:bd",	0.0027041644132),
            entry("72:bc:96:79:0b:1d",	0.0027041644132),
            entry("7e:38:ad:18:67:37",	0.0027041644132),
            entry("94:27:90:0c:2e:a5",	0.0027041644132),
            entry("9c:4f:cf:15:04:93",	0.0027041644132),
            entry("e0:e6:2e:1c:3a:23",	0.0027041644132),
            entry("e0:e6:2e:58:99:1b",	0.0027041644132),
            entry("e0:e6:2e:86:fc:0e",	0.0027041644132),
            entry("e4:e1:30:c2:6d:eb",	0.0027041644132),
            entry("e4:e1:30:c9:a7:98",	0.0027041644132),
            entry("00:06:91:3f:e9:f2",	0.0027041644132),
            entry("3a:f8:89:09:f1:9b",	0.0027041644132),
            entry("e4:e1:30:da:c2:65",	0.0027041644132),
            entry("00:26:44:35:61:94",	0.0027041644132),
            entry("04:f0:21:45:84:e2",	0.0021633315306),
            entry("04:f0:21:45:f5:94",	0.0021633315306),
            entry("04:f0:21:45:fd:6e",	0.0021633315306),
            entry("04:f0:21:46:06:00",	0.0021633315306),
            entry("04:f0:21:46:06:21",	0.0021633315306),
            entry("0a:c5:e1:db:45:74",	0.0021633315306),
            entry("32:c3:d9:33:e6:30",	0.0021633315306),
            entry("5c:77:76:d5:3e:c8",	0.0021633315306),
            entry("5c:e7:bf:e1:53:84",	0.0021633315306),
            entry("90:2b:d2:14:49:50",	0.0021633315306),
            entry("90:2b:d2:9f:79:1e",	0.0021633315306),
            entry("a4:50:46:88:77:1d",	0.0021633315306),
            entry("a8:a1:98:7a:84:16",	0.0021633315306),
            entry("e0:e6:2e:7c:4e:37",	0.0021633315306),
            entry("e4:e1:30:47:9b:eb",	0.0021633315306),
            entry("e4:e1:30:d0:0e:d1",	0.0021633315306),
            entry("fe:2d:5e:31:fe:6c",	0.0021633315306),
            entry("00:26:44:54:d9:25",	0.0021633315306),
            entry("20:a6:80:ff:43:3c",	0.0021633315306),
            entry("5c:77:76:69:d1:d8",	0.0021633315306),
            entry("8e:79:67:a6:19:23",	0.0021633315306),
            entry("9c:97:26:99:49:e9",	0.0021633315306),
            entry("00:be:3b:b6:14:17",	0.0016224986479),
            entry("04:f0:21:45:78:00",	0.0016224986479),
            entry("04:f0:21:45:fe:0a",	0.0016224986479),
            entry("04:f0:21:46:04:bc",	0.0016224986479),
            entry("04:f0:21:46:06:13",	0.0016224986479),
            entry("04:f0:21:46:06:18",	0.0016224986479),
            entry("06:41:69:bb:db:cc",	0.0016224986479),
            entry("24:09:95:1a:25:4e",	0.0016224986479),
            entry("32:07:4d:7d:61:ba",	0.0016224986479),
            entry("4c:4e:03:87:01:80",	0.0016224986479),
            entry("5c:77:76:71:ec:5e",	0.0016224986479),
            entry("68:c9:0b:01:04:81",	0.0016224986479),
            entry("74:4a:a4:f8:5a:ae",	0.0016224986479),
            entry("74:eb:80:87:7a:ca",	0.0016224986479),
            entry("94:27:90:39:72:91",	0.0016224986479),
            entry("d4:28:d5:a9:fd:74",	0.0016224986479),
            entry("e0:e6:2e:62:84:44",	0.0016224986479),
            entry("e0:e6:2e:73:29:30",	0.0016224986479),
            entry("e0:e6:2e:7c:52:5a",	0.0016224986479),
            entry("e0:e6:2e:8d:15:d6",	0.0016224986479),
            entry("e0:e6:2e:8e:a3:26",	0.0016224986479),
            entry("e4:a7:c5:cf:84:ff",	0.0016224986479),
            entry("fc:2d:5e:72:ba:56",	0.0016224986479),
            entry("00:06:91:3f:e9:f0",	0.0016224986479),
            entry("06:41:69:66:01:e3",	0.0016224986479),
            entry("5c:77:76:68:4a:8e",	0.0016224986479),
            entry("cc:2d:e0:31:fd:a9",	0.0016224986479),
            entry("e0:e6:2e:62:5b:e2",	0.0016224986479),
            entry("e4:58:e7:c1:83:e0",	0.0016224986479),
            entry("e4:e1:30:9f:5c:cb",	0.0016224986479),
            entry("e6:e1:30:3e:2a:01",	0.0016224986479),
            entry("00:1e:42:23:b1:16",	0.0010816657653),
            entry("04:f0:21:45:f5:e0",	0.0010816657653),
            entry("04:f0:21:46:01:7c",	0.0010816657653),
            entry("26:18:1d:43:ca:2d",	0.0010816657653),
            entry("2c:56:dc:b8:78:ac",	0.0010816657653),
            entry("4a:c8:62:04:cd:ef",	0.0010816657653),
            entry("5c:77:76:53:71:f0",	0.0010816657653),
            entry("94:27:90:9f:bc:bf",	0.0010816657653),
            entry("c0:25:e9:02:a3:0c",	0.0010816657653),
            entry("c4:86:e9:98:c6:8d",	0.0010816657653),
            entry("d2:5b:a8:ca:8c:5e",	0.0010816657653),
            entry("d8:55:75:af:76:24",	0.0010816657653),
            entry("e0:e6:2e:84:95:b4",	0.0010816657653),
            entry("00:06:91:40:71:d2",	0.0010816657653),
            entry("00:1b:b1:eb:e9:1e",	0.0010816657653),
            entry("0a:c5:e1:0b:90:39",	0.0010816657653),
            entry("0a:c5:e1:0e:74:87",	0.0010816657653),
            entry("0c:cb:85:a9:0c:e2",	0.0010816657653),
            entry("58:98:35:bb:6a:51",	0.0010816657653),
            entry("5c:77:76:1b:66:9c",	0.0010816657653),
            entry("6e:00:6b:63:87:91",	0.0010816657653),
            entry("a8:a1:98:3b:0a:0b",	0.0010816657653),
            entry("aa:db:03:93:2b:bd",	0.0010816657653),
            entry("ec:9b:f3:b6:91:ac",	0.0010816657653),
            entry("f0:92:1c:99:2f:16",	0.0010816657653),
            entry("fc:2d:5e:4d:ef:58",	0.0010816657653),
            entry("04:f0:21:45:86:bd",	0.0010816657653),
            entry("04:02:1f:b6:9e:ef",	0.0005408328826),
            entry("04:f0:21:2c:f1:27",	0.0005408328826),
            entry("1c:99:4c:f0:04:2b",	0.0005408328826),
            entry("4a:f0:7b:1b:b4:ec",	0.0005408328826),
            entry("50:9f:27:6b:66:d8",	0.0005408328826),
            entry("5c:77:76:9a:f8:74",	0.0005408328826),
            entry("68:a0:3e:5f:20:97",	0.0005408328826),
            entry("7c:7d:3d:2f:c1:cf",	0.0005408328826),
            entry("94:27:90:9d:ef:2e",	0.0005408328826),
            entry("94:65:2d:78:d0:0a",	0.0005408328826),
            entry("9c:df:03:9c:a4:49",	0.0005408328826),
            entry("a8:a1:98:78:c2:0b",	0.0005408328826),
            entry("a8:a1:98:7f:3e:f6",	0.0005408328826),
            entry("b0:c1:9e:86:99:9c",	0.0005408328826),
            entry("c4:49:bb:74:43:b4",	0.0005408328826),
            entry("d4:28:d5:1c:b6:e3",	0.0005408328826),
            entry("e0:e6:2e:76:34:6e",	0.0005408328826),
            entry("e0:e6:2e:80:e3:5a",	0.0005408328826),
            entry("e0:e6:2e:8b:8f:9c",	0.0005408328826),
            entry("e4:e1:30:e2:cc:05",	0.0005408328826),
            entry("fe:2d:5e:b1:11:55",	0.0005408328826),
            entry("00:06:91:3f:e0:e0",	0.0005408328826),
            entry("00:06:91:3f:e0:e2",	0.0005408328826),
            entry("00:06:91:cb:eb:f2",	0.0005408328826),
            entry("aa:db:03:42:23:8e",	0.0005408328826),
            entry("e0:e6:2e:44:fa:10",	0.0005408328826),
            entry("e4:e1:30:c7:85:0f",	0.0005408328826),
            entry("f6:dd:9e:ee:fa:28",	0.0005408328826),
            entry("2a:5a:eb:86:8d:e7",	0.0005408328826),
            entry("60:60:1f:bb:43:bb",	0.0005408328826),
            entry("a8:a1:98:6c:2b:db",	0.0005408328826),
            entry("e4:e1:30:37:c9:a5",	0.0005408328826)
    );
    int lowObsBound = (int) Math.ceil(numberOfAps / 6.0);
    int highObsBound = (int) Math.ceil(numberOfAps / 2.0);
    int minDelayMillis = 0;
    int maxDelayMillis = 60000;
    Random r = new Random();
    Random rMillis = new Random();
    Random rDupe = new Random();
    Random rAp = new Random();

    for (int i = 0; i < numberOfAps; i++) {
      bssids.add(randomMACAddress());
    }

    for (int i = 1; i <= bssids.size(); i++) {
      System.out.println(bssids.get(i - 1));
    }

    for (int i = 1; i <= bssids.size(); i++) {
      System.out.println(distribution.probability(i));
    }

    while (true) {
      int result = r.nextInt(highObsBound - lowObsBound) + lowObsBound;
      int delay = rMillis.nextInt(maxDelayMillis - minDelayMillis) + minDelayMillis;
      WiFiAPObservationOuterClass.WiFiAPObservationList.Builder observations = WiFiAPObservationOuterClass.WiFiAPObservationList.newBuilder().setPoiId(poiId).setTraveler(traveler);

      for (String ap : aps.keySet()) {
        double prob = rAp.nextDouble();
        double apProb = aps.get(ap);

        if (prob <= apProb) {
          WiFiAPObservationOuterClass.WiFiAPObservation obs = WiFiAPObservationOuterClass.WiFiAPObservation.newBuilder().setPoiId(poiId).setTraveler(traveler).setBssid(bssids.get(distribution.sample() - 1)).setSightingMillis(System.currentTimeMillis() - delay).build();
          observations.addObservations(obs);
        }
      }

/*      for (int i = 0; i < result; i++) {
        WiFiAPObservationOuterClass.WiFiAPObservation obs = WiFiAPObservationOuterClass.WiFiAPObservation.newBuilder().setPoiId(poiId).setTraveler(traveler).setBssid(bssids.get(distribution.sample() - 1)).setSightingMillis(System.currentTimeMillis() - delay).build();
        observations.addObservations(obs);
      }*/
      HttpEntity<WiFiAPObservationOuterClass.WiFiAPObservationList> entity = new HttpEntity<>(observations.build(), headers);
      restTemplate.postForObject(this.url, entity, WiFiAPObservationOuterClass.WiFiAPObservationList.class);

      // Check if we duplicate observation or not
      if (rDupe.nextInt(2) > 0) {
        restTemplate.postForObject(this.url, entity, WiFiAPObservationOuterClass.WiFiAPObservationList.class);
      }
    }
  }
}
