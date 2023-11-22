package net.sourceforge.stripes.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the ReflectUtil class
 *
 * @author Tim Fennell
 */
public class ReflectUtilTest {
  @Test
  public void testAccessibleMethodBaseCase() throws Exception {
    Method m = Object.class.getMethod("getClass");
    Method m2 = ReflectUtil.findAccessibleMethod(m);
    Assert.assertSame(m, m2);
  }

  @Test
  public void testAccessibleMethodWithMapEntry() throws Exception {
    Map<String, String> map = new HashMap<String, String>();
    map.put("foo", "bar");
    Map.Entry<String, String> entry = map.entrySet().iterator().next();
    PropertyDescriptor pd = ReflectUtil.getPropertyDescriptor(entry.getClass(), "value");
    Method m = pd.getReadMethod();
    m = ReflectUtil.findAccessibleMethod(m);
    String value = (String) m.invoke(entry);
    Assert.assertEquals(value, "bar");
  }

  @Test
  public void testCovariantProperty() {
    abstract class Base {
      abstract Object getId();
    }

    class ROSub extends Base {
      protected String id;

      @Override
      public String getId() {
        return id;
      }
    }

    class RWSub extends ROSub {
      @SuppressWarnings("unused")
      public void setId(String id) {
        this.id = id;
      }
    }

    PropertyDescriptor pd = ReflectUtil.getPropertyDescriptor(ROSub.class, "id");
    Assert.assertNotNull("Read method is null", pd.getReadMethod());
    Assert.assertNull("Write method is not null", pd.getWriteMethod());

    pd = ReflectUtil.getPropertyDescriptor(RWSub.class, "id");
    Assert.assertNotNull("Read method is null", (pd.getReadMethod()));
    Assert.assertNotNull("Write method is null", pd.getWriteMethod());
  }
}
