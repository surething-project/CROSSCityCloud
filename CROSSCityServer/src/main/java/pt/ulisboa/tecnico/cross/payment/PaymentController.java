package pt.ulisboa.tecnico.cross.payment;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.contract.gamification.PaymentScanning;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.utils.JwtService;
import pt.ulisboa.tecnico.cross.utils.MediaType;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/payment")
public class PaymentController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PaymentController.class);
  @Inject private JwtService jwtService;
  @Inject private PaymentService paymentService;

  @PUT
  @PermitAll
  @Consumes(MediaType.APPLICATION_PROTOBUF)
  public void payment(PaymentScanning.Transaction transaction) {
    String username = jwtService.getUsername(transaction.getEncryptedJwt().toByteArray());
    if (username != null) paymentService.payment(username, transaction.getGems());
    else {
      LOGGER.error("The jwt is invalid.");
      throw new APIException(BAD_REQUEST, "The jwt is invalid.");
    }
  }
}
