package pt.ulisboa.tecnico.cross.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.enterprise.context.ApplicationScoped;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static pt.ulisboa.tecnico.cross.CROSSCityServer.PROPS;

@ApplicationScoped
public class JwtService {

  public static final String ROLES_CLAIM = "roles";
  private static final long TTL_MILLIS = TimeUnit.HOURS.toMillis(1);
  private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;
  private final Keychain keychain = Keychain.get();

  private final byte[] signingKey;

  public JwtService() {
    signingKey = DatatypeConverter.parseBase64Binary(PROPS.getProperty("SECRET_KEY"));
  }

  public String createJwt(String subject, List<String> roles) {

    Map<String, Object> claims = new HashMap<>();
    claims.put(Claims.SUBJECT, subject);
    claims.put(ROLES_CLAIM, roles);
    claims.put(Claims.EXPIRATION, new Date(System.currentTimeMillis() + TTL_MILLIS));

    return Jwts.builder()
        .setClaims(claims)
        .signWith(new SecretKeySpec(signingKey, SIGNATURE_ALGORITHM.getJcaName()))
        .compact();
  }

  public Jws<Claims> decodeJwt(String jwt) {
    return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(jwt);
  }

  public String getUsername(byte[] encryptedJwt) {
    byte[] jwt = keychain.decrypt(encryptedJwt);
    if (jwt == null) return null;
    Claims claims = decodeJwt(new String(jwt, UTF_8)).getBody();
    if (new Date().after(claims.getExpiration())) return null;
    return claims.getSubject();
  }
}
