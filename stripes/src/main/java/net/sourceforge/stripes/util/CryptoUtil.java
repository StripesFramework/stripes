/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.util;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.exception.StripesRuntimeException;

/**
 * <p>Cryptographic utility that can encrypt and decrypt Strings using a key stored in
 * HttpSession.  Strings are encrypted by default using a 168bit DEDede (triple DES) key
 * and then Base 64 encoded in a way that is compatible with being inserte into web pages.</p>
 *
 * <p>A single encryption key is used to encrypt values for all sessions in the web application.
 * The key can come from multiple sources. Without any configuration the key will be generated
 * using a SecureRandom the first time it is needed. <b>Note: this will result in encrypted
 * values that are not decryptable across application restarts or across nodes in a cluster.</b>
 * Alternatively specific key material can be specified using the configuration parameter
 * <code>Stripes.EncryptionKey</code> in web.xml.  This key is text that is used to generate
 * a secret key, and ideally should be quite long (at least 20 characters).  If a key is
 * configured this way the same key will be used across all nodes in a cluster and across
 * restarts.</p>
 *
 * <p>Finally a key can be specified by calling {@link #setSecretKey(javax.crypto.SecretKey)} and
 * providing your own {@link SecretKey} instance. This method allows the specification of any
 * key from any source.  In addition the provided key can be for any algorithm supported by
 * the JVM in which it is constructed.  CryptoUtil will then use the algorithm returned by
 * {@link javax.crypto.SecretKey#getAlgorithm()}.  If using this method, the key should be set
 * before any requests are made, e.g. in a {@link jakarta.servlet.ServletContextListener}.</p>
 *
 * <p>Stripes originally performed a broken authentication scheme. It was rewritten in STS-934
 * to perform the Encrypt-then-Mac pattern. Also the encryption mode was changed from ECB to CBC.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.2
 * @see https://en.wikipedia.org/wiki/Authenticated_encryption
 */
public class CryptoUtil {
	private static final Log log = Log.getInstance(CryptoUtil.class);

    /** The algorithm that is used to encrypt values. */
    protected static final String ALGORITHM = "DESede";
    protected static final String CIPHER_MODE_MODIFIER = "/CBC/PKCS5Padding";
	protected static final int CIPHER_BLOCK_LENGTH = 8;
	private static final String CIPHER_HMAC_ALGORITHM = "HmacSHA256";
	private static final int CIPHER_HMAC_LENGTH = 32;

    /** Key used to look up the location of a secret key. */
    public static final String CONFIG_ENCRYPTION_KEY = "Stripes.EncryptionKey";

    /** Minimum number of bytes to raise the key material to before generating a key. */
    private static final int MIN_KEY_BYTES = 128;

    /** The options used for Base64 Encoding. */
    private static final int BASE64_OPTIONS = Base64.URL_SAFE | Base64.DONT_BREAK_LINES;

    /** Secret key to be used o encrypt and decrypt values. */
    private static SecretKey secretKey;

    /**
     * Takes in a String, encrypts it and then base64 encodes the resulting byte[] so that it can be
     * transmitted and stored as a String. Can be decrypted by a subsequent call to
     * {@link #decrypt(String)}. Because, null and "" are equivalent to the Stripes binding engine,
     * if {@code input} is null, then it will be encrypted as if it were "".
     * 
     * @param input the String to encrypt and encode
     * @return the encrypted, base64 encoded String
     */
    public static String encrypt(String input) {
        if (input == null)
            input = "";

        // encryption is disabled in debug mode
        Configuration configuration = StripesFilter.getConfiguration();
        if (configuration != null && configuration.isDebugMode())
            return input;

        try {
            byte[] inbytes = input.getBytes();
            final int inputLength = inbytes.length;
        	byte[] output = new byte[ calculateCipherbytes(inputLength) + CIPHER_HMAC_LENGTH ];

        	//key required by cipher and hmac
        	SecretKey key = getSecretKey();

        	/*
        	 * Generate an initialization vector required by block cipher modes
        	 */
            byte[] iv = generateInitializationVector();
			System.arraycopy(iv, 0, output, 0, CIPHER_BLOCK_LENGTH);
        	
        	/*
        	 * Encrypt-then-Mac (EtM) pattern, first encrypt plaintext 
        	 */
        	
            Cipher cipher = getCipher(key, Cipher.ENCRYPT_MODE, iv, 0, CIPHER_BLOCK_LENGTH);
            cipher.doFinal(inbytes, 0, inbytes.length, output, CIPHER_BLOCK_LENGTH);
            
        	/*
        	 * Encrypt-then-Mac (EtM) pattern, authenticate ciphertext
        	 */        	
            hmac(key, output, 0, output.length - CIPHER_HMAC_LENGTH, output, output.length - CIPHER_HMAC_LENGTH);

            // Then base64 encode the bytes
            return Base64.encodeBytes(output, BASE64_OPTIONS);
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not encrypt value.", e);
        }
    }

