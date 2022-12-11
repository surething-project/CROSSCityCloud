package pt.ulisboa.tecnico.cross;

import com.google.common.util.concurrent.AtomicDouble;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.simulation.Simulation;
import pt.ulisboa.tecnico.cross.simulation.User;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pt.ulisboa.tecnico.cross.TestingClient.PROPS;

public class PeerEndorsementsValidationTests {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(PeerEndorsementsValidationTests.class);
  private static final int MAX_NUMBER_OF_SIMULTANEOUS_SUBMISSIONS =
      Integer.parseInt(PROPS.getProperty("MAX_NUMBER_OF_SIMULTANEOUS_SUBMISSIONS"));
  private static final int MAX_NUMBER_OF_WITNESSES =
      Integer.parseInt(PROPS.getProperty("MAX_NUMBER_OF_WITNESSES"));
  private static final int NUMBER_OF_WITNESSES =
      Integer.parseInt(PROPS.getProperty("NUMBER_OF_WITNESSES"));
  private static final int NUMBER_OF_PW_COLLUSIONS =
      Integer.parseInt(PROPS.getProperty("NUMBER_OF_PW_COLLUSIONS"));
  private static final int NUMBER_OF_PREVIOUS_VISITS =
      Integer.parseInt(PROPS.getProperty("NUMBER_OF_PREVIOUS_VISITS"));
  private static final int NUMBER_OF_FORGED_ENDORSEMENTS =
      Integer.parseInt(PROPS.getProperty("NUMBER_OF_FORGED_ENDORSEMENTS"));
  private static final String TEST_ROUTE_ID = PROPS.getProperty("TEST_ROUTE_ID");
  private static final String TEST_POI_ID = PROPS.getProperty("TEST_POI_ID");
  private static final Random random = new Random();

  @Disabled
  @Test
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
    Map<Integer, Double> results = new HashMap<>();

    for (int S = 1; S <= MAX_NUMBER_OF_SIMULTANEOUS_SUBMISSIONS; S++) {
      AtomicDouble timer = new AtomicDouble(0);
      for (int i = 1; i <= iterations; i++) {
        LOGGER.debug(join(S, i));
        for (int T = 1; T <= S; T++) {
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
              } catch (InterruptedException e) {
                LOGGER.error("The thread has been interrupted.", e);
              }
            });
        simulations.clear();
      }
      results.put(S, timer.get() / (S * iterations));
      try {
        // Let the server cool down a bit.
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
      } catch (InterruptedException ignored) {
      }
    }
    saveResults("VVTC", results);
  }

  @Disabled
  @Test
  // Evolution of the visit validation time with the number of witnesses.
  void visitValidationTimeWitnesses() {
    String testId = join("VVTW", random.nextInt());
    LOGGER.info("Test identifier: " + testId);
    int iterations = 10;
    boolean forged = false;
    Map<Integer, Double> results = new HashMap<>();

    for (int W = 1; W <= MAX_NUMBER_OF_WITNESSES; W++) {
      User prover = new User();
      List<User> witnesses = Stream.generate(User::new).limit(W).collect(Collectors.toList());
      double timer = 0;
      for (int i = 1; i <= iterations; i++) {
        LOGGER.debug(join(W, i));
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
      results.put(W, timer / iterations);
    }
    saveResults("VVTW", results);
  }

  @Disabled
  @Test
  // W provers collude with W witnesses C times.
  void proverWitnessCollusions() {
    String testId = join("PWC", random.nextInt());
    LOGGER.info("Test identifier: " + testId);
    boolean forged = false;

    for (int W = 1; W <= NUMBER_OF_WITNESSES; W++) {
      User prover = new User();
      List<User> witnesses = Stream.generate(User::new).limit(W).collect(Collectors.toList());

      for (int C = 1; C <= NUMBER_OF_PW_COLLUSIONS; C++) {
        String testInstanceId = join(testId, W, C);
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
    }
  }

  @Disabled
  @Test
  // The prover makes visits with W different witnesses who already had VV verified visits.
  void endorsementWeightTarget() {
    String testId = join("EWT", random.nextInt());
    LOGGER.info("Test identifier: " + testId);
    boolean forged = false;
    int numberOfGoodBehaviorReportsPerVisit = 6;

    for (int V = 0; V <= NUMBER_OF_PREVIOUS_VISITS; V++) {
      User prover = new User();
      List<User> witnesses = Stream.generate(User::new).limit(1).collect(Collectors.toList());

      // Certifying visits to report good behavior.
      for (int R = 0; R < V * numberOfGoodBehaviorReportsPerVisit; R++) {
        List<User> witnessWitnesses =
            Stream.generate(User::new).limit(NUMBER_OF_WITNESSES).collect(Collectors.toList());
        for (User witness : witnesses) {
          String testInstanceId = join('#', testId, V, R, witnesses.indexOf(witness));
          Simulation.get()
              .simulate(
                  witness,
                  witnessWitnesses,
                  TEST_ROUTE_ID,
                  TEST_POI_ID,
                  testInstanceId,
                  testInstanceId,
                  forged);
        }
      }

      String testInstanceId = join(testId, V);
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
  }

  @Disabled
  @Test
  // The prover forges E endorsements.
  void forgedEndorsements() {
    User prover = new User();
    String testId = join("FE", random.nextInt());
    LOGGER.info("Test identifier: " + testId);
    boolean forged = true;

    for (int E = 1; E <= NUMBER_OF_FORGED_ENDORSEMENTS; E++) {
      List<User> witnesses = Stream.generate(User::new).limit(E).collect(Collectors.toList());
      String testInstanceId = join(testId, E);
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
  }

  @Disabled
  @Test
  void simpleTest() {
    User prover = new User();
    String testId = join("ST", random.nextInt());
    LOGGER.info("Test identifier: " + testId);
    boolean forged = false;

    List<User> witnesses = Stream.generate(User::new).limit(5).collect(Collectors.toList());
    Simulation.get()
        .simulate(prover, witnesses, TEST_ROUTE_ID, TEST_POI_ID, testId, testId, forged);
  }

  private String join(Object... elements) {
    return Arrays.stream(elements).map(String::valueOf).collect(Collectors.joining(" "));
  }

  private void saveResults(String filename, Map<Integer, Double> results) {
    try (FileWriter fileWriter = new FileWriter(filename + ".txt")) {
      PrintWriter printWriter = new PrintWriter(fileWriter);
      results.keySet().stream()
          .sorted()
          .forEach(k -> printWriter.println(k + " " + results.get(k)));
      printWriter.close();
    } catch (IOException e) {
      LOGGER.error("Error writing results to file.", e);
    }
  }
}
