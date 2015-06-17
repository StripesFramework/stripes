package net.sourceforge.stripes.util;

import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Basic tests for the CryptoUtil
 *
 * @author Tim Fennell
 */
public class CryptoUtilTest {

    @Test(groups="fast")
    public void basicEncryptionTest() throws Exception {
        String input = "A Basic String to Encrypt";
        String encrypted = CryptoUtil.encrypt(input);
        String decrypted = CryptoUtil.decrypt(encrypted);

        Assert.assertFalse(input.equals(encrypted), "Encrypted string should be different!");
        Assert.assertTrue(input.equals(decrypted), "Decrypted string should match!");
        
        input = "";
        for(int i = 0; i < 100; i++) {
            encrypted = CryptoUtil.encrypt(input);
            decrypted = CryptoUtil.decrypt(encrypted);

            Assert.assertTrue(input.equals(decrypted), "Decrypted string should match!");
            input += "x";
        }
    }

    @Test(groups="fast")
    public void encryptEmptyStringTest() throws Exception {
        String input = "";
        String encrypted = CryptoUtil.encrypt(input);
        String decrypted = CryptoUtil.decrypt(encrypted);

        Assert.assertFalse(input.equals(encrypted), "Encrypted string should be different!");
        Assert.assertTrue(input.equals(decrypted), "Decrypted string should match!");
    }

    @Test(groups="fast")
    public void encryptNullTest() throws Exception {
        String input = null;
        String encrypted = CryptoUtil.encrypt(input);
        String decrypted = CryptoUtil.decrypt(encrypted);

        Assert.assertEquals(decrypted, "", "Encrypting and then decrypting null should yield \"\"");
    }

    @Test(groups="fast")
    public void decryptNullTest() throws Exception {
        String input = null;
        String decrypted = CryptoUtil.decrypt(input);

        Assert.assertNull(decrypted, "Decrypting null should give back null.");
    }
    
    @Test(groups = "fast")
    public void decryptBogusInputTest() throws Exception {
        String input = "_sipApTvfAXjncUGTRUf4OwZJBdz4Mbp2ZxqVyzkKio=";
        String decrypted = CryptoUtil.decrypt(input);
        Assert.assertNull(decrypted, "Decrypting a bogus input should give back null.");
    }

    @Test(groups="fast")
    public void replacementKeyTest() throws Exception {
        SecretKey oldKey = CryptoUtil.getSecretKey(); // cache the old key

        try {
            KeyGenerator gen = KeyGenerator.getInstance(CryptoUtil.ALGORITHM);
            SecretKey key = gen.generateKey();
            CryptoUtil.setSecretKey(key);
            String input = "A string to be encrypted with a different algorigthm and key!";
            String output = CryptoUtil.encrypt(input);
            String result = CryptoUtil.decrypt(output);

            Assert.assertEquals(input, result);
        }
        finally {
            CryptoUtil.setSecretKey(oldKey);
        }
    }

	/** 
	 * This test is disabled because it is very very slow.
	 * It will launch a modified ciphertext attack, which should always be rejected by hmac verification.
	 */
    @Test(groups="slow", enabled=false)
	public void failOnWeakHash() throws Exception {
		String input = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		String encrypted = CryptoUtil.encrypt(input);
		SecureRandom rnd = new SecureRandom();
		byte[] random = new byte[64];
		int options = Base64.URL_SAFE | Base64.DONT_BREAK_LINES;
		byte[] choosen = Base64.decode(encrypted, options);
		for (int attempts = 0; attempts < Integer.MAX_VALUE; attempts++) {
			rnd.nextBytes(random);
			System.arraycopy(random, 0, choosen, CryptoUtil.CIPHER_BLOCK_LENGTH, CryptoUtil.CIPHER_BLOCK_LENGTH);
			String choosenciphertext = Base64.encodeBytes(choosen, options);
			String broken = CryptoUtil.decrypt(choosenciphertext);
			Assert.assertNull(broken, "hash failed: " + choosenciphertext
					+ " derived from " + encrypted);
		}
	}
    
	@Test(groups = "fast")
	public void failOnECB() throws Exception {
		String input1 = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		String encrypted1 = CryptoUtil.encrypt(input1);
		String encrypted2 = CryptoUtil.encrypt(input1);
		for (int i = 0; i < encrypted1.length() - 4; i++)
			Assert.assertFalse(
					encrypted2.contains(encrypted1.substring(i, i + 4)),
					"Predictable ECB detected: " + encrypted1 + " " + encrypted2);
	}

}
