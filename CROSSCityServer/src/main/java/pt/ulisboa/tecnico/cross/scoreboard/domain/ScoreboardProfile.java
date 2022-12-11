package pt.ulisboa.tecnico.cross.scoreboard.domain;

import pt.ulisboa.tecnico.cross.contract.gamification.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardProfile {

  private final String username;
  private final int position;
  private final int score;
  private final List<String> ownedBadges;

  public ScoreboardProfile(String username, int position, int score, List<String> ownedBadges) {
    this.username = username;
    this.position = position;
    this.score = score;
    this.ownedBadges = ownedBadges;
  }

  public ScoreboardProfile(String username, int position, int score) {
    this(username, position, score, new ArrayList<>());
  }

  public String getUsername() {
    return username;
  }

  public void addAllOwnedBadges(List<String> ownedBadges) {
    this.ownedBadges.addAll(ownedBadges);
  }

  public Scoreboard.ScoreboardProfile toProtobuf() {
    return Scoreboard.ScoreboardProfile.newBuilder()
        .setUsername(username)
        .setPosition(position)
        .setScore(score)
        .addAllOwnedBadges(ownedBadges)
        .build();
  }
}
