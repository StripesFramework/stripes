package net.sourceforge.stripes.util;

import org.testng.annotations.Test;
import org.testng.Assert;
import net.sourceforge.stripes.test.TestBean;
import net.sourceforge.stripes.test.TestEnum;

/**
 * TestNG test class that exercises the OgnlUtil class to ensure that the
 * custom null handling and list index handling is working properly.
 *
 * @author Tim Fennell
 */
public class OgnlUtilTest {

     @Test
     public void testGetSimpleProperties() throws Exception {
         TestBean root = new TestBean();
         root.setStringProperty("testValue");
         root.setIntProperty(77);
         root.setLongProperty(777l);
         root.setEnumProperty(TestEnum.Seventh);

         Assert.assertEquals("testValue", OgnlUtil.getValue("stringProperty", root));
         Assert.assertEquals(77, OgnlUtil.getValue("intProperty", root));
         Assert.assertEquals(777l, OgnlUtil.getValue("longProperty", root));
         Assert.assertEquals(TestEnum.Seventh, OgnlUtil.getValue("enumProperty", root));
     }

    @Test
    public void testGetNestedProperties() throws Exception {
        TestBean root = new TestBean();
        TestBean nested = new TestBean();
        root.setNestedBean(nested);
        nested.setStringProperty("testValue");
        nested.setIntProperty(77);
        nested.setLongProperty(777l);
        nested.setEnumProperty(TestEnum.Seventh);

        Assert.assertEquals("testValue", OgnlUtil.getValue("nestedBean.stringProperty", root));
        Assert.assertEquals(77, OgnlUtil.getValue("nestedBean.intProperty", root));
        Assert.assertEquals(777l, OgnlUtil.getValue("nestedBean.longProperty", root));
        Assert.assertEquals(TestEnum.Seventh, OgnlUtil.getValue("nestedBean.enumProperty", root));
    }

    @Test
    public void testGetNullProperties() throws Exception {
        TestBean root = new TestBean();

        // Test simple and nested props, leaving out primitives
        Assert.assertNull( OgnlUtil.getValue("stringProperty" , root) );
        Assert.assertNull( OgnlUtil.getValue("longProperty" , root) );
        Assert.assertNull( OgnlUtil.getValue("enumProperty" , root) );
        Assert.assertNull( OgnlUtil.getValue("nestedBean.stringProperty" , root) );
        Assert.assertNull( OgnlUtil.getValue("nestedBean.longProperty" , root) );
        Assert.assertNull( OgnlUtil.getValue("nestedBean.enumProperty" , root) );

        Assert.assertNull( OgnlUtil.getValue("stringArray", root));
        Assert.assertNull( OgnlUtil.getValue("stringList[7]", root));
        Assert.assertNull( OgnlUtil.getValue("stringList", root));
        Assert.assertNull( OgnlUtil.getValue("stringMap['seven']", root));
        Assert.assertNull( OgnlUtil.getValue("stringMap", root));
    }

    @Test
    public void testSetThenGetSimpleProperties() throws Exception {
        TestBean root = new TestBean();

        OgnlUtil.setValue("stringProperty", root, "testValue");
        OgnlUtil.setValue("intProperty",    root, 77);
        OgnlUtil.setValue("longProperty",   root, 777l);
        OgnlUtil.setValue("enumProperty",   root, TestEnum.Seventh);

        Assert.assertEquals("testValue", root.getStringProperty());
        Assert.assertEquals("testValue", OgnlUtil.getValue("stringProperty", root));

        Assert.assertEquals(77, root.getIntProperty());
        Assert.assertEquals(77, OgnlUtil.getValue("intProperty", root));

        Assert.assertEquals(new Long(777l), root.getLongProperty());
        Assert.assertEquals(777l, OgnlUtil.getValue("longProperty", root));

        Assert.assertEquals(TestEnum.Seventh, root.getEnumProperty());
        Assert.assertEquals(TestEnum.Seventh, OgnlUtil.getValue("enumProperty", root));

    }

