package net.sourceforge.stripes.util.bean;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.test.TestActionBean;
import net.sourceforge.stripes.test.TestBean;
import net.sourceforge.stripes.test.TestEnum;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 
 */
public class PropertyExpressionEvaluationTests extends FilterEnabledTestBase {

    @Test(groups="fast")
    public void testGetBasicPropertyType() {
        PropertyExpression expr = PropertyExpression.getExpression("singleLong");
        PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(expr, new TestActionBean());
        Class<?> type = eval.getType();
        Assert.assertEquals(type, Long.class);
    }

    @Test(groups="fast")
    public void testGetPropertyTypeWithPropertyAccess() {
        PropertyExpression expr = PropertyExpression.getExpression("publicLong");
        PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(expr, new TestActionBean());
        Class<?> type = eval.getType();
        Assert.assertEquals(type, Long.class);
    }

    @Test(groups="fast")
    public void testGetPropertyTypeForListOfLongs() {
        PropertyExpression expr = PropertyExpression.getExpression("listOfLongs[17]");
        PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(expr, new TestActionBean());
        Class<?> type = eval.getType();
        Assert.assertEquals(type, Long.class);
    }

    @Test(groups="fast")
    public void testGetPropertyTypeForReadThroughList() {
        PropertyExpression expr = PropertyExpression.getExpression("listOfBeans[3].enumProperty");
        PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(expr, new TestActionBean());
        Class<?> type = eval.getType();
        Assert.assertEquals(type, TestEnum.class);
    }

    @Test(groups="fast")
    public void testGetPropertyTypeWithBackToBackMapIndexing() {
        PropertyExpression expr = PropertyExpression.getExpression("nestedMap['foo']['bar']");
        PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(expr, new TestBean());
        Class<?> type = eval.getType();
        Assert.assertEquals(type, Boolean.class);
    }

    @Test(groups="fast")
    public void testGetPropertyTypeWithBackToBackListArrayIndexing() {
        PropertyExpression expr = PropertyExpression.getExpression("genericArray[1][0]");
        PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(expr, new TestBean());
        Class<?> type = eval.getType();
        Assert.assertEquals(type, Float.class);
    }

    //////////////////////////////////////////////////////////////////////////////////////

    @Test(groups="fast")
    public void testGetSimpleProperties() throws Exception {
        TestBean root = new TestBean();
        root.setStringProperty("testValue");
        root.setIntProperty(77);
        root.setLongProperty(777l);
        root.setEnumProperty(TestEnum.Seventh);

        Assert.assertEquals("testValue", BeanUtil.getPropertyValue("stringProperty", root));
        Assert.assertEquals(77, BeanUtil.getPropertyValue("intProperty", root));
        Assert.assertEquals(777l, BeanUtil.getPropertyValue("longProperty", root));
        Assert.assertEquals(TestEnum.Seventh, BeanUtil.getPropertyValue("enumProperty", root));
    }

    @Test(groups="fast")
    public void testGetNestedProperties() throws Exception {
        TestBean root = new TestBean();
        TestBean nested = new TestBean();
        root.setNestedBean(nested);
        nested.setStringProperty("testValue");
        nested.setIntProperty(77);
        nested.setLongProperty(777l);
        nested.setEnumProperty(TestEnum.Seventh);

        Assert.assertEquals("testValue", BeanUtil.getPropertyValue("nestedBean.stringProperty", root));
        Assert.assertEquals(77, BeanUtil.getPropertyValue("nestedBean.intProperty", root));
        Assert.assertEquals(777l, BeanUtil.getPropertyValue("nestedBean.longProperty", root));
        Assert.assertEquals(TestEnum.Seventh, BeanUtil.getPropertyValue("nestedBean.enumProperty", root));
    }

    @Test(groups="fast")
    public void testGetNullProperties() throws Exception {
        TestBean root = new TestBean();

        // Test simple and nested props, leaving out primitives
        Assert.assertNull( BeanUtil.getPropertyValue("stringProperty" , root) );
        Assert.assertNull( BeanUtil.getPropertyValue("longProperty" , root) );
        Assert.assertNull( BeanUtil.getPropertyValue("enumProperty" , root) );
        Assert.assertNull( BeanUtil.getPropertyValue("nestedBean.stringProperty" , root) );
        Assert.assertNull( BeanUtil.getPropertyValue("nestedBean.longProperty" , root) );
        Assert.assertNull( BeanUtil.getPropertyValue("nestedBean.enumProperty" , root) );

        Assert.assertNull( BeanUtil.getPropertyValue("stringArray", root));
        Assert.assertNull( BeanUtil.getPropertyValue("stringList[7]", root));
        Assert.assertNull( BeanUtil.getPropertyValue("stringList", root));
        Assert.assertNull( BeanUtil.getPropertyValue("stringMap['seven']", root));
        Assert.assertNull( BeanUtil.getPropertyValue("stringMap", root));
    }

