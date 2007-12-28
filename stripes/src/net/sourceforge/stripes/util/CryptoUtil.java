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

import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.config.Configuration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

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
 * key from any source.  In addition they provided key can be for any algorithm supported by
 * the JVM in which it is constructed.  CryptoUtil will then use the algorithm returned by
 * {@link javax.crypto.SecretKey#getAlgorithm()}.  If using this method, the key should be set
 * before any requests are made, e.g. in a {@link javax.servlet.ServletContextListener}.</p>
 *
 * <p>Two additional measures are taken to improve security. Firstly a nonce value is prepended
 * to the input during encryption.  This is a value generated each time using a SecureRandom.
 * Doing this ensures that the same value is not encrypted the same way each time and leads to
 * increased unpredictabibilty of the encrypted values.  Secondly a "magic number" is also
 * prepended to the input (after the nonce).  The magic number is verified at decryption time
 * to ensure that the value passed in was encrypted using the same key as was used for decryption.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.2
 */
public class CryptoUtil {
    private static final Log log = Log.getInstance(CryptoUtil.class);
    private static final SecureRandom random = new SecureRandom();

    /** The algorithm that is used to encrypt values. */
    public static final String ALGORITHM = "DESede";

    /** Key used to look up the location of a secret key. */
    public static final String CONFIG_ENCRYPTION_KEY = "Stripes.EncryptionKey";

    /** Minimum number of bytes to raise the key material to before generating a key. */
    private static final int MIN_KEY_BYTES = 128;

    /** The number of bytes that should be used to generate the nonce value. */
    private static final int NONCE_SIZE = 2;

    /** A seed number used when generating a hash code from a byte array. */
    private static final int HASH_CODE_SEED = 5381;

    /** The number of bytes required to hold the hash code (sizeof short) */
    private static final int HASH_CODE_SIZE = 2;

    /** Short hand for the combined size of the nonce + magic number. */
    private static final int DISCARD_BYTES = NONCE_SIZE + HASH_CODE_SIZE;

    /** The options used for Base64 Encoding. */
    private static final int BASE64_OPTIONS = Base64.URL_SAFE | Base64.DONT_BREAK_LINES;

    /** Secret key to be used o encrypt and decrypt values. */
    private static SecretKey secretKey;

    /**
     * Takes in a String, encrypts it and then base64 encodes the resulting byte[] so that
     * it can be transmitted and stored as a String.  Can be decrypted by a subsequent call
     * to {@link #decrypt(String, HttpServletRequest)} in the same session.
     *
     * @param input the String to encrypt and encode
     * @param request NO LONGER USED
     * @return the encrypted, base64 encoded String
     * @deprecated use {@link #encrypt(String)} instead
     */
    @Deprecated public static String encrypt(String input, HttpServletRequest request) {
        return encrypt(input);
    }

    /**
     * Takes in a String, encrypts it and then base64 encodes the resulting byte[] so that
     * it can be transmitted and stored as a String.  Can be decrypted by a subsequent call
     * to {@link #decrypt(String, HttpServletRequest)} in the same session.
     *
     * @param input the String to encrypt and encode
     * @return the encrypted, base64 encoded String
     */
    public static String encrypt(String input) {
        if (input == null) return null;

        try {
            // First size the output
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            byte[] inbytes = input.getBytes();
            final int inputLength = inbytes.length;
            int size = cipher.getOutputSize(DISCARD_BYTES + inputLength);
            byte[] output = new byte[size];

            // Then encrypt along with the nonce and the hash code
            byte[] nonce = nextNonce();
            byte[] hash = generateHashCode(nonce, inbytes);
            int index = cipher.update(hash, 0, HASH_CODE_SIZE, output, 0);
            index = cipher.update(nonce, 0, NONCE_SIZE, output, index);
            cipher.doFinal(inbytes, 0, inbytes.length, output, index);

            // Then base64 encode the bytes
            return Base64.encodeBytes(output, BASE64_OPTIONS);
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not encrypt value.", e);
        }
    }

    /**
     * Takes in a base64 encoded and encrypted String that was generated by a call
     * to {@link #encrypt(String)} and decrypts it.
     *
     * @param input the base64 String to decode and decrypt
     * @param request NO LONGER USED
     * @return the decrypted String
     * @throws GeneralSecurityException if the value cannot be decrypted for some reason. This
     *         can be caused by session expiration as it loses the original key.
     * @deprecated use {@link #decrypt(String)} instead
     */
    @Deprecated public static String decrypt(String input, HttpServletRequest request)
            throws GeneralSecurityException {

        return decrypt(input);
    }

    /**
     * Takes in a base64 encoded and encrypted String that was generated by a call
     * to {@link #encrypt(String)} and decrypts it.
     *
     * @param input the base64 String to decode and decrypt
     * @return the decrypted String
     * @throws GeneralSecurityException if the value cannot be decrypted for some reason. This
     *         can be caused by session expiration as it loses the original key.
     */
    public static String decrypt(String input)
            throws GeneralSecurityException {

        if (input == null) return null;

        // First un-base64 the String
        byte[] bytes = Base64.decode(input, BASE64_OPTIONS);

        // Then fetch a cipher and decrypt the bytes
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        byte[] output;
        try {
            output = cipher.doFinal(bytes);
        }
        catch (IllegalBlockSizeException e) {
            log.warn("Input was not encrypted with the current encryption key: ", input);
            return null;
        }
        catch (BadPaddingException e) {
            log.warn("Input was not encrypted with the current encryption key: ", input);
            return null;
        }

        // Check the hash code so we don't eat garbage
        if (!checkHashCode(output)) {
            log.warn("Input was not encrypted with the current encryption key: ", input);
            return null;
        }

        return new String(output, DISCARD_BYTES, output.length - DISCARD_BYTES);
    }

    /**
     * Gets the secret key that should be used to encrypt and decrypt values for the
     * current request.  If a key does not already exist in Session, one is created and
     * then deposited there for use later.
     *
     * @return a SecretKey that can be used to manufacture Ciphers
     */
    protected static Cipher getCipher(int mode) {
        try {
            SecretKey key = getSecretKey();

            // Then build a cipher for the correct mode
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(mode, key);
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

    /** Generates a nonce value using a secure random. */
    protected static byte[] nextNonce() {
        byte[] nonce = new byte[NONCE_SIZE];
        CryptoUtil.random.nextBytes(nonce);
        return nonce;
    }

    /** Generates and returns a hash code from the given byte arrays */
    protected static byte[] generateHashCode(byte[]... byteses) {
        long hash = HASH_CODE_SEED;
        for (int i = 0; i < byteses.length; i++) {
            byte[] bytes = byteses[i];
            for (int j = 0; j < bytes.length; j++) {
                hash = (((hash << 5) + hash) + bytes[j]);
            }
        }

        // convert to bytes
        byte[] hashBytes = new byte[HASH_CODE_SIZE];
        for (int i = HASH_CODE_SIZE - 1; i >= 0; i--) {
            hashBytes[i] = (byte) (hash & 0xff);
            hash >>>= 8;
        }
        return hashBytes;
    }

    /**
     * Checks the hash code in the first bytes of the value to make sure it is correct.
     * 
     * @param value byte array that contains the hash code and the bytes from which the hash code
     *            was generated
     * @return true if the hash code is valid; otherwise, false
     */
    protected static boolean checkHashCode(byte[] value) {
        // generate hash
        long hash = HASH_CODE_SEED;
        for (int i = HASH_CODE_SIZE; i < value.length; i++)
            hash = (((hash << 5) + hash) + value[i]);

        // compare to first bytes of array
        for (int i = HASH_CODE_SIZE - 1; i >= 0; i--) {
            if (value[i] != (byte) (hash & 0xff))
                return false;
            hash >>>= 8;
        }
        return true;
    }
}
