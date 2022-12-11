package pt.ulisboa.tecnico.cross.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class CryptoManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(CryptoManager.class);
  private static final String KEY_ALGORITHM_RSA = "RSA";
  private static final int KEY_SIZE = 1024;
  private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
  private static final String ENCRYPTION_ALGORITHM = "RSA/ECB/PKCS1Padding";
  private static final String CERTIFICATE_ALGORITHM = "X.509";

  private PrivateKey userPrivateKey;
  private PublicKey userPublicKey;
  private PublicKey serverPublicKey;

  /****************************
   * User key pair management *
   ****************************/

  public synchronized void generateNewUserKeyPair() {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA);
      keyPairGenerator.initialize(KEY_SIZE);

      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      userPublicKey = keyPair.getPublic();
      userPrivateKey = keyPair.getPrivate();
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("Failed to generate key pair.", e);
    }
  }

  public synchronized PublicKey getUserPublicKey() {
    if (userPublicKey == null) {
      LOGGER.error("The user's public key is null.");
      return null;
    }
    return userPublicKey;
  }

  public synchronized byte[] sign(byte[] data) {
    if (userPrivateKey == null) {
      LOGGER.error("The user's private key is null.");
      return null;
    }
    try {
      Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
      signature.initSign(userPrivateKey);
      signature.update(data);

      return signature.sign();
    } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
      LOGGER.error("Failed to sign data.", e);
      return null;
    }
  }

  /*************************
   * Server key management *
   *************************/

  public synchronized void storeServerCertificate(byte[] certificateBytes) {
    try {
      Certificate certificate =
          CertificateFactory.getInstance(CERTIFICATE_ALGORITHM)
              .generateCertificate(new ByteArrayInputStream(certificateBytes));
      serverPublicKey = certificate.getPublicKey();
    } catch (CertificateException e) {
      LOGGER.error("The server certificate store failed.", e);
    }
  }

  public synchronized byte[] encrypt(byte[] data) {
    if (serverPublicKey == null) {
      LOGGER.error("The server's public key is null.");
      return null;
    }
    try {
      Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);

      return cipher.doFinal(data);
    } catch (NoSuchPaddingException
        | IllegalBlockSizeException
        | NoSuchAlgorithmException
        | BadPaddingException
        | InvalidKeyException e) {
      LOGGER.error("Failed to encrypt data.", e);
      return null;
    }
  }
}
