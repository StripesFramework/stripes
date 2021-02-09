package net.sourceforge.stripes.util.bean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import net.sourceforge.stripes.FilterEnabledTestBase;


/**
 * Tests a corner cases where a property's getter and/or setter method(s) are specified
 * in an interface using a type parameter. In this case the compiler generates bridge
 * method(s) that wrap the actual implementations of of the interface methods, and have
 * erased types!  This test ensures that BeanUtil does the right thing and finds the
 * non-bridge method(s) and determines their types.
 *
 * @author Tim Fennell
 */
@SuppressWarnings("unused")
public class GenericInterfaceImplTest extends FilterEnabledTestBase {

   @Test
   public void testInheritFromGenericInterface() {
      GenericImpl bean = new GenericImpl();
      BeanUtil.setPropertyValue("prop.stringProperty", bean, "whee");
      assertThat(bean.getProp().getStringProperty()).isEqualTo("whee");
   }

   @Test
   public void testInheritFromWriteOnlyGenericInterface() {
      WriteOnlyGenericImpl bean = new WriteOnlyGenericImpl();
      assertThat(BeanUtil.getPropertyType("prop", bean)).isSameAs(WriteOnlyGenericImpl.class);
   }

   /** An interface that has a type parameter for a property type. */
   public interface GenericInterface<T> {

      T getProp();

      void setProp( T prop );
   }


   /** An interface with a type parameter and only a write method. */
   public interface WriteOnlyGenericInterface<T> {

      void setProp( T prop );
   }


   /** A simple implementation of a parameterized interface with a type argument. */
   public static class GenericImpl implements GenericInterface<GenericImpl> {

      GenericImpl prop;
      String      stringProperty;

      @Override
      public GenericImpl getProp() { return prop; }

      public String getStringProperty() { return stringProperty; }

      @Override
      public void setProp( GenericImpl prop ) { this.prop = prop; }

      public void setStringProperty( String stringProperty ) { this.stringProperty = stringProperty; }
   }


   /** An implementation of a parameterized interface with only a write method. */
   public static class WriteOnlyGenericImpl implements WriteOnlyGenericInterface<WriteOnlyGenericImpl> {

      @Override
      public void setProp( WriteOnlyGenericImpl prop ) { }
   }

}
