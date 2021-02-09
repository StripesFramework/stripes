package net.sourceforge.stripes.util.bean;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.testbeans.TestActionBean;
import net.sourceforge.stripes.testbeans.TestBean;
import net.sourceforge.stripes.testbeans.TestEnum;


public class PropertyExpressionEvaluationTests extends FilterEnabledTestBase {

   /**
    * Fix for a problem whereby in certain circumstances the PropertyExpressionEvaluation
    * would not fall back to looking at instance information to figure out the type info.
    */
   @Test
   public void testFallBackToInstanceInfo() {
      Map<String, TestBean> map = new HashMap<>();
      map.put("foo", new TestBean());
      map.get("foo").setStringProperty("bar");
      Map.Entry<String, TestBean> entry = map.entrySet().iterator().next();
      String value = (String)BeanUtil.getPropertyValue("value.stringProperty", entry);
      assertThat(value).isEqualTo("bar");
   }

   @Test
   public void testGetBasicPropertyType() {
      PropertyExpression expr = PropertyExpression.getExpression("singleLong");
      PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(expr, new TestActionBean());
      assertThat(eval.getType()).isSameAs(Long.class);
   }

   @Test
   public void testGetNestedProperties() {
      TestBean root = new TestBean();
      TestBean nested = new TestBean();
      root.setNestedBean(nested);
      nested.setStringProperty("testValue");
      nested.setIntProperty(77);
      nested.setLongProperty(777L);
      nested.setEnumProperty(TestEnum.Seventh);

      assertThat(BeanUtil.getPropertyValue("nestedBean.stringProperty", root)).isEqualTo("testValue");
      assertThat(BeanUtil.getPropertyValue("nestedBean.intProperty", root)).isEqualTo(77);
      assertThat(BeanUtil.getPropertyValue("nestedBean.longProperty", root)).isEqualTo(777L);
      assertThat(BeanUtil.getPropertyValue("nestedBean.enumProperty", root)).isEqualTo(TestEnum.Seventh);
   }

