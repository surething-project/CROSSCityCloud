package pt.ulisboa.tecnico.cross;

import java.io.IOException;
import java.util.logging.Logger;

public class CROSSClient {

  enum Type {
    SIMULATION,
    FILE
  }

  enum Mode {
    SCAVENGER,
    VOLATILE
  }

  private static final Logger LOGGER = Logger.getLogger(CROSSClient.class.getName());

  public static void main(String[] args) {
    if (args.length < 2) {
      throw new IllegalArgumentException("Insufficient arguments.");
    }

    Type type = Type.valueOf(args[0]);

    if (type == Type.FILE) {
      String relative_path_to_dataset = args[1];
      String timestamp_arg = args[2];
      String poi_arg = args[3];
      String user_id = args[4];
      long min_seconds_diff = Long.parseLong(args[5]);
      Mode mode = Mode.valueOf(args[6]);

      try {
        DatasetFileReader datasetFileReader = new DatasetFileReader(poi_arg, user_id, min_seconds_diff, mode);
        datasetFileReader.playbackDatasetFile(relative_path_to_dataset + "/" + timestamp_arg + "/" + poi_arg + "/" + user_id + ".txt");
      } catch (IOException e) {
        LOGGER.severe(e.getMessage());
      }
    } else if (type == Type.SIMULATION) {
      String poi_arg = args[1];
      int number_of_aps = Integer.parseInt(args[2]);

      try {
        ObsSimulator obsSimulator = new ObsSimulator();
        obsSimulator.simulate(poi_arg, "alice", number_of_aps);
      } catch (IOException e) {
        LOGGER.severe(e.getMessage());
      }
    }
  }
}
