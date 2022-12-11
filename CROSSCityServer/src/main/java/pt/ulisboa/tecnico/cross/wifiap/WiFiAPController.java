package pt.ulisboa.tecnico.cross.wifiap;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import pt.ulisboa.tecnico.cross.contract.WiFiAPOuterClass;
import pt.ulisboa.tecnico.cross.utils.MediaType;
import pt.ulisboa.tecnico.cross.wifiap.domain.WiFiAP;

import java.util.List;
import java.util.stream.Collectors;

@Path("/wifiap")
public class WiFiAPController {

  @Inject WiFiAPService wiFiAPService;

  @GET
  @RolesAllowed("ADMIN")
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public WiFiAPOuterClass.GetWiFiAPsResponse getWiFiAPs() {
    List<WiFiAPOuterClass.WiFiAP> wiFiAPs =
        wiFiAPService.getTriggerWiFiAPs().stream()
            .map(WiFiAP::toProtobuf)
            .collect(Collectors.toList());
    return WiFiAPOuterClass.GetWiFiAPsResponse.newBuilder().addAllWiFiAPs(wiFiAPs).build();
  }

  @GET
  @Path("/{bssid}")
  @RolesAllowed("ADMIN")
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public WiFiAPOuterClass.WiFiAP getWiFiAP(@PathParam("bssid") String bssid) {
    return wiFiAPService.getTriggerWiFiAP(bssid).toProtobuf();
  }
}