    /**
	 * Generates IV, random start bytes required by most block cipher modes,
	 * which is intended to prevent analyzing a cipher mode as an xor substitution cipher.
	 * 
	 * @return CIPHER_BLOCK_LENGTH bytes of random data
	 */
	private static byte[] generateInitializationVector() {
		// always create a new SecureRandom; get new OS/JVM random bytes instead of cycling the prng.
		SecureRandom random = new SecureRandom();
		byte[] iv = new byte[CIPHER_BLOCK_LENGTH];
		random.nextBytes(iv);
		return iv;
	}

    /**
     * Performs keyed authentication using HMAC.
     * Note: When building ciphertext+hmac array, data and mac will be the same array, and dataLength == macPos.
     * @param key the authentication key
     * @param data the data to be authenticated
     * @param dataPos start of data to be authenticated
     * @param dataLength the number of bytes to be authenticated
     * @param mac the array which holds the resulting hmac
     * @param macPos the position to write the hmac to
     */
    private static void hmac(SecretKey key, byte[] data, int dataPos, int dataLength, byte[] mac, int macPos) throws Exception {
		Mac m = Mac.getInstance(CIPHER_HMAC_ALGORITHM);
		m.init(key);
		m.update(data, dataPos, dataLength);
		m.doFinal(mac, macPos);
	}

    /**
     * Returns the ciphertext length for a given plaintext,
     * @param inputLength the length of plaintext
     * @return the length of ciphertext, calculated based on blockcipher block size
     */
	private static int calculateCipherbytes(int inputLength) {
		// 2 = IV + last block (including padding)
		int blocks = 2 + (inputLength/CIPHER_BLOCK_LENGTH);
		return blocks * CIPHER_BLOCK_LENGTH;
	}

	/**
     * Takes in a base64 encoded and encrypted String that was generated by a call to
     * {@link #encrypt(String)} and decrypts it. If {@code input} is null, then null will be
     * returned.
     * 
     * @param input the base64 String to decode and decrypt
     * @return the decrypted String
     */
    public static String decrypt(String input) {
        if (input == null)
            return null;

        // encryption is disabled in debug mode
        Configuration configuration = StripesFilter.getConfiguration();
        if (configuration != null && configuration.isDebugMode())
            return input;

        // First un-base64 the String
        byte[] bytes = Base64.decode(input, BASE64_OPTIONS);
        if (bytes == null || bytes.length < 1) {
            log.warn("Input is not Base64 encoded: ", input);
            return null;
        }
        
        if (bytes.length < CIPHER_BLOCK_LENGTH * 2 + CIPHER_HMAC_LENGTH) {
            log.warn("Input is too short: ", input);
            return null;        	
        }

        SecretKey key = getSecretKey();
        
        /*  
         * HMAC: validate ciphertext integrity. 
         * invalid hmac = choosen ciphertext attack against system.
         *
         * Encrypt-then-Mac (EtM) pattern, HMAC must be validated before the dangerous decrypt operation.
         *
         */
        
        byte[] mac = new byte[CIPHER_HMAC_LENGTH];
        try {
			hmac(key, bytes, 0, bytes.length - CIPHER_HMAC_LENGTH, mac, 0);
		} catch (Exception e1) {
	        log.warn("Unexpected error performing hmac on: ", input);
	        return null;
	 	}
        
        boolean validCiphertext;
        try {
			validCiphertext = hmacEquals(key, bytes, bytes.length - CIPHER_HMAC_LENGTH, mac, 0);
		} catch (Exception e1) {
	        log.warn("Unexpected error validating hmac of: ", input);
	        return null;
		}
		if (!validCiphertext) {
	        log.warn("Input was not encrypted with the current encryption key (bad HMAC): ", input);
	        return null;        	
        }
        
		/*
		 * Encrypt-then-Mac pattern;
		 * If validation success, ciphertext is assumed to be friendly and safe to process. 
		 * Padding attacks, wrong blocklength etc is not expected from this point.
		 * 
		 */
		
        // Then fetch a cipher and decrypt the bytes
        Cipher cipher = getCipher(key, Cipher.DECRYPT_MODE, bytes, 0, CIPHER_BLOCK_LENGTH);
        byte[] output;
        try {
            output = cipher.doFinal(bytes, CIPHER_BLOCK_LENGTH, bytes.length - CIPHER_HMAC_LENGTH - CIPHER_BLOCK_LENGTH);
        }
        catch (IllegalBlockSizeException e) {
            log.warn("Unexpected IllegalBlockSizeException on: ", input);
            return null;
        }
        catch (BadPaddingException e) {
            log.warn("Unexpected BadPaddingException on: ", input);
            return null;
        }

        return new String(output);
    }

