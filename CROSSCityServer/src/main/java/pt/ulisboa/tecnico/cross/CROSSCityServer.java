package pt.ulisboa.tecnico.cross;

import com.zaxxer.hikari.HikariDataSource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import pt.ulisboa.tecnico.cross.badging.BadgingController;
import pt.ulisboa.tecnico.cross.badging.services.BadgingService;
import pt.ulisboa.tecnico.cross.dataset.DatasetController;
import pt.ulisboa.tecnico.cross.dataset.DatasetService;
import pt.ulisboa.tecnico.cross.payment.PaymentController;
import pt.ulisboa.tecnico.cross.payment.PaymentService;
import pt.ulisboa.tecnico.cross.poi.POIController;
import pt.ulisboa.tecnico.cross.poi.POIService;
import pt.ulisboa.tecnico.cross.providers.AuthenticationFilterProvider;
import pt.ulisboa.tecnico.cross.providers.ProtocolBufferMessageBodyProvider;
import pt.ulisboa.tecnico.cross.route.RouteController;
import pt.ulisboa.tecnico.cross.route.RouteService;
import pt.ulisboa.tecnico.cross.scavenger.ScavengerController;
import pt.ulisboa.tecnico.cross.scavenger.ScavengerService;
import pt.ulisboa.tecnico.cross.scoreboard.ScoreboardController;
import pt.ulisboa.tecnico.cross.scoreboard.ScoreboardService;
import pt.ulisboa.tecnico.cross.trip.TripController;
import pt.ulisboa.tecnico.cross.trip.services.TripService;
import pt.ulisboa.tecnico.cross.user.UserController;
import pt.ulisboa.tecnico.cross.user.UserService;
import pt.ulisboa.tecnico.cross.utils.JwtService;
import pt.ulisboa.tecnico.cross.utils.LCTService;
import pt.ulisboa.tecnico.cross.utils.MessagingUtils;
import pt.ulisboa.tecnico.cross.wifiap.WiFiAPController;
import pt.ulisboa.tecnico.cross.wifiap.WiFiAPService;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.LogManager;

public class CROSSCityServer {

  public static final int VERSION = 2;
  public static final Properties PROPS = new Properties();
  private static final Logger LOGGER = LoggerFactory.getLogger(CROSSCityServer.class);
  private static HikariDataSource ds;

  public static Connection getDb() throws SQLException {
    return ds.getConnection();
  }

  public static void main(String[] args) throws IOException {
    configureLog();
    PROPS.load(CROSSCityServer.class.getResourceAsStream("/CROSSCityServer.properties"));
    MessagingUtils.get().init();

    setConnectionPool(
        System.getenv("CROSS_DB_CONNECTION"),
        System.getenv("CROSS_DB_NAME"),
        System.getenv("CROSS_DB_USER"),
        PROPS.getProperty("DB_PWD"));

    final HttpServer server =
        startServer(URI.create(String.format("%s/v%d", System.getenv("CROSS_SERVER"), VERSION)));
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  server.shutdown();
                  ds.close();
                }));
    LOGGER.info("CROSS Server has started! To stop it, press CTRL+C.");
    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      LOGGER.error(e.getMessage());
    }
  }

  private static void setConnectionPool(String conn, String name, String user, String pwd) {
    String url = String.format("jdbc:postgresql://%s:5432/%s", conn, name);
    ds = new HikariDataSource();
    ds.setJdbcUrl(url);
    ds.setUsername(user);
    ds.setPassword(pwd);
  }

  private static HttpServer startServer(URI uri) {
    return GrizzlyHttpServerFactory.createHttpServer(uri, getResourceConfig());
  }

  private static ResourceConfig getResourceConfig() {
    ResourceConfig config = new ResourceConfig();
    config.registerClasses(
        AuthenticationFilterProvider.class,
        BadgingController.class,
        DatasetController.class,
        POIController.class,
        PaymentController.class,
        ProtocolBufferMessageBodyProvider.class,
        RouteController.class,
        ScavengerController.class,
        ScoreboardController.class,
        TripController.class,
        UserController.class,
        WiFiAPController.class);

    config.register(
        new AbstractBinder() {
          @Override
          protected void configure() {
            bindAsContract(BadgingService.class);
            bindAsContract(DatasetService.class);
            bindAsContract(JwtService.class);
            bindAsContract(LCTService.class);
            bindAsContract(POIService.class);
            bindAsContract(PaymentService.class);
            bindAsContract(RouteService.class);
            bindAsContract(ScavengerService.class);
            bindAsContract(ScoreboardService.class);
            bindAsContract(TripService.class);
            bindAsContract(UserService.class);
            bindAsContract(WiFiAPService.class);
          }
        });
    return config;
  }

  private static void configureLog() {
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.install();
  }
}
