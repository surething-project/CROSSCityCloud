package pt.ulisboa.tecnico.cross;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

public class CROSSSignInSampler extends AbstractJavaSamplerClient {
  private final String SIGNIN_URL = "SigninUrl";
  private final String USER_ID = "UserId";

  private String signinUrl;
  private String userId;

  @Override
  public Arguments getDefaultParameters() {
    Arguments defaultParameters = new Arguments();
    defaultParameters.addArgument(SIGNIN_URL, "http://localhost:8080/v2/user/signin");
    defaultParameters.addArgument(USER_ID, "alice");
    return defaultParameters;
  }

  private void setParameters(JavaSamplerContext context) {
    signinUrl = context.getParameter(SIGNIN_URL);
    userId = context.getParameter(USER_ID);
  }

  @Override
  public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
    final SampleResult result = new SampleResult();
    setParameters(javaSamplerContext);
    APIClient apiClient = new APIClient();
    result.sampleStart();
    try {
      long startTime = System.currentTimeMillis();
      String jwt = apiClient.signin(signinUrl, userId);
      result.setLatency(System.currentTimeMillis() - startTime);
      result.setResponseCodeOK();
      result.setSuccessful(true);
      result.setResponseMessage(jwt);
    } catch (ResponseStatusException | RestClientException e) {
      result.setResponseCode("500");
      result.setSuccessful(false);
    }
    result.sampleEnd();
    return result;
  }
}
