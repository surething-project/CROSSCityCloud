package pt.ulisboa.tecnico.cross.simulation;

import pt.ulisboa.tecnico.cross.account.CryptoManager;
import pt.ulisboa.tecnico.cross.api.APIManager;

import java.util.UUID;

public class User {

  private final String username;
  private final String sessionId;
  private final CryptoManager cryptoManager;
  private String jwt;

  public User() {
    username = UUID.randomUUID().toString();
    sessionId = UUID.randomUUID().toString();
    cryptoManager = new CryptoManager();
  }

  public String getUsername() {
    return username;
  }

  public String getSessionId() {
    return sessionId;
  }

  public CryptoManager getCryptoManager() {
    return cryptoManager;
  }

  public String getJwt() {
    return jwt;
  }

  synchronized void authenticate() {
    if (jwt == null) {
      cryptoManager.generateNewUserKeyPair();
      jwt =
          APIManager.get()
              .getUserAPI()
              .signup(username, UUID.randomUUID().toString(), sessionId, cryptoManager);
    }
  }
}
