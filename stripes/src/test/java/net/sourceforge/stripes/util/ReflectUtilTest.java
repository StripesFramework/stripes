package net.sourceforge.stripes.util;

import org.testng.annotations.Test;
import org.testng.Assert;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyDescriptor;

/**
 * Tests for the ReflectUtil class
 *
 * @author Tim Fennell
 */
public class ReflectUtilTest {

    @Test(groups = "fast")
    public void testAccessibleMethodBaseCase() throws Exception {
        Method m = Object.class.getMethod("getClass");
        Method m2 = ReflectUtil.findAccessibleMethod(m);
        Assert.assertSame(m, m2);
    }

    @Test(groups = "fast")
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

    @Test(groups = "fast")
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
        Assert.assertNotNull(pd.getReadMethod(), "Read method is null");
        Assert.assertNull(pd.getWriteMethod(), "Write method is not null");

        pd = ReflectUtil.getPropertyDescriptor(RWSub.class, "id");
        Assert.assertNotNull(pd.getReadMethod(), "Read method is null");
        Assert.assertNotNull(pd.getWriteMethod(), "Write method is null");
    }

    interface A<S, T, U> {
    }
    interface B<V extends Number, W> extends A<W, String, V> {
    }
    interface C extends B<Integer, Long> {
    }

    @Test(groups = "fast")
    public void testResolveTypeArgsOfSuperInterface() {
        class Impl implements C {
        }
        Type[] typeArgs = ReflectUtil.getActualTypeArguments(Impl.class, A.class);
        Assert.assertEquals(typeArgs.length, 3);
        Assert.assertEquals(typeArgs[0], Long.class);
        Assert.assertEquals(typeArgs[1], String.class);
        Assert.assertEquals(typeArgs[2], Integer.class);
    }

    @Test(groups = "fast")
    public void testResolveTypeArgsOfSuperclass() {
        abstract class BaseClass1<S, T, U> {
        }
        abstract class BaseClass2<V, W> extends BaseClass1<W, String, V> {}
        class Impl1<X> extends BaseClass2<Integer, X> {
        }
        class Impl2 extends Impl1<Long> {
        }
        class Impl3 extends Impl2 {
        }
        Type[] typeArgs = ReflectUtil.getActualTypeArguments(Impl3.class, BaseClass1.class);
        Assert.assertEquals(typeArgs.length, 3);
        Assert.assertEquals(typeArgs[0], Long.class);
        Assert.assertEquals(typeArgs[1], String.class);
        Assert.assertEquals(typeArgs[2], Integer.class);
    }
}
