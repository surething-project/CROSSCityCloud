package pt.ulisboa.tecnico.cross.error;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.ulisboa.tecnico.cross.utils.MediaType;

public class APIException extends WebApplicationException {

  public APIException(Status status, String message) {
    super(Response.status(status).type(MediaType.TEXT_PLAIN).entity(message).build());
  }
}
