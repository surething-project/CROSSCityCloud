package pt.ulisboa.tecnico.cross.user;

import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.user.domain.User;

import java.sql.*;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

public class UserRepository {

  public static UserRepository get() {
    return UserRepositoryHolder.INSTANCE;
  }

  private UserRepository() {}

  public User getUser(Connection con, String username) throws SQLException {

    PreparedStatement stmt = con.prepareStatement("SELECT * FROM cross_user WHERE username = ?");
    stmt.setString(1, username);

    ResultSet rs = stmt.executeQuery();
    if (!rs.next()) {
      throw new APIException(
          NOT_FOUND, String.format("The user '%s' is not registered.", username));
    }
    User user =
        new User(
            username,
            rs.getBytes("password_hash"),
            rs.getBytes("password_salt"),
            rs.getString("registration_token"),
            rs.getInt("gems"));
    stmt.close();
    return user;
  }

  public byte[] getPublicKey(Connection con, String username, String sessionId)
      throws SQLException {

    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT pubkey FROM user_crypto_identity WHERE username = ? AND session_id = ?");
    stmt.setString(1, username);
    stmt.setString(2, sessionId);

    ResultSet rs = stmt.executeQuery();
    byte[] publicKey = rs.next() ? rs.getBytes("pubkey") : null;
    stmt.close();
    return publicKey;
  }

  public void addCryptoIdentity(Connection con, String username, String sessionId, byte[] publicKey)
      throws SQLException {

    PreparedStatement stmt =
        con.prepareStatement(
            "INSERT INTO user_crypto_identity (username, session_id, pubkey) VALUES (?, ?, ?)");
    stmt.setString(1, username);
    stmt.setString(2, sessionId);
    stmt.setBytes(3, publicKey);
    stmt.executeUpdate();
    stmt.close();
  }

  public void createUser(Connection con, String username, byte[] passwordHash, byte[] passwordSalt)
      throws SQLException {

    PreparedStatement stmt =
        con.prepareStatement(
            "INSERT INTO cross_user (username, password_hash, password_salt, gems, joined) VALUES (?, ?, ?, ?, ?)");
    stmt.setString(1, username);
    stmt.setBytes(2, passwordHash);
    stmt.setBytes(3, passwordSalt);
    stmt.setInt(4, 0);
    stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
    stmt.executeUpdate();
    stmt.close();
  }

  public void renewToken(Connection con, String username, String registrationToken)
      throws SQLException {

    PreparedStatement stmt =
        con.prepareStatement("UPDATE cross_user SET registration_token = ? WHERE username = ?");
    stmt.setString(1, registrationToken);
    stmt.setString(2, username);
    stmt.executeUpdate();
    stmt.close();
  }

  public void updateGems(Connection con, String username, int awardedGems) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement("UPDATE cross_user SET gems = gems + ? WHERE username = ?");
    stmt.setInt(1, awardedGems);
    stmt.setString(2, username);
    stmt.executeUpdate();
    stmt.close();
  }

  private static class UserRepositoryHolder {
    private static final UserRepository INSTANCE = new UserRepository();
  }
}
