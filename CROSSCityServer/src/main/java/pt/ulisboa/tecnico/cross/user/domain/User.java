package pt.ulisboa.tecnico.cross.user.domain;

import java.util.ArrayList;
import java.util.List;

public class User {

  private final String username;
  private final byte[] passwordHash;
  private final byte[] passwordSalt;
  private final String registrationToken;
  private final int gems;
  private final List<String> ownedBadges;

  public User(
      String username,
      byte[] passwordHash,
      byte[] passwordSalt,
      String registrationToken,
      int gems,
      List<String> ownedBadges) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.passwordSalt = passwordSalt;
    this.registrationToken = registrationToken;
    this.gems = gems;
    this.ownedBadges = ownedBadges;
  }

  public User(
      String username,
      byte[] passwordHash,
      byte[] passwordSalt,
      String registrationToken,
      int gems) {
    this(username, passwordHash, passwordSalt, registrationToken, gems, new ArrayList<>());
  }

  public String getUsername() {
    return username;
  }

  public byte[] getPasswordHash() {
    return passwordHash;
  }

  public byte[] getPasswordSalt() {
    return passwordSalt;
  }

  public String getRegistrationToken() {
    return registrationToken;
  }

  public int getGems() {
    return gems;
  }

  public List<String> getOwnedBadges() {
    return ownedBadges;
  }

  public void addAllOwnedBadges(List<String> ownedBadges) {
    this.ownedBadges.addAll(ownedBadges);
  }
}