    @Test(groups="fast")
    public void testSetThenGetSimpleProperties() throws Exception {
        TestBean root = new TestBean();

        BeanUtil.setPropertyValue("stringProperty", root, "testValue");
        BeanUtil.setPropertyValue("intProperty",    root, 77);
        BeanUtil.setPropertyValue("longProperty",   root, 777l);
        BeanUtil.setPropertyValue("enumProperty",   root, TestEnum.Seventh);

        Assert.assertEquals("testValue", root.getStringProperty());
        Assert.assertEquals("testValue", BeanUtil.getPropertyValue("stringProperty", root));

        Assert.assertEquals(77, root.getIntProperty());
        Assert.assertEquals(77, BeanUtil.getPropertyValue("intProperty", root));

        Assert.assertEquals(new Long(777l), root.getLongProperty());
        Assert.assertEquals(777l, BeanUtil.getPropertyValue("longProperty", root));

        Assert.assertEquals(TestEnum.Seventh, root.getEnumProperty());
        Assert.assertEquals(TestEnum.Seventh, BeanUtil.getPropertyValue("enumProperty", root));

    }

    @Test(groups="fast")
    public void testSetThenGetNestedProperties() throws Exception {
        TestBean root = new TestBean();

        BeanUtil.setPropertyValue("nestedBean.stringProperty", root, "testValue");
        BeanUtil.setPropertyValue("nestedBean.intProperty",    root, 77);
        BeanUtil.setPropertyValue("nestedBean.longProperty",   root, 777l);
        BeanUtil.setPropertyValue("nestedBean.enumProperty",   root, TestEnum.Seventh);

        Assert.assertEquals("testValue", root.getNestedBean().getStringProperty());
        Assert.assertEquals("testValue", BeanUtil.getPropertyValue("nestedBean.stringProperty", root));

        Assert.assertEquals(77, root.getNestedBean().getIntProperty());
        Assert.assertEquals(77, BeanUtil.getPropertyValue("nestedBean.intProperty", root));

        Assert.assertEquals(new Long(777l), root.getNestedBean().getLongProperty());
        Assert.assertEquals(777l, BeanUtil.getPropertyValue("nestedBean.longProperty", root));

        Assert.assertEquals(TestEnum.Seventh, root.getNestedBean().getEnumProperty());
        Assert.assertEquals(TestEnum.Seventh, BeanUtil.getPropertyValue("nestedBean.enumProperty", root));

        Assert.assertTrue(root.getNestedBean() == BeanUtil.getPropertyValue("nestedBean", root));
    }

    @Test(groups="fast")
    public void testListProperties() throws Exception {
        TestBean root = new TestBean();

        BeanUtil.setPropertyValue("beanList[3]", root, new TestBean());
        Assert.assertNull( BeanUtil.getPropertyValue("beanList[0]", root) );
        Assert.assertNull( BeanUtil.getPropertyValue("beanList[1]", root) );
        Assert.assertNull( BeanUtil.getPropertyValue("beanList[2]", root) );
        Assert.assertNotNull( BeanUtil.getPropertyValue("beanList[3]", root) );
        Assert.assertNull( BeanUtil.getPropertyValue("beanList[4]", root) );

        BeanUtil.setPropertyValue("beanList[3].stringProperty", root, "testValue");
        Assert.assertEquals("testValue", root.getBeanList().get(3).getStringProperty());

        BeanUtil.setPropertyValue("stringList[1]", root, "testValue");
        BeanUtil.setPropertyValue("stringList[5]", root, "testValue");
        BeanUtil.setPropertyValue("stringList[9]", root, "testValue");
        BeanUtil.setPropertyValue("stringList[7]", root, "testValue");
        BeanUtil.setPropertyValue("stringList[3]", root, "testValue");

        for (int i=0; i<10; ++i) {
            if (i%2 == 0) {
                Assert.assertNull(root.getStringList().get(i),
                                  "String list index " + i + " should be null.");
            }
            else {
                Assert.assertEquals("testValue", root.getStringList().get(i),
                                    "String list index " + i + " should be 'testValue'.");
            }
        }
    }

    @Test(groups="fast")
    public void testMapProperties() throws Exception {
        TestBean root = new TestBean();

        Assert.assertNull( BeanUtil.getPropertyValue("stringMap['foo']", root) );
        Assert.assertNull( BeanUtil.getPropertyValue("stringMap['bar']", root) );
        Assert.assertNull( BeanUtil.getPropertyValue("stringMap['testValue']", root) );

        Assert.assertNull( BeanUtil.getPropertyValue("beanMap['foo']", root) );
        Assert.assertNull( BeanUtil.getPropertyValue("beanMap['bar']", root) );
        Assert.assertNull( BeanUtil.getPropertyValue("beanMap['testValue']", root) );

        Assert.assertNull( BeanUtil.getPropertyValue("beanMap['foo'].longProperty", root) );
        Assert.assertNull( BeanUtil.getPropertyValue("beanMap['bar'].stringProperty", root) );
        Assert.assertNull( BeanUtil.getPropertyValue("beanMap['testValue'].enumProperty", root) );

        BeanUtil.setPropertyValue("stringMap['testKey']", root, "testValue");
        Assert.assertEquals("testValue", root.getStringMap().get("testKey"));

        BeanUtil.setPropertyValue("beanMap['testKey'].enumProperty", root, TestEnum.Fifth);
        Assert.assertNotNull(root.getBeanMap());
        Assert.assertNotNull(root.getBeanMap().get("testKey"));
        Assert.assertEquals(TestEnum.Fifth, root.getBeanMap().get("testKey").getEnumProperty());
    }

