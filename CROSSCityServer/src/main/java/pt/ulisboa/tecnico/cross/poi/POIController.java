package pt.ulisboa.tecnico.cross.poi;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import pt.ulisboa.tecnico.cross.contract.POIOuterClass;
import pt.ulisboa.tecnico.cross.contract.POIOuterClass.GetPOIsResponse;
import pt.ulisboa.tecnico.cross.poi.domain.POI;
import pt.ulisboa.tecnico.cross.utils.MediaType;

import java.util.stream.Collectors;

@Path("/poi")
public class POIController {

  @Inject POIService poiService;

  @GET
  @PermitAll
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public GetPOIsResponse getPOIs() {
    return GetPOIsResponse.newBuilder()
        .addAllPOIs(poiService.getPOIs().stream().map(POI::toProtobuf).collect(Collectors.toList()))
        .build();
  }

  @GET
  @Path("/{id}")
  @PermitAll
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public POIOuterClass.POI getPOI(@PathParam("id") String id) {
    return poiService.getPOI(id).toProtobuf();
  }
}
