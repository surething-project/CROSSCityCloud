package pt.ulisboa.tecnico.cross.dataset;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.error.APIException;

import java.sql.Connection;
import java.sql.SQLException;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.getDb;

@ApplicationScoped
public class DatasetService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetService.class);
  private final DatasetRepository datasetRepository = DatasetRepository.get();

  public String getLatestDatasetVersion() {
    try (Connection con = getDb()) {
      return datasetRepository.getLatestDatasetVersion(con);
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new APIException(INTERNAL_SERVER_ERROR, "Database error.");
    }
  }
}
