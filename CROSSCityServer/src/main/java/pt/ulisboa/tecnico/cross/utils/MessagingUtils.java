package pt.ulisboa.tecnico.cross.utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.CROSSCityServer;
import pt.ulisboa.tecnico.cross.user.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class MessagingUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessagingUtils.class);
  private final UserRepository userRepository = UserRepository.get();

  public static MessagingUtils get() {
    return MessagingUtilsHolder.INSTANCE;
  }

  private MessagingUtils() {}

  public void init() throws IOException {
    InputStream credentialsStream =
        CROSSCityServer.class.getResourceAsStream(
            "/firebase/cross-city-4822f-firebase-adminsdk-dl16r-a8e54cdc50.json");
    if (credentialsStream != null) {
      FirebaseApp.initializeApp(
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(credentialsStream))
              .build());
    } else {
      LOGGER.error("File containing Firebase credentials not found.");
    }
  }

  public void notify(
      Connection con, String username, Notification notification, Map<String, String> data)
      throws SQLException {
    String token = userRepository.getUser(con, username).getRegistrationToken();
    if (token == null || token.isEmpty()) {
      LOGGER.warn("The user has not yet registered the messaging token.");
      return;
    }
    Message.Builder msg = Message.builder();
    if (notification != null) msg.setNotification(notification);
    if (data != null) msg.putAllData(data);
    msg.setToken(token).build();
    FirebaseMessaging.getInstance().sendAsync(msg.build());
    LOGGER.info("Notification sent to device with token: " + token);
  }

  private static class MessagingUtilsHolder {
    private static final MessagingUtils INSTANCE = new MessagingUtils();
  }
}
