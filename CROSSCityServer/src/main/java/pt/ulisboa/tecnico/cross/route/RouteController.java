package pt.ulisboa.tecnico.cross.route;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import pt.ulisboa.tecnico.cross.contract.RouteOuterClass;
import pt.ulisboa.tecnico.cross.route.domain.Route;
import pt.ulisboa.tecnico.cross.utils.MediaType;

import java.util.List;
import java.util.stream.Collectors;

@Path("/route")
public class RouteController {

  @Inject private RouteService routeService;

  @GET
  @PermitAll
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public RouteOuterClass.GetRoutesResponse getRoutes() {
    List<RouteOuterClass.Route> routes =
        routeService.getRoutes().stream().map(Route::toProtobuf).collect(Collectors.toList());
    return RouteOuterClass.GetRoutesResponse.newBuilder().addAllRoutes(routes).build();
  }

  @GET
  @Path("/{id}")
  @PermitAll
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public RouteOuterClass.Route getRoute(@PathParam("id") String id) {
    return routeService.getRoute(id).toProtobuf();
  }
}