    @Test(groups="fast")
    public void testSetNull() throws Exception {
        TestBean root = new TestBean();

        // Try setting a null on a null nested property and make sure the nested
        // property doesn't get instantiated
        Assert.assertNull(root.getNestedBean());
        BeanUtil.setPropertyToNull("nestedBean.stringProperty", root);
        Assert.assertNull(root.getNestedBean());

        // Now set the property set the nest bean and do it again
        root.setNestedBean( new TestBean() );
        BeanUtil.setPropertyToNull("nestedBean.stringProperty", root);
        Assert.assertNotNull(root.getNestedBean());
        Assert.assertNull(root.getNestedBean().getStringProperty());

        // Now set the string property and null it out for real
        root.getNestedBean().setStringProperty("Definitely Not Null");
        BeanUtil.setPropertyToNull("nestedBean.stringProperty", root);
        Assert.assertNotNull(root.getNestedBean());
        Assert.assertNull(root.getNestedBean().getStringProperty());

        // Now use setNullValue to trim the nestedBean
        BeanUtil.setPropertyToNull("nestedBean", root);
        Assert.assertNull(root.getNestedBean());

        // Now try some nulling out of indexed properties
        root.setStringList(new ArrayList<String>());
        root.getStringList().add("foo");
        root.getStringList().add("bar");
        Assert.assertNotNull(root.getStringList().get(0));
        Assert.assertNotNull(root.getStringList().get(1));
        BeanUtil.setPropertyToNull("stringList[1]", root);
        Assert.assertNotNull(root.getStringList().get(0));
        Assert.assertNull(root.getStringList().get(1));
    }

    @Test(groups="fast")
    public void testSetNullPrimitives() throws Exception {
        TestBean root = new TestBean();
        root.setBooleanProperty(true);
        root.setIntProperty(77);

        Assert.assertEquals(77, root.getIntProperty());
        Assert.assertEquals(true, root.isBooleanProperty());

        BeanUtil.setPropertyToNull("intProperty", root);
        BeanUtil.setPropertyToNull("booleanProperty", root);

        Assert.assertEquals(0, root.getIntProperty());
        Assert.assertEquals(false, root.isBooleanProperty());
    }

    /**
     * Tests a bug whereby the Introspector/PropertyDescriptor returns inaccessible
     * methods for getKey() and getValue() on all the JDK implementations of Map. A general
     * fix has been implmented to work up the chain and find an accessible method to invoke.
     */
    @Test(groups="fast")
    public void testMapEntryPropertyDescriptorBug() throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        map.put("key", "value");
        Map.Entry<String,String> entry = map.entrySet().iterator().next();
        String value = (String) BeanUtil.getPropertyValue("value", entry);
        Assert.assertEquals(value, "value");
    }

    /**
     * Fix for a problem whereby in certain circumstances the PropertyExpressionEvaluation
     * would not fall back to looking at instance information to figure out the type info.
     */
    @Test(groups="fast")
    public void testFallBackToInstanceInfo() throws Exception {
        Map<String,TestBean> map = new HashMap<String,TestBean>();
        map.put("foo", new TestBean());
        map.get("foo").setStringProperty("bar");
        Map.Entry<String,TestBean> entry = map.entrySet().iterator().next();
        String value = (String) BeanUtil.getPropertyValue("value.stringProperty", entry);
        Assert.assertEquals(value, "bar");
    }

    /** Following classes are part of an inheritance torture test! */
    public static class Wombat { public String getName() { return "Wombat"; } }
    public static class SubWombat extends Wombat { @Override public String getName() { return "SubWombat"; } }
    public static class Foo<P extends Wombat> {
        private P wombat;
        public P getWombat() { return this.wombat; }
        public void setWombat(P wombat) { this.wombat = wombat; }
    }
    public static class Owner {
        private Foo<SubWombat> foo;
        public Foo<SubWombat> getFoo() { return foo; }
        public void setFoo(final Foo<SubWombat> foo) { this.foo = foo; }
    }

    @Test(groups="fast")
    public void testGnarlyInheritanceAndGenerics() throws Exception {
        Owner owner = new Owner();
        Foo<SubWombat> foo = new Foo<SubWombat>();
        owner.setFoo(foo);
        foo.setWombat(new SubWombat());
        String value = (String) BeanUtil.getPropertyValue("foo.wombat.name", owner);
        Assert.assertEquals(value, "SubWombat");
    }


}
