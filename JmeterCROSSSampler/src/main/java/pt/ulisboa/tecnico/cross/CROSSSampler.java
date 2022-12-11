package pt.ulisboa.tecnico.cross;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass;

public class CROSSSampler extends AbstractJavaSamplerClient {
  private final String TRIP_URL = "TripUrl";
  private final String JWT = "Jwt";

  private String tripUrl;
  private String jwt;

  @Override
  public Arguments getDefaultParameters() {
    Arguments defaultParameters = new Arguments();
    defaultParameters.addArgument(TRIP_URL, "http://localhost:8080/v2/trip");
    defaultParameters.addArgument(JWT, "token");
    return defaultParameters;
  }

  private void setParameters(JavaSamplerContext context) {
    tripUrl = context.getParameter(TRIP_URL);
    jwt = context.getParameter(JWT);
  }

  @Override
  public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
    final SampleResult result = new SampleResult();
    setParameters(javaSamplerContext);
    APIClient apiClient = new APIClient();
    result.sampleStart();
    try {
      long startTime = System.currentTimeMillis();
      ResponseEntity<TripOuterClass.GetTripsResponse> response = apiClient.getTrips(tripUrl, jwt);
      result.setLatency(System.currentTimeMillis() - startTime);
      result.setContentType("application/x-protobuf");
      result.setResponseCodeOK();
      result.setSuccessful(true);
    } catch (ResponseStatusException | RestClientException e) {
      result.setResponseCode("500");
      result.setSuccessful(false);
    }
    result.sampleEnd();
    return result;
  }
}
