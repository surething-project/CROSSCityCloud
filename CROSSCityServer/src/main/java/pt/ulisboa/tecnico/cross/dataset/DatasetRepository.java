package pt.ulisboa.tecnico.cross.dataset;

import pt.ulisboa.tecnico.cross.error.APIException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class DatasetRepository {

  public static DatasetRepository get() {
    return DatasetRepositoryHolder.INSTANCE;
  }

  private DatasetRepository() {}

  public String getLatestDatasetVersion(Connection con) throws SQLException {
    PreparedStatement stmt = con.prepareStatement("SELECT * FROM dataset WHERE id = 'latest'");
    ResultSet rs = stmt.executeQuery();
    if (rs.next()) return rs.getString("version");
    else throw new APIException(INTERNAL_SERVER_ERROR, "Dataset not defined.");
  }

  private static class DatasetRepositoryHolder {
    private static final DatasetRepository INSTANCE = new DatasetRepository();
  }
}