	/**
	 * Compares HMAC in a manner secured against timing attacks, as per NCC Group "Double Hmac Verification" recepie.
	 * Destructive compare, the hmac's will be replaced with the hmac's of themselves as a side effect.
	 * @param key the hmac crypto key
	 * @param mac1 the array which contains the hmac
	 * @param mac1pos the position of the hmac in mac1 array.
	 * @param mac2 the array which contains the hmac
	 * @param mac2pos the position of the hmac in mac2 array.
	 * @return true if hmacs are equal, otherwise false
	 * @see double hmac as per https://www.nccgroup.trust/us/about-us/newsroom-and-events/blog/2011/february/double-hmac-verification/ 
	 */
    private static boolean hmacEquals(SecretKey key, byte[] mac1, int mac1pos,
			byte[] mac2, int mac2pos) throws Exception {
    	hmac(key, mac1, mac1pos, CIPHER_HMAC_LENGTH, mac1, mac1pos);
       	hmac(key, mac2, mac2pos, CIPHER_HMAC_LENGTH, mac2, mac2pos);
       	for(int i = 0; i < CIPHER_HMAC_LENGTH; i++)
       		if (mac1[mac1pos+i] != mac2[mac2pos+i])
       			return false;
		return true;
	}

    /**
     * Generates a cipher based on a key and an initialization vector
     * @param key the crypto key
     * @param mode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * @param iv the initialization vector
     * @param ivpos the start position of the initialization vector, typically 0
     * @param ivlength the length of the initialization vector
     * @return the cipher object
     * @see Cipher#ENCRYPT_MODE
     * @see Cipher#DECRYPT_MODE
     */
	protected static Cipher getCipher(SecretKey key, int mode, byte[] iv, int ivpos, int ivlength) {
        try {
            // Then build a cipher for the correct mode
            Cipher cipher = Cipher.getInstance(key.getAlgorithm() + CIPHER_MODE_MODIFIER);
            IvParameterSpec ivps = new IvParameterSpec(iv, ivpos, ivlength);
            cipher.init(mode, key, ivps);
            return cipher;
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not generate a Cipher.", e);
        }
    }

    /**
     * Returns the secret key to be used to encrypt and decrypt values. The key will be generated
     * the first time it is requested.  Will look for source material for the key in config and
     * use that if found.  Otherwise will generate key material using a SecureRandom and then
     * manufacture the key. Once the key is created it is cached locally and the
     * same key instance will be returned until the application is shutdown or restarted.
     *
     * @return SecretKey the secret key used to encrypt and decrypt values
     */
    protected static synchronized SecretKey getSecretKey() {
        try {
            if (CryptoUtil.secretKey == null) {
                // Check to see if a key location was specified in config
                byte[] material = getKeyMaterialFromConfig();

                // If there wasn't a key string in config, make one
                if (material == null) {
                    material = new byte[MIN_KEY_BYTES];
                    new SecureRandom().nextBytes(material);
                }
                // Hash the key string given in config
                else {
                    MessageDigest digest = MessageDigest.getInstance("SHA1");
                    int length = digest.getDigestLength();
                    byte[] hashed = new byte[MIN_KEY_BYTES];
                    for (int i = 0; i < hashed.length; i += length) {
                        material = digest.digest(material);
                        System.arraycopy(material, 0, hashed, i,
                                Math.min(length, MIN_KEY_BYTES - i));
                    }
                    material = hashed;
                }

                // Now manufacture the actual Secret Key instance
                SecretKeyFactory factory = SecretKeyFactory.getInstance(CryptoUtil.ALGORITHM);
                CryptoUtil.secretKey = factory.generateSecret(new DESedeKeySpec(material));
            }
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not generate a secret key.", e);
        }

        return CryptoUtil.secretKey;
    }

    /**
     * Attempts to load material from which to manufacture a secret key from the Stripes
     * Configuration. If config is unavailable or there is no material configured null
     * will be returned.
     *
     * @return a byte[] of key material, or null
     */
    protected static byte[] getKeyMaterialFromConfig() {
        try {
            Configuration config = StripesFilter.getConfiguration();
            if (config != null) {
                String key = config.getBootstrapPropertyResolver().getProperty(CONFIG_ENCRYPTION_KEY);
                if (key != null) {
                    return key.getBytes();
                }
            }
        }
        catch (Exception e) {
            log.warn("Could not load key material from configuration.", e);
        }

        return null;
    }

    /**
     * Sets the secret key that will be used by the CryptoUtil to perform encryption
     * and decryption.  In general the use of the config property (Stripes.EncryptionKey)
     * should be preferred, but if specific encryption methods are required, this method
     * allows the caller to set a SecretKey suitable to any symmetric encryption algorithm
     * available in the JVM.
     *
     * @param key the secret key to be used to encrypt and decrypt values going forward
     */
    public static synchronized void setSecretKey(SecretKey key) {
        CryptoUtil.secretKey = key;
    }

}
