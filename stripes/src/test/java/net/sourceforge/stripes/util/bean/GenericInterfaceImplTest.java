package net.sourceforge.stripes.util.bean;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.mock.MockServletContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Tests a corner cases where a property's getter and/or setter method(s) are specified
 * in an interface using a type parameter. In this case the compiler generates bridge
 * method(s) that wrap the actual implementations of of the interface methods, and have
 * erased types!  This test ensures that BeanUtil does the right thing and finds the
 * non-bridge method(s) and determines their types.
 *
 * @author Tim Fennell
 */
public class GenericInterfaceImplTest extends FilterEnabledTestBase {
    /** An interface that has a type parameter for a property type. */
    public static interface GenericInterface<T> {
        T getProp();
        void setProp(T prop);
    }

    /** A simple implementation of a parameterized interface with a type argument. */
    public static class GenericImpl implements GenericInterface<GenericImpl> {
        GenericImpl prop;
        String stringProperty;

        public GenericImpl getProp() { return prop; }
        public void setProp(GenericImpl prop) { this.prop = prop; }

        public String getStringProperty() { return stringProperty; }
        public void setStringProperty(String stringProperty) { this.stringProperty = stringProperty; }
    }

    /** An interface with a type parameter and only a write method. */
    public static interface WriteOnlyGenericInterface<T> {
        void setProp(T prop);
    }

    /** An implementation of a parameterized interface with only a write method. */
    public static class WriteOnlyGenericImpl implements WriteOnlyGenericInterface<WriteOnlyGenericImpl> {
        public void setProp(WriteOnlyGenericImpl prop) {  }
    }

    @Test(groups="fast")
    public void testInheritFromGenericInterface() throws Exception {
        GenericImpl bean = new GenericImpl();
        BeanUtil.setPropertyValue("prop.stringProperty", bean, "whee");
        Assert.assertEquals(bean.getProp().getStringProperty(), "whee");
    }

    @Test(groups="fast")
    public void testInheritFromWriteOnlyGenericInterface() throws Exception {
        WriteOnlyGenericImpl bean = new WriteOnlyGenericImpl();
        Assert.assertEquals(BeanUtil.getPropertyType("prop", bean), WriteOnlyGenericImpl.class);
    }

}
