package pt.ulisboa.tecnico.cross.badging;

import pt.ulisboa.tecnico.cross.badging.domain.Badge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BadgingRepository {

  public static BadgingRepository get() {
    return BadgingRepositoryHolder.INSTANCE;
  }

  private BadgingRepository() {}

  private Badge createBadgeFromResultSet(ResultSet rs) throws SQLException {
    Badge badge =
        new Badge(
            rs.getString("id"),
            rs.getInt("position"),
            rs.getString("image_url"),
            rs.getString("main_locale"));
    while (!rs.isAfterLast() && badge.getId().equals(rs.getString("id"))) {
      String locale = rs.getString("lang");

      if (!badge.getNames().containsKey(locale)) {
        badge.putName(locale, rs.getString("name"));
      }
      if (!badge.getQuests().containsKey(locale)) {
        badge.putQuest(locale, rs.getString("quest"));
      }
      if (!badge.getAchievements().containsKey(locale)) {
        badge.putAchievement(locale, rs.getString("achievement"));
      }
      rs.next();
    }
    return badge;
  }

  public List<Badge> getBadges(Connection con) throws SQLException {
    List<Badge> badges = new ArrayList<>();
    PreparedStatement stmt =
        con.prepareStatement(
            "SELECT b.*, bl.lang, bl.name, bl.quest, bl.achievement "
                + "FROM badge b "
                + "INNER JOIN badge_lang bl ON b.id = bl.badge_id "
                + "ORDER BY b.id");

    ResultSet rs = stmt.executeQuery();
    rs.next();
    while (!rs.isAfterLast()) badges.add(createBadgeFromResultSet(rs));
    stmt.close();
    return badges;
  }

  public List<String> getOwnedBadges(Connection con, String username) throws SQLException {
    List<String> ownedBadges = new ArrayList<>();
    PreparedStatement stmt =
        con.prepareStatement("SELECT badge_id FROM user_badge WHERE username = ?");
    stmt.setString(1, username);

    ResultSet rs = stmt.executeQuery();
    while (rs.next()) ownedBadges.add(rs.getString("badge_id"));
    stmt.close();
    return ownedBadges;
  }

  public void awardBadge(Connection con, String username, String awardedBadge) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement("INSERT INTO user_badge (username, badge_id) VALUES (?, ?)");
    stmt.setString(1, username);
    stmt.setString(2, awardedBadge);
    stmt.executeUpdate();
    stmt.close();
  }

  private static class BadgingRepositoryHolder {
    private static final BadgingRepository INSTANCE = new BadgingRepository();
  }
}
