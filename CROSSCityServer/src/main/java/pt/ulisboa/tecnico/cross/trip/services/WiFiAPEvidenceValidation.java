package pt.ulisboa.tecnico.cross.trip.services;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.TimeProvider;
import org.glassfish.grizzly.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.cross.error.APIException;
import pt.ulisboa.tecnico.cross.poi.domain.POI;
import pt.ulisboa.tecnico.cross.trip.domain.Trip;
import pt.ulisboa.tecnico.cross.trip.domain.Visit;
import pt.ulisboa.tecnico.cross.trip.domain.WiFiAPEvidence;
import pt.ulisboa.tecnico.cross.wifiap.domain.WiFiAP;
import pt.ulisboa.tecnico.cross.wifiap.domain.WiFiAP.WiFiAPType;
import pt.ulisboa.tecnico.surerepute.SureReputeClient;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.PROPS;
import static pt.ulisboa.tecnico.surerepute.Report.INTENTIONALLY_MALICIOUS;

public class WiFiAPEvidenceValidation {

  private static final Logger LOGGER = LoggerFactory.getLogger(WiFiAPEvidenceValidation.class);
  private final int TOTP_PERIOD = Integer.parseInt(PROPS.getProperty("TOTP_PERIOD"));
  private final int TOTP_DIGIT = Integer.parseInt(PROPS.getProperty("TOTP_DIGIT"));
  private final int TOTP_SKEW = Integer.parseInt(PROPS.getProperty("TOTP_SKEW"));
  private final SureReputeClient sureReputeClient;

  WiFiAPEvidenceValidation(SureReputeClient sureReputeClient) {
    this.sureReputeClient = sureReputeClient;
  }

  double calcConfidence(Trip trip, Visit visit, POI visitedPOI) {
    if (visit.getWiFiAPEvidences().size() == 0) return 0;

    Map<String, WiFiAP> wiFiUntrustedAPs = new HashMap<>();
    Map<String, WiFiAP> wiFiTOTPAPs = new HashMap<>();

    for (WiFiAP wiFiAP : visitedPOI.getWiFiAPs().values()) {
      if (wiFiAP.getType() == WiFiAPType.UNTRUSTED) {
        wiFiUntrustedAPs.put(wiFiAP.getBssid(), wiFiAP);
      } else if (wiFiAP.getType() == WiFiAPType.TOTP) {
        wiFiTOTPAPs.put(wiFiAP.getBssid(), wiFiAP);
      }
    }
    if (wiFiTOTPAPs.isEmpty() && wiFiUntrustedAPs.isEmpty()) return 0;

    List<Pair<WiFiAP, WiFiAPEvidence>> sightedTOTPAPs = new ArrayList<>();
    List<Pair<WiFiAP, WiFiAPEvidence>> sightedUntrustedAPs = new ArrayList<>();

    for (WiFiAPEvidence wiFiAPEvidence : visit.getWiFiAPEvidences()) {
      if (wiFiTOTPAPs.containsKey(wiFiAPEvidence.getBssid())) {
        WiFiAP wiFiAP = wiFiTOTPAPs.get(wiFiAPEvidence.getBssid());
        sightedTOTPAPs.add(new Pair<>(wiFiAP, wiFiAPEvidence));
      } else if (wiFiUntrustedAPs.containsKey(wiFiAPEvidence.getBssid())) {
        WiFiAP wiFiAP = wiFiUntrustedAPs.get(wiFiAPEvidence.getBssid());
        sightedUntrustedAPs.add(new Pair<>(wiFiAP, wiFiAPEvidence));
      }
    }

    if (sightedTOTPAPs.size() > 0) {
      return calcTOTPAPsConfidence(trip, visit, sightedTOTPAPs);
    } else if (sightedUntrustedAPs.size() > 0) {
      return calcUntrustedAPsConfidence(wiFiUntrustedAPs.size(), sightedUntrustedAPs.size());
    }
    return 0;
  }

  private double calcUntrustedAPsConfidence(int nKnownUntrustedAPs, int nSightedUntrustedAPs) {
    LOGGER.info("Number of valid untrusted APs sighted: " + nSightedUntrustedAPs);
    LOGGER.info("Number of known untrusted APs: " + nKnownUntrustedAPs);
    return (double) nSightedUntrustedAPs / nKnownUntrustedAPs;
  }

  private double calcTOTPAPsConfidence(
      Trip trip, Visit visit, List<Pair<WiFiAP, WiFiAPEvidence>> sightedTOTPAPs) {
    Set<Long> sightingPeriods = new HashSet<>();

    for (Pair<WiFiAP, WiFiAPEvidence> sightedTOTPAP : sightedTOTPAPs) {
      WiFiAP wiFiAP = sightedTOTPAP.getFirst();
      WiFiAPEvidence wiFiAPEvidence = sightedTOTPAP.getSecond();

      long sightingSeconds = TimeUnit.MILLISECONDS.toSeconds(wiFiAPEvidence.getSightingMillis());
      if (sightingSeconds > visit.getLeaveTime().getEpochSecond()
          || sightingSeconds < visit.getEntryTime().getEpochSecond()) {
        LOGGER.error(
            "The sighting of the modified Wi-Fi AP with BSSID {} did not occur during the visit.",
            wiFiAP.getBssid());
        sureReputeClient.reportBehaviorNonBlocking(trip.getTraveler(), INTENTIONALLY_MALICIOUS);
        throw new APIException(BAD_REQUEST, "Fraud detection in the sighting of a Wi-Fi AP.");
      }

      String totpSecret = wiFiAP.getTOTPSecret();
      Matcher ssidMatcher =
          Pattern.compile(wiFiAP.getTOTPRegexp()).matcher(wiFiAPEvidence.getSsid());
      if (!ssidMatcher.matches()) {
        LOGGER.error(
            "The SSID format of the modified Wi-Fi AP with BSSID {} is invalid.",
            sightedTOTPAP.getFirst().getBssid());
        sureReputeClient.reportBehaviorNonBlocking(trip.getTraveler(), INTENTIONALLY_MALICIOUS);
        throw new APIException(BAD_REQUEST, "Fraud detection in the sighting of a Wi-Fi AP.");
      }
      String totpToken = ssidMatcher.group(1);
      if (!isTOTPTokenValid(totpToken, totpSecret, sightingSeconds, TOTP_PERIOD)) {
        LOGGER.error(
            "The token '{}' of the modified Wi-Fi AP with BSSID {} is invalid.",
            totpToken,
            wiFiAP.getBssid());
        sureReputeClient.reportBehaviorNonBlocking(trip.getTraveler(), INTENTIONALLY_MALICIOUS);
        throw new APIException(BAD_REQUEST, "Fraud detection in the sighting of a Wi-Fi AP.");
      }

      sightingPeriods.add(Math.floorDiv(sightingSeconds, TOTP_PERIOD));
    }
    LOGGER.info("Modified Wi-Fi APs were sighted in {} periods.", sightingPeriods.size());

    double visitDuration =
        visit.getLeaveTime().getEpochSecond() - visit.getEntryTime().getEpochSecond();
    return Math.min(sightingPeriods.size() * TOTP_PERIOD / visitDuration, 1);
  }

  private boolean isTOTPTokenValid(
      String totpToken, String totpSecret, long sightingSeconds, int totpPeriod) {
    CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA512, TOTP_DIGIT);
    TimeProvider timeProvider = () -> sightingSeconds;
    DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    verifier.setTimePeriod(totpPeriod);
    verifier.setAllowedTimePeriodDiscrepancy(TOTP_SKEW);
    return verifier.isValidCode(totpToken, totpSecret);
  }
}
