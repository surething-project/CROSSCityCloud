package pt.ulisboa.tecnico.cross.badging.services;

import pt.ulisboa.tecnico.cross.badging.BadgingRepository;
import pt.ulisboa.tecnico.cross.trip.TripRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BadgeAttribution {

  private final BadgingRepository badgingRepository = BadgingRepository.get();
  private final TripRepository tripRepository = TripRepository.get();

  private final int[] endorsementsAmount = new int[] {10, 100, 1000};
  private final int[] visitsAmount = new int[] {3, 15, 30};
  private final int[] routesAmount = new int[] {1};

  private final Function<Integer, String> endorsementsMapper = amount -> amount + "_endorsements";
  private final Function<Integer, String> visitsMapper = amount -> amount + "_visits";
  private final Function<Integer, String> routesMapper = amount -> amount + "_routes";

  public List<String> review(Connection con, String username) throws SQLException {
    List<String> ownedBadges = badgingRepository.getOwnedBadges(con, username);
    List<String> awardedBadges = new ArrayList<>();
    review(
        tripRepository.getNumberOfTestimonies(con, username),
        endorsementsAmount,
        endorsementsMapper,
        ownedBadges,
        awardedBadges);
    review(
        tripRepository.getNumberOfTraveledVisits(con, username),
        visitsAmount,
        visitsMapper,
        ownedBadges,
        awardedBadges);
    review(
        tripRepository.getNumberOfTraveledRoutes(con, username),
        routesAmount,
        routesMapper,
        ownedBadges,
        awardedBadges);
    return awardedBadges;
  }

  private void review(
      int amountObtained,
      int[] amounts,
      Function<Integer, String> mapper,
      List<String> ownedBadges,
      List<String> awardedBadges) {
    for (int amount : amounts) {
      if (amountObtained >= amount) {
        if (!ownedBadges.contains(mapper.apply(amount))) awardedBadges.add(mapper.apply(amount));
      } else break;
    }
  }
}
