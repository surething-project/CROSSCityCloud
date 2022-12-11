package pt.ulisboa.tecnico.cross.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static pt.ulisboa.tecnico.cross.CROSSCityServer.PROPS;

public class Keychain {

  private static final Logger LOGGER = LoggerFactory.getLogger(Keychain.class);
  private static final String KEY_ALGORITHM = "RSA";
  private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
  private static final String ENCRYPTION_ALGORITHM = "RSA/ECB/PKCS1Padding";
  private PrivateKey privateKey;
  private byte[] certificate;

  public static Keychain get() {
    return KeychainHolder.INSTANCE;
  }

  private Keychain() {
    try {
      KeyStore keyStore = KeyStore.getInstance("PKCS12");
      keyStore.load(
          new FileInputStream(PROPS.getProperty("KEYSTORE_PATH")),
          PROPS.getProperty("KEYSTORE_PWD").toCharArray());
      privateKey =
          (PrivateKey)
              keyStore.getKey(
                  PROPS.getProperty("KEYSTORE_ALIAS"),
                  PROPS.getProperty("KEYSTORE_PWD").toCharArray());
      certificate = keyStore.getCertificate(PROPS.getProperty("KEYSTORE_ALIAS")).getEncoded();
    } catch (KeyStoreException
        | UnrecoverableKeyException
        | CertificateException
        | IOException
        | NoSuchAlgorithmException e) {
      LOGGER.error(e.getMessage());
      System.exit(1);
    }
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  public byte[] getCertificate() {
    return certificate;
  }

  public PublicKey getPublicKey(byte[] encodedPublicKey) {
    try {
      return KeyFactory.getInstance(KEY_ALGORITHM)
          .generatePublic(new X509EncodedKeySpec(encodedPublicKey));
    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      return null;
    }
  }

  public byte[] decrypt(byte[] encryptedData) {
    try {
      Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      return cipher.doFinal(encryptedData);
    } catch (NoSuchPaddingException
        | NoSuchAlgorithmException
        | InvalidKeyException
        | IllegalBlockSizeException
        | BadPaddingException e) {
      LOGGER.error("Failed to decrypt data.", e);
      return null;
    }
  }

  public boolean verify(PublicKey publicKey, byte[] signedData, byte[] dataSignature) {
    try {
      Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
      signature.initVerify(publicKey);
      signature.update(signedData);
      return signature.verify(dataSignature);
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      LOGGER.error("Failed to verify data signature.", e);
      return false;
    }
  }

  private static class KeychainHolder {
    private static final Keychain INSTANCE = new Keychain();
  }
}
