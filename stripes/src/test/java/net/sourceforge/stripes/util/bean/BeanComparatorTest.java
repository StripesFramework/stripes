package net.sourceforge.stripes.util.bean;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.sourceforge.stripes.testbeans.TestBean;
import net.sourceforge.stripes.testbeans.TestEnum;


/**
 * Test cases for the BeanComparator class that sorts lists of JavaBeans based on
 * their properties.
 *
 * @author Tim Fennell
 */
public class BeanComparatorTest {

   @Test
   public void testNestedPropertySort() {
      List<TestBean> beans = new ArrayList<>();
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setNestedBean(new TestBean());
      beans.get(beans.size() - 1).getNestedBean().setEnumProperty(TestEnum.Fourth);
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setNestedBean(new TestBean());
      beans.get(beans.size() - 1).getNestedBean().setEnumProperty(TestEnum.Second);
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setNestedBean(new TestBean());
      beans.get(beans.size() - 1).getNestedBean().setEnumProperty(TestEnum.Ninth);
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setNestedBean(new TestBean());
      beans.get(beans.size() - 1).getNestedBean().setEnumProperty(TestEnum.Eight);
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setNestedBean(new TestBean());
      beans.get(beans.size() - 1).getNestedBean().setEnumProperty(TestEnum.First);

      beans.sort(new BeanComparator("nestedBean.enumProperty"));

      assertThat(beans.get(0).getNestedBean().getEnumProperty()).isEqualTo(TestEnum.First);
      assertThat(beans.get(1).getNestedBean().getEnumProperty()).isEqualTo(TestEnum.Second);
      assertThat(beans.get(2).getNestedBean().getEnumProperty()).isEqualTo(TestEnum.Fourth);
      assertThat(beans.get(3).getNestedBean().getEnumProperty()).isEqualTo(TestEnum.Eight);
      assertThat(beans.get(4).getNestedBean().getEnumProperty()).isEqualTo(TestEnum.Ninth);
   }

   @Test
   public void testNullPropertySort() {
      List<TestBean> beans = new ArrayList<>();
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setStringProperty("hello");
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setStringProperty(null);
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setStringProperty("whatever");

      beans.sort(new BeanComparator("stringProperty"));

      assertThat(beans.get(0).getStringProperty()).isEqualTo("hello");
      assertThat(beans.get(1).getStringProperty()).isEqualTo("whatever");
      assertThat(beans.get(2).getStringProperty()).isEqualTo(null);
   }

   @Test
   public void testNullPropertySort2() {
      List<TestBean> beans = new ArrayList<>();
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setStringProperty(null);
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setStringProperty(null);
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setStringProperty("whatever");

      beans.sort(new BeanComparator("stringProperty"));

      assertThat(beans.get(0).getStringProperty()).isEqualTo("whatever");
      assertThat(beans.get(1).getStringProperty()).isNull();
      assertThat(beans.get(2).getStringProperty()).isNull();
   }

   @Test
   public void testSimpleMultiPropertySort() {
      List<TestBean> beans = new ArrayList<>();
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setLongProperty(2L);
      beans.get(beans.size() - 1).setStringProperty("hello");
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setLongProperty(2L);
      beans.get(beans.size() - 1).setStringProperty("goodbye");
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setLongProperty(1L);
      beans.get(beans.size() - 1).setStringProperty("whatever");
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setLongProperty(1L);
      beans.get(beans.size() - 1).setStringProperty("huh?");
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setLongProperty(3L);
      beans.get(beans.size() - 1).setStringProperty("no way!");

      beans.sort(new BeanComparator("longProperty", "stringProperty"));

      assertThat(beans.get(0).getStringProperty()).isEqualTo("huh?");
      assertThat(beans.get(1).getStringProperty()).isEqualTo("whatever");
      assertThat(beans.get(2).getStringProperty()).isEqualTo("goodbye");
      assertThat(beans.get(3).getStringProperty()).isEqualTo("hello");
      assertThat(beans.get(4).getStringProperty()).isEqualTo("no way!");
   }

   @Test
   public void testSimplePropertySort() {
      List<TestBean> beans = new ArrayList<>();
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setStringProperty("hello");
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setStringProperty("goodbye");
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setStringProperty("whatever");
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setStringProperty("huh?");
      beans.add(new TestBean());
      beans.get(beans.size() - 1).setStringProperty("no way!");

      beans.sort(new BeanComparator("stringProperty"));

      assertThat(beans.get(0).getStringProperty()).isEqualTo("goodbye");
      assertThat(beans.get(1).getStringProperty()).isEqualTo("hello");
      assertThat(beans.get(2).getStringProperty()).isEqualTo("huh?");
      assertThat(beans.get(3).getStringProperty()).isEqualTo("no way!");
      assertThat(beans.get(4).getStringProperty()).isEqualTo("whatever");
   }
}
