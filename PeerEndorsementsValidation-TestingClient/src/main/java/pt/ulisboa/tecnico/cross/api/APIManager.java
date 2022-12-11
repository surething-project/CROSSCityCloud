package pt.ulisboa.tecnico.cross.api;

import pt.ulisboa.tecnico.cross.api.trip.TripAPI;
import pt.ulisboa.tecnico.cross.api.user.UserAPI;

import static pt.ulisboa.tecnico.cross.TestingClient.PROPS;

public class APIManager {

  public final String SERVER_BASE_URL = PROPS.getProperty("SERVER_BASE_URL");

  private final TripAPI tripAPI;
  private final UserAPI userAPI;

  public static APIManager get() {
    return APIManagerHolder.INSTANCE;
  }

  private APIManager() {
    tripAPI = new TripAPI();
    userAPI = new UserAPI();
  }

  public TripAPI getTripAPI() {
    return tripAPI;
  }

  public UserAPI getUserAPI() {
    return userAPI;
  }

  private static class APIManagerHolder {
    private static final APIManager INSTANCE = new APIManager();
  }
}
