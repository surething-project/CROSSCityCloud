package pt.ulisboa.tecnico.cross;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class TestingClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestingClient.class);
  public static final Properties PROPS = new Properties();

  static {
    try {
      PROPS.load(
          TestingClient.class.getResourceAsStream(
              String.format("/%s.properties", TestingClient.class.getSimpleName())));
    } catch (IOException e) {
      LOGGER.error("Failed to read properties.");
      System.exit(1);
    }
  }
}
