package net.sourceforge.stripes.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;


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
      assertThat(m2).isSameAs(m);
   }

   @Test
   public void testAccessibleMethodWithMapEntry() throws Exception {
      Map<String, String> map = new HashMap<>();
      map.put("foo", "bar");
      Map.Entry<String, String> entry = map.entrySet().iterator().next();
      PropertyDescriptor pd = ReflectUtil.getPropertyDescriptor(entry.getClass(), "value");
      Method m = pd.getReadMethod();
      m = ReflectUtil.findAccessibleMethod(m);
      String value = (String)m.invoke(entry);

      assertThat(value).isEqualTo("bar");
   }

   @Test
   public void testCovariantProperty() {
      @SuppressWarnings("unused")
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
         public void setId( String id ) {
            this.id = id;
         }
      }

      PropertyDescriptor pd = ReflectUtil.getPropertyDescriptor(ROSub.class, "id");
      assertThat(pd.getReadMethod()).isNotNull();
      assertThat(pd.getWriteMethod()).isNull();

      pd = ReflectUtil.getPropertyDescriptor(RWSub.class, "id");
      assertThat(pd.getReadMethod()).isNotNull();
      assertThat(pd.getWriteMethod()).isNotNull();
   }
}
