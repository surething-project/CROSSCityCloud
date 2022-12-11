package pt.ulisboa.tecnico.cross.api.trip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pt.ulisboa.tecnico.cross.api.APIClient;
import pt.ulisboa.tecnico.cross.api.APIManager;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass.CreateOrUpdateTripResponse;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass.Trip;

public class TripAPI {

  private static final Logger LOGGER = LoggerFactory.getLogger(TripAPI.class);

  public long createOrUpdateTrip(String jwt, Trip trip) {
    RestTemplate restTemplate = APIClient.getRestTemplate();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", jwt);
    HttpEntity<Trip> httpEntity = new HttpEntity<>(trip, httpHeaders);

    try {
      long beginning = System.nanoTime();
      CreateOrUpdateTripResponse response =
          restTemplate.postForObject(
              APIManager.get().SERVER_BASE_URL + "trip",
              httpEntity,
              CreateOrUpdateTripResponse.class);
      long ending = System.nanoTime();
      assert response != null;
      return ending - beginning;
    } catch (RestClientException e) {
      LOGGER.error("Failed to submit visit.", e);
      return -1;
    }
  }
}
