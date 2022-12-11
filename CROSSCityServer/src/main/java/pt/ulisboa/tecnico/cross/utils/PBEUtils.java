package pt.ulisboa.tecnico.cross.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class PBEUtils {

  private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA512";
  private static final int KDF_ITERATIONS = 500;
  private static final int KDF_KEY_LENGTH = 512;

  private final SecureRandom secRandom = new SecureRandom();

  public static PBEUtils get() {
    return PBEUtilsHolder.INSTANCE;
  }

  private PBEUtils() {}

  public byte[] newSalt() {
    byte[] salt = new byte[16];
    secRandom.nextBytes(salt);
    return salt;
  }

  public boolean isExpectedPwd(String pwd, byte[] pwdHash, byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    return Arrays.equals(hash(pwd, salt), pwdHash);
  }

  public byte[] hash(String pwd, byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    PBEKeySpec spec = new PBEKeySpec(pwd.toCharArray(), salt, KDF_ITERATIONS, KDF_KEY_LENGTH);
    SecretKeyFactory skf = SecretKeyFactory.getInstance(KDF_ALGORITHM);
    return skf.generateSecret(spec).getEncoded();
  }

  private static class PBEUtilsHolder {
    private static final PBEUtils INSTANCE = new PBEUtils();
  }
}
