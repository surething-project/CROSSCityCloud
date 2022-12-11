package pt.ulisboa.tecnico.cross.utils;

import com.google.protobuf.ByteString;
import eu.surething_project.core.*;
import eu.surething_project.signature.util.SignatureManager;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import pt.ulisboa.tecnico.cross.trip.domain.Trip;
import pt.ulisboa.tecnico.cross.trip.domain.Visit;
import pt.ulisboa.tecnico.transparency.ledger.contract.Ledger;
import pt.ulisboa.tecnico.transparency.ledger.contract.Ledger.AuditResult;
import pt.ulisboa.tecnico.transparency.ledger.contract.Ledger.SLCT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static java.lang.System.currentTimeMillis;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.PROPS;

@ApplicationScoped
public class LCTService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LCTService.class);
  private final Keychain keychain = Keychain.get();
  private final RestTemplate restTemplate;
  private final long mmd;

  public LCTService() {
    this.mmd = Long.parseLong(PROPS.getProperty("LCT_MMD"));
    this.restTemplate = new RestTemplate(List.of(new ProtobufHttpMessageConverter()));
  }

  public void submitLCertificates(Trip trip) {
    List<LocationClaim> locationClaims =
        trip.getVisits().stream().map(Visit::toLocationClaimProtobuf).collect(Collectors.toList());
    List<LocationCertificate> locationCertificates = new ArrayList<>();
    List<SLCT> slcts = new ArrayList<>();
    for (LocationClaim locationClaim : locationClaims) {
      slcts.add(this.sendLCertificate(locationClaim, locationCertificates));
    }
    verifyLCertificatesSubmission(locationCertificates, slcts);
  }

  private void verifyLCertificatesSubmission(
      List<LocationCertificate> locationCertificates, List<SLCT> slcts) {
    Optional<SLCT> slct =
        slcts.stream()
            .reduce((slct1, slct2) -> slct1.getTimestamp() > slct2.getTimestamp() ? slct1 : slct2);
    if (slct.isEmpty()) throw new RuntimeException("Invalid SLCT received.");

    long timeToSleep = slct.get().getTimestamp() + this.mmd - currentTimeMillis();
    if (timeToSleep > 0) {
      try {
        Thread.sleep(timeToSleep);
      } catch (InterruptedException e) {
        throw new RuntimeException("Error waiting for SLCT to pass: " + e.getMessage());
      }
    }
    AuditResult auditResult;
    try {
      auditResult =
          restTemplate.postForObject(PROPS.getProperty("AUDITOR_URL"), slct, AuditResult.class);
    } catch (ResponseStatusException e) {
      throw new RuntimeException("There was an error verifying the submission: " + e.getMessage());
    }
    if (auditResult != null
        && auditResult.getSignedLocationCertificatesList().stream()
            .map(Ledger.SignedLocationCertificate::getLocationCertificate)
            .collect(Collectors.toList())
            .containsAll(locationCertificates)) {
      LOGGER.info(
          "All Location Certificates for Location Claims have been successfully submitted!");
    } else {
      throw new RuntimeException("Location certificates were not submitted successfully.");
    }
  }

  public SLCT sendLCertificate(
      LocationClaim locationClaim, List<LocationCertificate> locationCertificates) {
    LocationVerification locationVerification =
        LocationVerification.newBuilder()
            .setClaimId(locationClaim.getClaimId())
            .setTime(Time.newBuilder().setTimestamp(fromMillis(currentTimeMillis())))
            .setEvidenceType(locationClaim.getEvidenceType())
            .setEvidence(locationClaim.getEvidence())
            .build();
    byte[] signatureByteArray;
    try {
      signatureByteArray =
          SignatureManager.sign(locationVerification.toByteArray(), keychain.getPrivateKey());
    } catch (Exception e) {
      throw new RuntimeException(
          "Unable to submit LClaim! Error signing LVerification: " + e.getMessage());
    }
    Random random = new Random();
    eu.surething_project.core.Signature signature =
        Signature.newBuilder()
            .setCryptoAlgo("SHA256withRSA")
            .setValue(ByteString.copyFrom(signatureByteArray))
            .setNonce(random.nextLong())
            .build();
    LocationCertificate locationCertificate =
        LocationCertificate.newBuilder()
            .setVerification(locationVerification)
            .setVerifierSignature(signature)
            .build();
    locationCertificates.add(locationCertificate);
    try {
      return restTemplate.postForObject(
          PROPS.getProperty("LEDGER_URL"), locationCertificate, SLCT.class);
    } catch (ResponseStatusException e) {
      throw new RuntimeException("There was an error verifying the submission: " + e.getMessage());
    }
  }
}