    @Test
    public void testSetThenGetNestedProperties() throws Exception {
        TestBean root = new TestBean();

        OgnlUtil.setValue("nestedBean.stringProperty", root, "testValue");
        OgnlUtil.setValue("nestedBean.intProperty",    root, 77);
        OgnlUtil.setValue("nestedBean.longProperty",   root, 777l);
        OgnlUtil.setValue("nestedBean.enumProperty",   root, TestEnum.Seventh);

        Assert.assertEquals("testValue", root.getNestedBean().getStringProperty());
        Assert.assertEquals("testValue", OgnlUtil.getValue("nestedBean.stringProperty", root));

        Assert.assertEquals(77, root.getNestedBean().getIntProperty());
        Assert.assertEquals(77, OgnlUtil.getValue("nestedBean.intProperty", root));

        Assert.assertEquals(new Long(777l), root.getNestedBean().getLongProperty());
        Assert.assertEquals(777l, OgnlUtil.getValue("nestedBean.longProperty", root));

        Assert.assertEquals(TestEnum.Seventh, root.getNestedBean().getEnumProperty());
        Assert.assertEquals(TestEnum.Seventh, OgnlUtil.getValue("nestedBean.enumProperty", root));

        Assert.assertTrue(root.getNestedBean() == OgnlUtil.getValue("nestedBean", root));
    }

    @Test
    public void testListProperties() throws Exception {
        TestBean root = new TestBean();

        OgnlUtil.setValue("beanList[3]", root, new TestBean());
        Assert.assertNull( OgnlUtil.getValue("beanList[0]", root) );
        Assert.assertNull( OgnlUtil.getValue("beanList[1]", root) );
        Assert.assertNull( OgnlUtil.getValue("beanList[2]", root) );
        Assert.assertNotNull( OgnlUtil.getValue("beanList[3]", root) );
        Assert.assertNull( OgnlUtil.getValue("beanList[4]", root) );

        OgnlUtil.setValue("beanList[3].stringProperty", root, "testValue");
        Assert.assertEquals("testValue", root.getBeanList().get(3).getStringProperty());

        OgnlUtil.setValue("stringList[1]", root, "testValue");
        OgnlUtil.setValue("stringList[5]", root, "testValue");
        OgnlUtil.setValue("stringList[9]", root, "testValue");
        OgnlUtil.setValue("stringList[7]", root, "testValue");
        OgnlUtil.setValue("stringList[3]", root, "testValue");

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

    @Test
    public void testMapProperties() throws Exception {
        TestBean root = new TestBean();

        Assert.assertNull( OgnlUtil.getValue("stringMap['foo']", root) );
        Assert.assertNull( OgnlUtil.getValue("stringMap['bar']", root) );
        Assert.assertNull( OgnlUtil.getValue("stringMap['testValue']", root) );

        Assert.assertNull( OgnlUtil.getValue("beanMap['foo']", root) );
        Assert.assertNull( OgnlUtil.getValue("beanMap['bar']", root) );
        Assert.assertNull( OgnlUtil.getValue("beanMap['testValue']", root) );

        Assert.assertNull( OgnlUtil.getValue("beanMap['foo'].longProperty", root) );
        Assert.assertNull( OgnlUtil.getValue("beanMap['bar'].stringProperty", root) );
        Assert.assertNull( OgnlUtil.getValue("beanMap['testValue'].enumProperty", root) );

        OgnlUtil.setValue("stringMap['testKey']", root, "testValue");
        Assert.assertEquals("testValue", root.getStringMap().get("testKey"));

        OgnlUtil.setValue("beanMap['testKey'].enumProperty", root, TestEnum.Fifth);
        Assert.assertNotNull(root.getBeanMap());
        Assert.assertNotNull(root.getBeanMap().get("testKey"));
        Assert.assertEquals(TestEnum.Fifth, root.getBeanMap().get("testKey").getEnumProperty());
    }

}
