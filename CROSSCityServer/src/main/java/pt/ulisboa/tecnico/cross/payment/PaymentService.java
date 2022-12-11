package pt.ulisboa.tecnico.cross.payment;

import com.google.firebase.messaging.Notification;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.user.UserRepository;
import pt.ulisboa.tecnico.cross.user.domain.User;
import pt.ulisboa.tecnico.cross.utils.MessagingUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.getDb;

@ApplicationScoped
public class PaymentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);
  private final UserRepository userRepository = UserRepository.get();
  private final MessagingUtils messagingUtils = MessagingUtils.get();

  public void payment(String username, int gems) {
    try (Connection con = getDb()) {
      con.setAutoCommit(false);
      User user = userRepository.getUser(con, username);
      if (user.getGems() >= gems) userRepository.updateGems(con, username, -gems);
      else {
        LOGGER.error("The user does not have enough gems to make the payment.");
        throw new APIException(
            PRECONDITION_FAILED, "The user does not have enough gems to make the payment.");
      }
      messagingUtils.notify(
          con,
          username,
          Notification.builder()
              .setTitle("Payment")
              .setBody(String.format("The payment of %d gems was successful!", gems))
              .build(),
          Collections.singletonMap("deductedGems", String.valueOf(gems)));
      con.commit();
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }
}
