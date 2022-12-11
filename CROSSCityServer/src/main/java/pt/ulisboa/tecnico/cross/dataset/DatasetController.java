package pt.ulisboa.tecnico.cross.dataset;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import pt.ulisboa.tecnico.cross.contract.Dataset.GetLatestDatasetResponse;
import pt.ulisboa.tecnico.cross.utils.MediaType;

@Path("/dataset")
public class DatasetController {

  @Inject private DatasetService datasetService;

  @GET
  @PermitAll
  @Produces(MediaType.APPLICATION_PROTOBUF)
  public GetLatestDatasetResponse getLatestDataset() {
    return GetLatestDatasetResponse.newBuilder()
        .setVersion(datasetService.getLatestDatasetVersion())
        .build();
  }
}
