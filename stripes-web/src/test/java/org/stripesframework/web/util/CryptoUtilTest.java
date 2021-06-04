package org.stripesframework.web.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/**
 * Basic tests for the CryptoUtil
 *
 * @author Tim Fennell
 */
public class CryptoUtilTest {

   @Test
   public void basicEncryptionTest() {
      String input = "A Basic String to Encrypt";
      String encrypted = CryptoUtil.encrypt(input);
      String decrypted = CryptoUtil.decrypt(encrypted);

      assertThat(encrypted).isNotEqualTo(input).describedAs("Encrypted string should be different!");
      assertThat(decrypted).isEqualTo(input).describedAs("Decrypted string should match!");

      input = "";
      for ( int i = 0; i < 100; i++ ) {
         encrypted = CryptoUtil.encrypt(input);
         decrypted = CryptoUtil.decrypt(encrypted);

         assertThat(decrypted).isEqualTo(input).describedAs("Decrypted string should match!");
         input += "x";
      }
   }

   @Test
   public void decryptBogusInputTest() {
      String input = "_sipApTvfAXjncUGTRUf4OwZJBdz4Mbp2ZxqVyzkKio=";
      String decrypted = CryptoUtil.decrypt(input);
      assertThat(decrypted).describedAs("Decrypting a bogus input should give back null.").isNull();
   }

   @SuppressWarnings("ConstantConditions")
   @Test
   public void decryptNullTest() {
      String input = null;
      String decrypted = CryptoUtil.decrypt(input);
      assertThat(decrypted).describedAs("Decrypting null should give back null.").isNull();
   }

   @Test
   public void encryptEmptyStringTest() {
      String input = "";
      String encrypted = CryptoUtil.encrypt(input);
      String decrypted = CryptoUtil.decrypt(encrypted);

      assertThat(encrypted).isNotEqualTo(input).describedAs("Encrypted string should be different!");
      assertThat(decrypted).isEqualTo(input).describedAs("Decrypted string should match!");
   }

   @Test
   public void encryptNullTest() {
      String input = null;
      String encrypted = CryptoUtil.encrypt(input);
      String decrypted = CryptoUtil.decrypt(encrypted);

      assertThat(decrypted).isEmpty();
   }

   @Test
   public void failOnECB() {
      String input1 = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
      String encrypted1 = CryptoUtil.encrypt(input1);
      String encrypted2 = CryptoUtil.encrypt(input1);
      for ( int i = 0; i < encrypted1.length() - 4; i++ ) {
         assertThat(encrypted2.contains(encrypted1.substring(i, i + 4))).isFalse().describedAs("Predictable ECB detected: " + encrypted1 + " " + encrypted2);
      }
   }

   /**
    * This test is disabled because it is very very slow.
    * It will launch a modified ciphertext attack, which should always be rejected by hmac verification.
    */
   @Test
   @Disabled
   public void failOnWeakHash() {
      String input = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
      String encrypted = CryptoUtil.encrypt(input);
      SecureRandom rnd = new SecureRandom();
      byte[] random = new byte[64];
      int options = Base64.URL_SAFE | Base64.DONT_BREAK_LINES;
      byte[] choosen = Base64.decode(encrypted, options);
      for ( int attempts = 0; attempts < Integer.MAX_VALUE; attempts++ ) {
         rnd.nextBytes(random);
         System.arraycopy(random, 0, choosen, CryptoUtil.CIPHER_BLOCK_LENGTH, CryptoUtil.CIPHER_BLOCK_LENGTH);
         String choosenciphertext = Base64.encodeBytes(choosen, options);
         String broken = CryptoUtil.decrypt(choosenciphertext);
         if ( broken != null ) {
            fail("hash failed: " + choosenciphertext + " derived from " + encrypted);
         }
      }
   }

   @Test
   public void replacementKeyTest() throws NoSuchAlgorithmException {
      SecretKey oldKey = CryptoUtil.getSecretKey(); // cache the old key

      try {
         KeyGenerator gen = KeyGenerator.getInstance(CryptoUtil.ALGORITHM);
         SecretKey key = gen.generateKey();
         CryptoUtil.setSecretKey(key);
         String input = "A string to be encrypted with a different algorigthm and key!";
         String output = CryptoUtil.encrypt(input);
         String result = CryptoUtil.decrypt(output);

         assertThat(input).isEqualTo(result);
      }
      finally {
         CryptoUtil.setSecretKey(oldKey);
      }
   }

}