   @Test
   public void testGetNullProperties() {
      TestBean root = new TestBean();

      // Test simple and nested props, leaving out primitives
      assertThat(BeanUtil.getPropertyValue("stringProperty", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("longProperty", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("enumProperty", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("nestedBean.stringProperty", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("nestedBean.longProperty", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("nestedBean.enumProperty", root)).isNull();

      assertThat(BeanUtil.getPropertyValue("stringArray", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("stringList[7]", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("stringList", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("stringMap['seven']", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("stringMap", root)).isNull();
   }

   @Test
   public void testGetPropertyTypeForListOfLongs() {
      PropertyExpression expr = PropertyExpression.getExpression("listOfLongs[17]");
      PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(expr, new TestActionBean());
      assertThat(eval.getType()).isSameAs(Long.class);
   }

   @Test
   public void testGetPropertyTypeForReadThroughList() {
      PropertyExpression expr = PropertyExpression.getExpression("listOfBeans[3].enumProperty");
      PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(expr, new TestActionBean());
      assertThat(eval.getType()).isSameAs(TestEnum.class);
   }

   @Test
   public void testGetPropertyTypeWithBackToBackListArrayIndexing() {
      PropertyExpression expr = PropertyExpression.getExpression("genericArray[1][0]");
      PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(expr, new TestBean());
      assertThat(eval.getType()).isSameAs(Float.class);
   }

   @Test
   public void testGetPropertyTypeWithBackToBackMapIndexing() {
      PropertyExpression expr = PropertyExpression.getExpression("nestedMap['foo']['bar']");
      PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(expr, new TestBean());
      assertThat(eval.getType()).isSameAs(Boolean.class);
   }

   @Test
   public void testGetPropertyTypeWithPropertyAccess() {
      PropertyExpression expr = PropertyExpression.getExpression("publicLong");
      PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(expr, new TestActionBean());
      assertThat(eval.getType()).isSameAs(Long.class);
   }

   @Test
   public void testGetSimpleProperties() {
      TestBean root = new TestBean();
      root.setStringProperty("testValue");
      root.setIntProperty(77);
      root.setLongProperty(777L);
      root.setEnumProperty(TestEnum.Seventh);

      assertThat(BeanUtil.getPropertyValue("stringProperty", root)).isEqualTo("testValue");
      assertThat(BeanUtil.getPropertyValue("intProperty", root)).isEqualTo(77);
      assertThat(BeanUtil.getPropertyValue("longProperty", root)).isEqualTo(777L);
      assertThat(BeanUtil.getPropertyValue("enumProperty", root)).isEqualTo(TestEnum.Seventh);
   }

   @Test
   public void testGnarlyInheritanceAndGenerics() {
      Owner owner = new Owner();
      Foo<SubWombat> foo = new Foo<>();
      owner.setFoo(foo);
      foo.setWombat(new SubWombat());

      String value = (String)BeanUtil.getPropertyValue("foo.wombat.name", owner);

      assertThat(value).isEqualTo("SubWombat");
   }

   @Test
   public void testListProperties() {
      TestBean root = new TestBean();

      BeanUtil.setPropertyValue("beanList[3]", root, new TestBean());
      assertThat(BeanUtil.getPropertyValue("beanList[0]", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("beanList[1]", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("beanList[2]", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("beanList[3]", root)).isNotNull();
      assertThat(BeanUtil.getPropertyValue("beanList[4]", root)).isNull();

      BeanUtil.setPropertyValue("beanList[3].stringProperty", root, "testValue");
      assertThat(root.getBeanList().get(3).getStringProperty()).isEqualTo("testValue");

      BeanUtil.setPropertyValue("stringList[1]", root, "testValue");
      BeanUtil.setPropertyValue("stringList[5]", root, "testValue");
      BeanUtil.setPropertyValue("stringList[9]", root, "testValue");
      BeanUtil.setPropertyValue("stringList[7]", root, "testValue");
      BeanUtil.setPropertyValue("stringList[3]", root, "testValue");

      for ( int i = 0; i < 10; ++i ) {
         if ( i % 2 == 0 ) {
            assertThat(root.getStringList().get(i)).describedAs("String list index " + i + " should be null.").isNull();
         } else {
            assertThat(root.getStringList().get(i)).describedAs("String list index " + i + " should be 'testValue'.").isEqualTo("testValue");
         }
      }
   }

   /**
    * Tests a bug whereby the Introspector/PropertyDescriptor returns inaccessible
    * methods for getKey() and getValue() on all the JDK implementations of Map. A general
    * fix has been implmented to work up the chain and find an accessible method to invoke.
    */
   @Test
   public void testMapEntryPropertyDescriptorBug() {
      Map<String, String> map = new HashMap<>();
      map.put("key", "value");
      Map.Entry<String, String> entry = map.entrySet().iterator().next();
      String value = (String)BeanUtil.getPropertyValue("value", entry);
      assertThat(value).isEqualTo("value");
   }

   @Test
   public void testMapProperties() {
      TestBean root = new TestBean();

      assertThat(BeanUtil.getPropertyValue("stringMap['foo']", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("stringMap['bar']", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("stringMap['testValue']", root)).isNull();

      assertThat(BeanUtil.getPropertyValue("beanMap['foo']", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("beanMap['bar']", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("beanMap['testValue']", root)).isNull();

      assertThat(BeanUtil.getPropertyValue("beanMap['foo'].longProperty", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("beanMap['bar'].stringProperty", root)).isNull();
      assertThat(BeanUtil.getPropertyValue("beanMap['testValue'].enumProperty", root)).isNull();

      BeanUtil.setPropertyValue("stringMap['testKey']", root, "testValue");
      assertThat(root.getStringMap().get("testKey")).isEqualTo("testValue");

      BeanUtil.setPropertyValue("beanMap['testKey'].enumProperty", root, TestEnum.Fifth);
      assertThat(root.getBeanMap()).isNotNull();
      assertThat(root.getBeanMap().get("testKey")).isNotNull();
      assertThat(root.getBeanMap().get("testKey").getEnumProperty()).isEqualTo(TestEnum.Fifth);
   }

   @Test
   public void testSetNull() {
      TestBean root = new TestBean();

      // Try setting a null on a null nested property and make sure the nested
      // property doesn't get instantiated
      assertThat(root.getNestedBean()).isNull();
      BeanUtil.setPropertyToNull("nestedBean.stringProperty", root);
      assertThat(root.getNestedBean()).isNull();

      // Now set the property set the nest bean and do it again
      root.setNestedBean(new TestBean());
      BeanUtil.setPropertyToNull("nestedBean.stringProperty", root);
      assertThat(root.getNestedBean()).isNotNull();
      assertThat(root.getNestedBean().getStringProperty()).isNull();

      // Now set the string property and null it out for real
      root.getNestedBean().setStringProperty("Definitely Not Null");
      BeanUtil.setPropertyToNull("nestedBean.stringProperty", root);
      assertThat(root.getNestedBean()).isNotNull();
      assertThat(root.getNestedBean().getStringProperty()).isNull();

      // Now use setNullValue to trim the nestedBean
      BeanUtil.setPropertyToNull("nestedBean", root);
      assertThat(root.getNestedBean()).isNull();

      // Now try some nulling out of indexed properties
      root.setStringList(new ArrayList<>());
      root.getStringList().add("foo");
      root.getStringList().add("bar");
      assertThat(root.getStringList().get(0)).isNotNull();
      assertThat(root.getStringList().get(1)).isNotNull();
      BeanUtil.setPropertyToNull("stringList[1]", root);
      assertThat(root.getStringList().get(0)).isNotNull();
      assertThat(root.getStringList().get(1)).isNull();
   }

   @Test
   public void testSetNullPrimitives() {
      TestBean root = new TestBean();
      root.setBooleanProperty(true);
      root.setIntProperty(77);

      assertThat(root.getIntProperty()).isEqualTo(77);
      assertThat(root.isBooleanProperty()).isTrue();

      BeanUtil.setPropertyToNull("intProperty", root);
      BeanUtil.setPropertyToNull("booleanProperty", root);

      assertThat(root.getIntProperty()).isEqualTo(0);
      assertThat(root.isBooleanProperty()).isFalse();
   }

   @Test
   public void testSetThenGetNestedProperties() {
      TestBean root = new TestBean();

      BeanUtil.setPropertyValue("nestedBean.stringProperty", root, "testValue");
      BeanUtil.setPropertyValue("nestedBean.intProperty", root, 77);
      BeanUtil.setPropertyValue("nestedBean.longProperty", root, 777L);
      BeanUtil.setPropertyValue("nestedBean.enumProperty", root, TestEnum.Seventh);

      assertThat(root.getNestedBean().getStringProperty()).isEqualTo("testValue");
      assertThat(BeanUtil.getPropertyValue("nestedBean.stringProperty", root)).isEqualTo("testValue");

      assertThat(root.getNestedBean().getIntProperty()).isEqualTo(77);
      assertThat(BeanUtil.getPropertyValue("nestedBean.intProperty", root)).isEqualTo(77);

      assertThat(root.getNestedBean().getLongProperty()).isEqualTo(777L);
      assertThat(BeanUtil.getPropertyValue("nestedBean.longProperty", root)).isEqualTo(777L);

      assertThat(root.getNestedBean().getEnumProperty()).isEqualTo(TestEnum.Seventh);
      assertThat(BeanUtil.getPropertyValue("nestedBean.enumProperty", root)).isEqualTo(TestEnum.Seventh);

      assertThat(BeanUtil.getPropertyValue("nestedBean", root)).isSameAs(root.getNestedBean());
   }

   @Test
   public void testSetThenGetSimpleProperties() {
      TestBean root = new TestBean();

      BeanUtil.setPropertyValue("stringProperty", root, "testValue");
      BeanUtil.setPropertyValue("intProperty", root, 77);
      BeanUtil.setPropertyValue("longProperty", root, 777L);
      BeanUtil.setPropertyValue("enumProperty", root, TestEnum.Seventh);

      assertThat(root.getStringProperty()).isEqualTo("testValue");
      assertThat(BeanUtil.getPropertyValue("stringProperty", root)).isEqualTo("testValue");

      assertThat(root.getIntProperty()).isEqualTo(77);
      assertThat(BeanUtil.getPropertyValue("intProperty", root)).isEqualTo(77);

      assertThat(root.getLongProperty()).isEqualTo(777L);
      assertThat(BeanUtil.getPropertyValue("longProperty", root)).isEqualTo(777L);

      assertThat(root.getEnumProperty()).isEqualTo(TestEnum.Seventh);
      assertThat(BeanUtil.getPropertyValue("enumProperty", root)).isEqualTo(TestEnum.Seventh);
   }

   @SuppressWarnings("unused")
   public static class Foo<P extends Wombat> {

      private P wombat;

      public P getWombat() { return wombat; }

      public void setWombat( P wombat ) { this.wombat = wombat; }
   }


   public static class Owner {

      private Foo<SubWombat> foo;

      public Foo<SubWombat> getFoo() { return foo; }

      public void setFoo( final Foo<SubWombat> foo ) { this.foo = foo; }
   }


   public static class SubWombat extends Wombat {

      @Override
      public String getName() { return "SubWombat"; }
   }


   /** Following classes are part of an inheritance torture test! */
   public static class Wombat {

      public String getName() { return "Wombat"; }
   }

}
