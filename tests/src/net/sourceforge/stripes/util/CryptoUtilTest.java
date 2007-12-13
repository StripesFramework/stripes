package net.sourceforge.stripes.util;

import org.testng.annotations.Test;
import org.testng.Assert;
import net.sourceforge.stripes.mock.MockHttpServletRequest;
import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.StripesTestFixture;

import javax.servlet.http.HttpServletRequest;

/**
 * Basic tests for the CryptoUtil
 *
 * @author Tim Fennell
 */
public class CryptoUtilTest {

    /** Manufactures a mock request. */
    protected HttpServletRequest getRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("/foo", "/bar");
        request.setSession(new MockHttpSession(StripesTestFixture.getServletContext()));
        return request;
    }

    @Test(groups="fast")
    public void basicEncryptionTest() throws Exception {
        HttpServletRequest request = getRequest();

        String input = "A Basic String to Encrypt";
        String encrypted = CryptoUtil.encrypt(input, request);
        String decrypted = CryptoUtil.decrypt(encrypted, request);

        Assert.assertFalse(input.equals(encrypted), "Encrypted string should be different!");
        Assert.assertTrue(input.equals(decrypted), "Decrypted string should match!");
    }

    @Test(groups="fast")
    public void encryptEmptyStringTest() throws Exception {
        HttpServletRequest request = getRequest();

        String input = "";
        String encrypted = CryptoUtil.encrypt(input, request);
        String decrypted = CryptoUtil.decrypt(encrypted, request);

        Assert.assertFalse(input.equals(encrypted), "Encrypted string should be different!");
        Assert.assertTrue(input.equals(decrypted), "Decrypted string should match!");
    }

    @Test(groups="fast")
    public void encryptNullTest() throws Exception {
        HttpServletRequest request = getRequest();

        String input = null;
        String encrypted = CryptoUtil.encrypt(input, request);

        Assert.assertNull(encrypted, "Encrypting null should give back null.");
    }

    @Test(groups="fast")
    public void decryptNullTest() throws Exception {
        HttpServletRequest request = getRequest();

        String input = null;
        String decrypted = CryptoUtil.decrypt(input, request);

        Assert.assertNull(decrypted, "Decrypting null should give back null.");
    }
    
    @Test(groups = "fast")
    public void decryptBogusInputTest() throws Exception {
        String input = CryptoUtil.encrypt("This is bogus!", getRequest());
        String decrypted = CryptoUtil.decrypt(input, getRequest());
        Assert.assertNull(decrypted, "Decrypting a bogus input should give back null.");
    }
}
