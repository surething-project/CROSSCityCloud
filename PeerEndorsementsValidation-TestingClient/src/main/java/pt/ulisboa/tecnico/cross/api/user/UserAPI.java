package pt.ulisboa.tecnico.cross.api.user;

import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import pt.ulisboa.tecnico.cross.account.CryptoManager;
import pt.ulisboa.tecnico.cross.api.APIClient;
import pt.ulisboa.tecnico.cross.api.APIManager;
import pt.ulisboa.tecnico.cross.contract.User.Credentials;
import pt.ulisboa.tecnico.cross.contract.User.CryptoIdentity;
import pt.ulisboa.tecnico.cross.contract.User.Welcome;

public class UserAPI {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserAPI.class);

  public String signup(
      String username, String password, String sessionId, CryptoManager cryptoManager) {
    CryptoIdentity cryptoIdentity =
        CryptoIdentity.newBuilder()
            .setSessionId(sessionId)
            .setPublicKey(ByteString.copyFrom(cryptoManager.getUserPublicKey().getEncoded()))
            .build();
    Credentials credentials =
        Credentials.newBuilder()
            .setUsername(username)
            .setPassword(password)
            .setCryptoIdentity(cryptoIdentity)
            .build();

    try {
      Welcome welcome =
          APIClient.getRestTemplate()
              .postForObject(
                  APIManager.get().SERVER_BASE_URL + "user/signup", credentials, Welcome.class);
      assert welcome != null;
      cryptoManager.storeServerCertificate(welcome.getServerCertificate().toByteArray());
      return welcome.getJwt();
    } catch (RestClientException e) {
      LOGGER.error("Failed to authenticate the user.", e);
      System.exit(1);
      return null;
    }
  }
}
