package pt.ulisboa.tecnico.cross.scoreboard;

import pt.ulisboa.tecnico.cross.scoreboard.domain.ScoreboardProfile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreboardRepository {

  public static ScoreboardRepository get() {
    return ScoreboardRepositoryHolder.INSTANCE;
  }

  private ScoreboardRepository() {}

  public List<ScoreboardProfile> getScoreboard(Connection con, String username, String datepart)
      throws SQLException {
    List<ScoreboardProfile> scoreboard = new ArrayList<>();
    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT * FROM ("
                + "SELECT ROW_NUMBER() OVER (ORDER BY SUM(awarded_score) DESC, MAX(timestamp) DESC) AS position, username, SUM(awarded_score) AS score "
                + "FROM ("
                + "SELECT username, timestamp, awarded_score FROM user_score "
                + "UNION SELECT ? AS username, NOW() AS timestamp, 0 AS awarded_score) rewards "
                + (!datepart.isEmpty() ? "WHERE timestamp >= DATE_TRUNC(?, NOW()) " : "")
                + "GROUP BY username) scoreboard "
                + "WHERE position <= 50 OR username = ?");
    stmt.setString(1, username);
    if (!datepart.isEmpty()) stmt.setString(2, datepart);
    stmt.setString(datepart.isEmpty() ? 2 : 3, username);

    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
      scoreboard.add(
          new ScoreboardProfile(
              rs.getString("username"), rs.getInt("position"), rs.getInt("score")));
    }
    stmt.close();
    return scoreboard;
  }

  public void updateScore(Connection con, String username, int awardedScore) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement(
            "INSERT INTO user_score (username, timestamp, awarded_score) VALUES (?, ?, ?)");
    stmt.setString(1, username);
    stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
    stmt.setInt(3, awardedScore);
    stmt.executeUpdate();
    stmt.close();
  }

  private static class ScoreboardRepositoryHolder {
    private static final ScoreboardRepository INSTANCE = new ScoreboardRepository();
  }
}
