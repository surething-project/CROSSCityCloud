package pt.ulisboa.tecnico.cross.badging.domain;

import pt.ulisboa.tecnico.cross.contract.gamification.Badging;

import java.util.HashMap;
import java.util.Map;

public class Badge {

  private final String id;
  private final int position;
  private final String imageUrl;
  private final String mainLocale;
  private final Map<String, String> names;
  private final Map<String, String> quests;
  private final Map<String, String> achievements;

  public Badge(String id, int position, String imageUrl, String mainLocale) {
    this.id = id;
    this.position = position;
    this.imageUrl = imageUrl;
    this.mainLocale = mainLocale;
    this.names = new HashMap<>();
    this.quests = new HashMap<>();
    this.achievements = new HashMap<>();
  }

  public String getId() {
    return id;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getMainLocale() {
    return mainLocale;
  }

  public Map<String, String> getNames() {
    return names;
  }

  public Map<String, String> getQuests() {
    return quests;
  }

  public Map<String, String> getAchievements() {
    return achievements;
  }

  public void putName(String locale, String name) {
    names.put(locale, name);
  }

  public void putQuest(String locale, String quest) {
    quests.put(locale, quest);
  }

  public void putAchievement(String locale, String achievement) {
    achievements.put(locale, achievement);
  }

  public Badging.Badge toProtobuf() {
    return Badging.Badge.newBuilder()
        .setId(id)
        .setPosition(position)
        .setImageURL(imageUrl)
        .setMainLocale(mainLocale)
        .putAllNames(names)
        .putAllQuests(quests)
        .putAllAchievements(achievements)
        .build();
  }
}
