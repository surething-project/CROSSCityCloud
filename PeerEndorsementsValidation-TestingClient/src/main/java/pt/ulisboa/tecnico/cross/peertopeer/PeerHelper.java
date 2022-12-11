package pt.ulisboa.tecnico.cross.peertopeer;

import java.security.SecureRandom;
import java.util.BitSet;

import static pt.ulisboa.tecnico.cross.TestingClient.PROPS;

// https://www.overleaf.com/read/hsgrgwmcmhnr
public class PeerHelper {

  public final int N_CHALLENGE_ITERATIONS =
      Integer.parseInt(PROPS.getProperty("N_CHALLENGE_ITERATIONS"));
  private final SecureRandom random = new SecureRandom();

  public static PeerHelper get() {
    return PeerHelperHolder.INSTANCE;
  }

  /*********************
   * Auxiliary methods *
   *********************/

  BitSet randomValue() {
    BitSet value = new BitSet(N_CHALLENGE_ITERATIONS);
    for (int i = 0; i < N_CHALLENGE_ITERATIONS; i++) value.set(i, random.nextBoolean());
    return value;
  }

  private static class PeerHelperHolder {
    private static final PeerHelper INSTANCE = new PeerHelper();
  }
}
