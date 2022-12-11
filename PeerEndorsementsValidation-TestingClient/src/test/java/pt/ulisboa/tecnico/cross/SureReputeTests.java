package pt.ulisboa.tecnico.cross;

import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.math3.util.Precision;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.repository.SureReputeRepositoryChanger;
import pt.ulisboa.tecnico.cross.simulation.Simulation;
import pt.ulisboa.tecnico.cross.simulation.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pt.ulisboa.tecnico.cross.TestingClient.PROPS;

public class SureReputeTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(SureReputeTests.class);
  private static final int MAX_NUMBER_OF_WITNESSES =
      Integer.parseInt(PROPS.getProperty("MAX_NUMBER_OF_WITNESSES"));
  private static final String TEST_ROUTE_ID = PROPS.getProperty("TEST_ROUTE_ID");
  private static final String TEST_POI_ID = PROPS.getProperty("TEST_POI_ID");
  private static final Random random = new Random();

  @Test
  @Disabled
  // Evolution of the visit validation time with the number of witnesses.
  void visitValidationTimeWitnesses() {
    String testId = join("VVTW", random.nextLong());
    LOGGER.info("Test identifier: " + testId);
    int iterations = 10;
    boolean forged = false;

    for (int W = 1; W <= MAX_NUMBER_OF_WITNESSES; W++) {
      User prover = new User();
      List<User> witnesses = Stream.generate(User::new).limit(W).collect(Collectors.toList());
      double timer = 0;
      for (int i = 1; i <= iterations; i++) {
        LOGGER.debug(join(i, W));
        String testInstanceId = join(testId, i, W);
        timer +=
            Simulation.get()
                .simulate(
                    prover,
                    witnesses,
                    TEST_ROUTE_ID,
                    TEST_POI_ID,
                    testInstanceId,
                    testInstanceId,
                    forged);
      }
      System.out.println(timer / iterations);
    }
  }

  @Test
  @Disabled
  // Evolution of the visit validation time with concurrency
  void visitValidationTimeConcurrency() {
    String testId = join("VVTC", random.nextInt());
    LOGGER.info("Test identifier: " + testId);
    int iterations = 10;
    boolean forged = false;
    User prover = new User();
    List<User> witnesses =
        Stream.generate(User::new).limit(MAX_NUMBER_OF_WITNESSES / 2).collect(Collectors.toList());
    List<Thread> simulations = new ArrayList<>();
    for (int S = 1; S <= 15; S += 1) {
      AtomicDouble timer = new AtomicDouble(0);
      for (int i = 1; i <= iterations; i++) {
        for (int T = 1; T <= S; T++) {
          LOGGER.debug(join(i, S, T));
          String testInstanceId = join(testId, i, S, T);
          simulations.add(
              new Thread(
                  () ->
                      timer.addAndGet(
                          Simulation.get()
                              .simulate(
                                  prover,
                                  witnesses,
                                  TEST_ROUTE_ID,
                                  TEST_POI_ID,
                                  testInstanceId,
                                  testInstanceId,
                                  forged))));
        }
        simulations.forEach(Thread::start);
        simulations.forEach(
            simulation -> {
              try {
                simulation.join();
              } catch (InterruptedException ignored) {
              }
            });
        simulations.clear();
        try {
          // Let the server cool down a bit.
          Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        } catch (InterruptedException ignored) {
        }
      }
      System.out.println(timer.get() / (S * iterations));
    }
  }

  @Test
  @Disabled
  // Tests for Scores
  void visitValidationScores() throws SQLException {
    String testId = join("VVTC", random.nextInt());
    SureReputeRepositoryChanger rs = new SureReputeRepositoryChanger();
    LOGGER.info("Test identifier: " + testId);
    boolean forged = false;
    // Scores: 0, 0.35, 0.5
    Double[] proverGoodBehavior = {0.0, 0.615385, 1.0};
    Double[] proverBadBehavior = {1000.0, 2.0, 1.0};
    // Scores: 0, 0.25, 0.35, 0.5, 0.75, 1
    Double[] witnessGoodBehavior = {0.0, 0.0, 0.615385, 1.0, 5.0, 1000.0};
    Double[] witnessBadBehavior = {1000.0, 2.0, 2.0, 1.0, 5.0, 0.0};
    for (int i = 0; i < proverGoodBehavior.length; i++) {
      double proverScore =
          (proverGoodBehavior[i] + 1) / (proverGoodBehavior[i] + proverBadBehavior[i] + 2);
      for (int j = 0; j < witnessGoodBehavior.length; j++) {
        for (int S = 1; S <= 15; S += 1) {
          double witnessScore =
              (witnessGoodBehavior[j] + 1) / (witnessBadBehavior[j] + witnessGoodBehavior[j] + 2);
          LOGGER.debug(join(S, Precision.round(proverScore, 2), Precision.round(witnessScore, 2)));
          String testInstanceId = join(testId, i, j, S);
          User prover = new User();
          rs.storePseudonym(prover.getUsername(), proverGoodBehavior[i], proverBadBehavior[i]);
          List<User> witnesses = Stream.generate(User::new).limit(S).collect(Collectors.toList());
          for (User witness : witnesses) {
            rs.storePseudonym(witness.getUsername(), witnessGoodBehavior[j], witnessBadBehavior[j]);
          }
          if (Simulation.get()
                  .simulate(
                      prover,
                      witnesses,
                      TEST_ROUTE_ID,
                      TEST_POI_ID,
                      testInstanceId,
                      testInstanceId,
                      forged)
              == -1) System.err.print("Invalid, ");
          else System.err.print("Valid, ");
        }
        System.err.println("");
      }
    }
  }

  private String join(Object... elements) {
    return Arrays.stream(elements).map(String::valueOf).collect(Collectors.joining(" "));
  }
}
