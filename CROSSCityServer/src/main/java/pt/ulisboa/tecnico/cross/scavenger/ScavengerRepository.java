package pt.ulisboa.tecnico.cross.scavenger;

import com.google.cloud.bigquery.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.trip.domain.Visit;
import pt.ulisboa.tecnico.cross.utils.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static pt.ulisboa.tecnico.cross.CROSSCityServer.PROPS;

public class ScavengerRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScavengerRepository.class);

  private final BigQuery bigquery;
  private final String datasetId;
  private final String tablePrefix;
  private final DateFormat tableIdDateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private final String VOLATILE_SET_TABLE_PREFIX = "volatile_";
  private final String STABLE_SET_PERIOD_TABLE_PREFIX = "stable_";
  private final String STABLE_SET_EPOCH_TABLE_PREFIX = "stable_set_";

  public static ScavengerRepository get() {
    return ScavengerRepositoryHolder.INSTANCE;
  }

  private ScavengerRepository() {
    bigquery = BigQueryOptions.getDefaultInstance().getService();
    datasetId = PROPS.getProperty("DATASET_ID");
    tablePrefix = PROPS.getProperty("PROJECT_ID") + "." + datasetId + ".";
  }

  private Set<String> getBssidsFromResult(TableResult result) {
    Set<String> bssids = new HashSet<>();

    result.iterateAll().forEach(rows -> bssids.add(rows.get("bssid").getStringValue()));

    return bssids;
  }

  private String getBssidSetQueryString(
      String tablePath,
      String tableUnion,
      String poiId,
      String claimTimeLower,
      String claimTimeUpper,
      String stableSetEpochTablePath,
      int countThreshold,
      double percentile,
      boolean isVolatileSet) {
    String tablePathClause = isVolatileSet ? "`" + tablePath + "`" : tableUnion;
    String timeBoundClause =
        isVolatileSet
            ? " AND start_time >= TIMESTAMP(\""
                + claimTimeLower
                + "\") AND end_time <= TIMESTAMP(\""
                + claimTimeUpper
                + "\") "
            : " ";
    String stableSetExclusionClause =
        isVolatileSet
            ? " AND bssid NOT IN (SELECT bssid from `" + stableSetEpochTablePath + "`)"
            : "";
    String percentileOperator = isVolatileSet ? "<=" : ">=";

    return "WITH bssid_count AS (SELECT bssid, SUM(count) as total_count FROM "
        + tablePathClause
        + " WHERE poi_id = '"
        + poiId
        + "'"
        + stableSetExclusionClause
        + timeBoundClause
        + "GROUP BY bssid HAVING total_count >= "
        + countThreshold
        + ") SELECT bssid FROM bssid_count WHERE total_count "
        + percentileOperator
        + " (SELECT PERCENTILE_CONT(total_count, "
        + percentile
        + ") OVER() as percentile_count FROM (SELECT * FROM bssid_count) LIMIT 1)";
  }

  public Set<String> getStableSetForVisit(Visit visit) {
    String poiId = visit.getPoiId();

    String tablePath =
        tablePrefix
            + STABLE_SET_EPOCH_TABLE_PREFIX
            + poiId
            + "_"
            + DateUtils.getFirstDayOfWeekOffset(visit.getEntryTime(), 0);

    try {
      String query = "SELECT bssid FROM `" + tablePath + "`";
      QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();

      TableResult result = bigquery.query(queryConfig);
      Set<String> stableSet = getBssidsFromResult(result);

      for (String bssid : stableSet) {
        LOGGER.info("STABLE BSSID: " + bssid);
      }
      LOGGER.info("Stable set retrieved successfully");

      return stableSet;
    } catch (BigQueryException | InterruptedException e) {
      LOGGER.info("Unable to retrieve stable set \n" + e);
    }

    return Collections.emptySet();
  }

  public Set<String> getVolatileSetForVisit(Visit visit, long delta, int countThreshold) {
    String poiId = visit.getPoiId();
    DateTimeFormatter timestampDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    long secondsDuration =
        visit.getLeaveTime().getEpochSecond() - visit.getEntryTime().getEpochSecond();
    // TODO: Claim Time is the point in time equivalent to half the duration
    Instant claimTime =
        Instant.ofEpochSecond(visit.getLeaveTime().getEpochSecond() - secondsDuration / 2);
    // TODO: Handle visits with a midnight occurrence
    String tablePath =
        tablePrefix
            + VOLATILE_SET_TABLE_PREFIX
            + poiId
            + "_"
            + tableIdDateFormat.format(Date.from(visit.getEntryTime()));
    String stableTablePath =
        tablePrefix
            + STABLE_SET_EPOCH_TABLE_PREFIX
            + poiId
            + "_"
            + DateUtils.getFirstDayOfWeekOffset(visit.getEntryTime(), 0);
    String claimTimeLower =
        LocalDateTime.ofInstant(
                Instant.ofEpochSecond(claimTime.getEpochSecond() - delta), ZoneId.of("UTC"))
            .format(timestampDateFormat);
    String claimTimeUpper =
        LocalDateTime.ofInstant(
                Instant.ofEpochSecond(claimTime.getEpochSecond() + delta), ZoneId.of("UTC"))
            .format(timestampDateFormat);

    LOGGER.info("CLAIM TIME LOWER BOUND: " + claimTimeLower);
    LOGGER.info("CLAIM TIME UPPER BOUND: " + claimTimeUpper);

    try {
      String query =
          getBssidSetQueryString(
              tablePath,
              "",
              poiId,
              claimTimeLower,
              claimTimeUpper,
              stableTablePath,
              countThreshold,
              0.10,
              true);
      QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();

      TableResult result = bigquery.query(queryConfig);
      Set<String> volatileSet = getBssidsFromResult(result);

      for (String bssid : volatileSet) {
        LOGGER.info("VOLATILE BSSID: " + bssid);
      }
      LOGGER.info("Volatile set retrieved successfully");

      return volatileSet;
    } catch (BigQueryException | InterruptedException e) {
      LOGGER.info("Unable to retrieve volatile set \n" + e);
    }

    return Collections.emptySet();
  }

  private String getStableSetPeriodSelectQuery(String poiId, String periodTs) {
    return "SELECT poi_id, bssid, count FROM `"
        + tablePrefix
        + STABLE_SET_PERIOD_TABLE_PREFIX
        + poiId
        + "_"
        + periodTs
        + "`";
  }

  public void createStableSetTable(String poiId, Instant epochLowerBound, Instant epochUpperBound) {
    LocalDate currentDate = LocalDate.ofInstant(epochLowerBound, ZoneId.systemDefault());
    LOGGER.info(
        "Attempting to create stable set epoch table for POI "
            + poiId
            + " between periods "
            + currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            + " and "
            + LocalDate.ofInstant(epochUpperBound, ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    String tableUnion =
        "("
            + getStableSetPeriodSelectQuery(
                poiId, currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    currentDate = currentDate.plusDays(1);
    long epochUpperBoundDay =
        LocalDate.ofInstant(epochUpperBound, ZoneId.systemDefault()).toEpochDay();
    while (currentDate.toEpochDay() <= epochUpperBoundDay) {
      tableUnion =
          tableUnion
              + " UNION ALL "
              + getStableSetPeriodSelectQuery(
                  poiId, currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
      currentDate = currentDate.plusDays(1);
    }
    tableUnion = tableUnion + ")";

    String destinationTablePath =
        STABLE_SET_EPOCH_TABLE_PREFIX
            + poiId
            + "_"
            + tableIdDateFormat.format(Date.from(epochLowerBound));

    String query = getBssidSetQueryString("", tableUnion, poiId, "", "", "", 1, 0.90, false);

    try {
      TableId destinationTable = TableId.of(datasetId, destinationTablePath);
      QueryJobConfiguration queryConfig =
          QueryJobConfiguration.newBuilder(query).setDestinationTable(destinationTable).build();

      bigquery.query(queryConfig);

      LOGGER.info("Stable set epoch table created successfully");
    } catch (BigQueryException | InterruptedException e) {
      LOGGER.info("Unable to create stable set epoch table created query\n" + e);
    }
  }

  private static class ScavengerRepositoryHolder {
    private static final ScavengerRepository INSTANCE = new ScavengerRepository();
  }
}
