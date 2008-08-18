package net.sourceforge.stripes.util.bean;

import org.testng.annotations.Test;
import org.testng.Assert;
import net.sourceforge.stripes.test.TestBean;
import net.sourceforge.stripes.test.TestEnum;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Test cases for the BeanComparator class that sorts lists of JavaBeans based on
 * their properties.
 *
 * @author Tim Fennell
 */
public class BeanComparatorTest {
    @Test(groups="fast")
    public void testSimplePropertySort() throws Exception {
        List<TestBean> beans = new ArrayList<TestBean>();
        beans.add(new TestBean());
        beans.get(beans.size()-1).setStringProperty("hello");
        beans.add(new TestBean());
        beans.get(beans.size()-1).setStringProperty("goodbye");
        beans.add(new TestBean());
        beans.get(beans.size()-1).setStringProperty("whatever");
        beans.add(new TestBean());
        beans.get(beans.size()-1).setStringProperty("huh?");
        beans.add(new TestBean());
        beans.get(beans.size()-1).setStringProperty("no way!");

        Collections.sort(beans, new BeanComparator("stringProperty"));
        Assert.assertEquals(beans.get(0).getStringProperty(), "goodbye");
        Assert.assertEquals(beans.get(1).getStringProperty(), "hello");
        Assert.assertEquals(beans.get(2).getStringProperty(), "huh?");
        Assert.assertEquals(beans.get(3).getStringProperty(), "no way!");
        Assert.assertEquals(beans.get(4).getStringProperty(), "whatever");
    }

    @Test(groups="fast")
    public void testSimpleMultiPropertySort() throws Exception {
        List<TestBean> beans = new ArrayList<TestBean>();
        beans.add(new TestBean());
        beans.get(beans.size()-1).setLongProperty(2l);
        beans.get(beans.size()-1).setStringProperty("hello");
        beans.add(new TestBean());
        beans.get(beans.size()-1).setLongProperty(2l);
        beans.get(beans.size()-1).setStringProperty("goodbye");
        beans.add(new TestBean());
        beans.get(beans.size()-1).setLongProperty(1l);
        beans.get(beans.size()-1).setStringProperty("whatever");
        beans.add(new TestBean());
        beans.get(beans.size()-1).setLongProperty(1l);
        beans.get(beans.size()-1).setStringProperty("huh?");
        beans.add(new TestBean());
        beans.get(beans.size()-1).setLongProperty(3l);
        beans.get(beans.size()-1).setStringProperty("no way!");

        Collections.sort(beans, new BeanComparator("longProperty", "stringProperty"));
        Assert.assertEquals(beans.get(0).getStringProperty(), "huh?");
        Assert.assertEquals(beans.get(1).getStringProperty(), "whatever");
        Assert.assertEquals(beans.get(2).getStringProperty(), "goodbye");
        Assert.assertEquals(beans.get(3).getStringProperty(), "hello");
        Assert.assertEquals(beans.get(4).getStringProperty(), "no way!");
    }

    @Test(groups="fast")
    public void testNullPropertySort() throws Exception {
        List<TestBean> beans = new ArrayList<TestBean>();
        beans.add(new TestBean());
        beans.get(beans.size()-1).setStringProperty("hello");
        beans.add(new TestBean());
        beans.get(beans.size()-1).setStringProperty(null);
        beans.add(new TestBean());
        beans.get(beans.size()-1).setStringProperty("whatever");

        Collections.sort(beans, new BeanComparator("stringProperty"));
        Assert.assertEquals(beans.get(0).getStringProperty(), "hello");
        Assert.assertEquals(beans.get(1).getStringProperty(), "whatever");
        Assert.assertEquals(beans.get(2).getStringProperty(), null);
    }

    @Test(groups="fast")
    public void testNullPropertySort2() throws Exception {
        List<TestBean> beans = new ArrayList<TestBean>();
        beans.add(new TestBean());
        beans.get(beans.size()-1).setStringProperty(null);
        beans.add(new TestBean());
        beans.get(beans.size()-1).setStringProperty(null);
        beans.add(new TestBean());
        beans.get(beans.size()-1).setStringProperty("whatever");

        Collections.sort(beans, new BeanComparator("stringProperty"));
        Assert.assertEquals(beans.get(0).getStringProperty(), "whatever");
        Assert.assertEquals(beans.get(1).getStringProperty(), null);
        Assert.assertEquals(beans.get(2).getStringProperty(), null);
    }

    @Test(groups="fast")
    public void testNestedPropertySort() throws Exception {
        List<TestBean> beans = new ArrayList<TestBean>();
        beans.add(new TestBean());
        beans.get(beans.size()-1).setNestedBean(new TestBean());
        beans.get(beans.size()-1).getNestedBean().setEnumProperty(TestEnum.Fourth);
        beans.add(new TestBean());
        beans.get(beans.size()-1).setNestedBean(new TestBean());
        beans.get(beans.size()-1).getNestedBean().setEnumProperty(TestEnum.Second);
        beans.add(new TestBean());
        beans.get(beans.size()-1).setNestedBean(new TestBean());
        beans.get(beans.size()-1).getNestedBean().setEnumProperty(TestEnum.Ninth);
        beans.add(new TestBean());
        beans.get(beans.size()-1).setNestedBean(new TestBean());
        beans.get(beans.size()-1).getNestedBean().setEnumProperty(TestEnum.Eight);
        beans.add(new TestBean());
        beans.get(beans.size()-1).setNestedBean(new TestBean());
        beans.get(beans.size()-1).getNestedBean().setEnumProperty(TestEnum.First);

        Collections.sort(beans, new BeanComparator("nestedBean.enumProperty"));
        Assert.assertEquals(beans.get(0).getNestedBean().getEnumProperty(), TestEnum.First);
        Assert.assertEquals(beans.get(1).getNestedBean().getEnumProperty(), TestEnum.Second);
        Assert.assertEquals(beans.get(2).getNestedBean().getEnumProperty(), TestEnum.Fourth);
        Assert.assertEquals(beans.get(3).getNestedBean().getEnumProperty(), TestEnum.Eight);
        Assert.assertEquals(beans.get(4).getNestedBean().getEnumProperty(), TestEnum.Ninth);
    }
}
