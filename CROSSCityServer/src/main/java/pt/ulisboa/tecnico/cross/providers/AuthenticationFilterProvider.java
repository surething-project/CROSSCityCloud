package pt.ulisboa.tecnico.cross.providers;

import io.jsonwebtoken.Claims;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import pt.ulisboa.tecnico.cross.utils.JwtService;

import java.lang.reflect.Method;
import java.util.*;

import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static pt.ulisboa.tecnico.cross.utils.JwtService.ROLES_CLAIM;

@Provider
public class AuthenticationFilterProvider implements ContainerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  @Context private ResourceInfo resourceInfo;
  @Inject private JwtService jwtService;

  @SuppressWarnings("unchecked")
  @Override
  public void filter(ContainerRequestContext requestContext) {

    Method method = resourceInfo.getResourceMethod();

    if (method.isAnnotationPresent(PermitAll.class)) return;
    if (method.isAnnotationPresent(DenyAll.class)) {
      requestContext.abortWith(
          Response.status(FORBIDDEN).entity("Access blocked for all users!").build());
      return;
    }

    List<String> auth = requestContext.getHeaders().get(AUTHORIZATION_HEADER);
    if (auth == null || auth.isEmpty()) {
      requestContext.abortWith(
          Response.status(UNAUTHORIZED).entity("Authorization header is missing.").build());
      return;
    }

    String jwt = auth.get(0);
    Claims claims = jwtService.decodeJwt(jwt).getBody();

    if (new Date().after(claims.getExpiration())) {
      requestContext.abortWith(
          Response.status(UNAUTHORIZED).entity("The JWT has expired.").build());
      return;
    }

    String username = claims.getSubject();
    List<String> roles = (List<String>) claims.get(ROLES_CLAIM);

    requestContext.setProperty("username", username);
    requestContext.setProperty("roles", roles);

    if (method.isAnnotationPresent(RolesAllowed.class)) {
      Set<String> rolesAllowed =
          new HashSet<>(Arrays.asList(method.getAnnotation(RolesAllowed.class).value()));
      rolesAllowed.retainAll(roles);

      if (rolesAllowed.isEmpty()) {
        requestContext.abortWith(
            Response.status(UNAUTHORIZED)
                .entity("You do not have permissions to access this resource.")
                .build());
      }
    }
  }
}
