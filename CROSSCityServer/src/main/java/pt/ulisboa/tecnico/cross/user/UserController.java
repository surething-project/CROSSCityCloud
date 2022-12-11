package pt.ulisboa.tecnico.cross.user;

import com.google.protobuf.ByteString;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import pt.ulisboa.tecnico.cross.contract.User.Credentials;
import pt.ulisboa.tecnico.cross.contract.User.Token;
import pt.ulisboa.tecnico.cross.contract.User.Welcome;
import pt.ulisboa.tecnico.cross.user.domain.User;
import pt.ulisboa.tecnico.cross.utils.JwtService;
import pt.ulisboa.tecnico.cross.utils.Keychain;
import pt.ulisboa.tecnico.cross.utils.MediaType;

import java.util.List;

@Path("/user")
public class UserController {

  @Inject private JwtService jwtService;
  @Inject private UserService userService;
  private final Keychain keychain = Keychain.get();

  @POST()
  @Path("/signin")
  @PermitAll
  @Consumes(MediaType.APPLICATION_PROTOBUF)
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public Welcome signin(Credentials credentials) {
    User user =
        userService.authenticateUser(
            credentials.getUsername(),
            credentials.getPassword(),
            // The user may be logging in from another device
            credentials.hasCryptoIdentity() ? credentials.getCryptoIdentity().getSessionId() : null,
            credentials.hasCryptoIdentity()
                ? credentials.getCryptoIdentity().getPublicKey().toByteArray()
                : null);
    return Welcome.newBuilder()
        .setGems(user.getGems())
        .addAllOwnedBadges(user.getOwnedBadges())
        // In the future, new roles may be defined here
        .setJwt(jwtService.createJwt(credentials.getUsername(), List.of("USER")))
        .setServerCertificate(ByteString.copyFrom(keychain.getCertificate()))
        .build();
  }

  @POST()
  @Path("/signup")
  @PermitAll
  @Consumes(MediaType.APPLICATION_PROTOBUF)
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public Welcome signup(Credentials credentials) {
    userService.registerUser(
        credentials.getUsername(),
        credentials.getPassword(),
        credentials.getCryptoIdentity().getSessionId(),
        credentials.getCryptoIdentity().getPublicKey().toByteArray());
    return Welcome.newBuilder()
        // In the future, new roles may be defined here
        .setJwt(jwtService.createJwt(credentials.getUsername(), List.of("USER")))
        .setServerCertificate(ByteString.copyFrom(keychain.getCertificate()))
        .build();
  }

  @POST()
  @Path("/register_token")
  @RolesAllowed("USER")
  @Consumes(MediaType.APPLICATION_PROTOBUF)
  public void registerToken(@Context ContainerRequestContext requestContext, Token token) {
    String username = (String) requestContext.getProperty("username");
    userService.registerToken(username, token.getRegistrationToken());
  }
}
