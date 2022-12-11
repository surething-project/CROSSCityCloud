package pt.ulisboa.tecnico.cross.repository;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SureReputeRepositoryChanger {
  private final Connection con;

  public SureReputeRepositoryChanger() throws SQLException {
    String url =
        String.format("jdbc:postgresql://%s:%d/%s", "localhost", 5432, "surereputeserver1");
    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl(url);
    ds.setUsername("surereputeserver1");
    ds.setPassword("surereputeserver1");
    con = ds.getConnection();
  }

  public void storePseudonym(String pseudonym, Double positiveBehavior, Double negativeBehavior)
      throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement(
            "INSERT INTO pseudonym_score (pseudonym, positive_behavior, negative_behavior) VALUES (?, ?, ?) "
                + "ON CONFLICT (pseudonym) "
                + "DO UPDATE SET positive_behavior = ?, negative_behavior = ?");
    stmt.setString(1, pseudonym);
    stmt.setDouble(2, positiveBehavior);
    stmt.setDouble(3, negativeBehavior);
    stmt.setDouble(4, positiveBehavior);
    stmt.setDouble(5, negativeBehavior);
    stmt.executeUpdate();
    stmt.close();
  }
}
