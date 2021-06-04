package org.stripesframework.spring.testbeans;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A simple JavaBean that has all sorts of properties, making it good
 * for testing out pieces of Stripes.
 *
 * @author Tim Fennell
 */
public class TestBean {

   private String   stringProperty;
   private int      intProperty;
   private Long     longProperty;
   private TestEnum enumProperty;
   private boolean  booleanProperty;

   public  List<Float>[]                     genericArray;
   private String[]                          stringArray;
   private List<String>                      stringList;
   private Set<String>                       stringSet;
   private Map<String, String>               stringMap;
   private Map<String, Map<String, Boolean>> nestedMap;
   private Map<Long, Long>                   longMap;

   private List<TestBean>        beanList;
   private Map<String, TestBean> beanMap;

   private TestBean nestedBean;

   public List<TestBean> getBeanList() {
      return beanList;
   }

   public Map<String, TestBean> getBeanMap() {
      return beanMap;
   }

   public TestEnum getEnumProperty() {
      return enumProperty;
   }

   public int getIntProperty() {
      return intProperty;
   }

   public Map<Long, Long> getLongMap() { return longMap; }

   public Long getLongProperty() {
      return longProperty;
   }

   public TestBean getNestedBean() {
      return nestedBean;
   }

   public Map<String, Map<String, Boolean>> getNestedMap() { return nestedMap; }

   public String[] getStringArray() {
      return stringArray;
   }

   public List<String> getStringList() {
      return stringList;
   }

   public Map<String, String> getStringMap() {
      return stringMap;
   }

   public String getStringProperty() {
      return stringProperty;
   }

   public Set<String> getStringSet() { return stringSet; }

   public boolean isBooleanProperty() {
      return booleanProperty;
   }

   public void setBeanList( List<TestBean> beanList ) {
      this.beanList = beanList;
   }

   public void setBeanMap( Map<String, TestBean> beanMap ) {
      this.beanMap = beanMap;
   }

   public void setBooleanProperty( boolean booleanProperty ) {
      this.booleanProperty = booleanProperty;
   }

   public void setEnumProperty( TestEnum enumProperty ) {
      this.enumProperty = enumProperty;
   }

   public void setIntProperty( int intProperty ) {
      this.intProperty = intProperty;
   }

   public void setLongMap( Map<Long, Long> longMap ) { this.longMap = longMap; }

   public void setLongProperty( Long longProperty ) {
      this.longProperty = longProperty;
   }

   public void setNestedBean( TestBean nestedBean ) {
      this.nestedBean = nestedBean;
   }

   public void setNestedMap( Map<String, Map<String, Boolean>> nestedMap ) { this.nestedMap = nestedMap; }

   public void setStringArray( String[] stringArray ) {
      this.stringArray = stringArray;
   }

   public void setStringList( List<String> stringList ) {
      this.stringList = stringList;
   }

   public void setStringMap( Map<String, String> stringMap ) {
      this.stringMap = stringMap;
   }

   public void setStringProperty( String stringProperty ) {
      this.stringProperty = stringProperty;
   }

   public void setStringSet( Set<String> stringSet ) { this.stringSet = stringSet; }
}
