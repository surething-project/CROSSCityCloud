package pt.ulisboa.tecnico.cross.user;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.badging.BadgingRepository;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.user.domain.User;
import pt.ulisboa.tecnico.cross.utils.PBEUtils;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;

import static jakarta.ws.rs.core.Response.Status.*;
import static org.postgresql.util.PSQLState.UNIQUE_VIOLATION;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.getDb;

@ApplicationScoped
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository = UserRepository.get();
  private final BadgingRepository badgingRepository = BadgingRepository.get();
  private final PBEUtils pbeUtils = PBEUtils.get();

  public User authenticateUser(
      String username, String password, @Nullable String sessionId, @Nullable byte[] publicKey) {
    try (Connection con = getDb()) {
      User user = userRepository.getUser(con, username);
      boolean isExpectedPwd =
          pbeUtils.isExpectedPwd(password, user.getPasswordHash(), user.getPasswordSalt());
      if (!isExpectedPwd) {
        throw new APIException(UNAUTHORIZED, "Incorrect username or password.");
      }
      if (sessionId != null && publicKey != null) {
        userRepository.addCryptoIdentity(con, username, sessionId, publicKey);
      }
      user.addAllOwnedBadges(badgingRepository.getOwnedBadges(con, username));
      return user;
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Encryption error.");
    }
  }

  public void registerUser(
      String username, String password, @Nonnull String sessionId, @Nonnull byte[] publicKey) {
    try (Connection con = getDb()) {
      byte[] passwordSalt = pbeUtils.newSalt();
      byte[] passwordHash = pbeUtils.hash(password, passwordSalt);
      con.setAutoCommit(false);
      userRepository.createUser(con, username, passwordHash, passwordSalt);
      userRepository.addCryptoIdentity(con, username, sessionId, publicKey);
      con.commit();
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      if (e.getSQLState().equals(UNIQUE_VIOLATION.getState())) {
        throw new APIException(CONFLICT, "The user '" + username + "' already exists.");
      }
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Encryption error.");
    }
  }

  public void registerToken(String username, String registrationToken) {
    try (Connection con = getDb()) {
      userRepository.renewToken(con, username, registrationToken);
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }
}
