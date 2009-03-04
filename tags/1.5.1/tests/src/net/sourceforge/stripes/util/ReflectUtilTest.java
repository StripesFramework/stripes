package net.sourceforge.stripes.util;

import org.testng.annotations.Test;
import org.testng.Assert;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyDescriptor;

/**
 * Tests for the ReflectUtil class
 *
 * @author Tim Fennell
 */
public class ReflectUtilTest {
    @Test(groups="fast")
    public void testAccessibleMethodBaseCase() throws Exception {
        Method m = Object.class.getMethod("getClass");
        Method m2 = ReflectUtil.findAccessibleMethod(m);
        Assert.assertSame(m, m2);
    }

    @Test(groups="fast")
    public void testAccessibleMethodWithMapEntry() throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        map.put("foo", "bar");
        Map.Entry<String,String> entry = map.entrySet().iterator().next();
        PropertyDescriptor pd = ReflectUtil.getPropertyDescriptor(entry.getClass(), "value");
        Method m = pd.getReadMethod();

        // The returned method should fail
        try {
            m.invoke(entry);
            Assert.fail("Method should have thrown an illegal access exception!");
        }
        catch (IllegalAccessException iae) { /* This is expected. */ }

        // Now try doing it right!
        m = ReflectUtil.findAccessibleMethod(m);
        String value = (String) m.invoke(entry);
        Assert.assertEquals(value, "bar");
    }
}
