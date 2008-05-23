package net.sourceforge.stripes.test;

import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.LongTypeConverter;

import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * An ActionBean that has a fairly complete/complex set of properties in order to
 * help out in binding tests.
 *
 * @author Tim Fennell
 */
@UrlBinding("/test/Test.action")
public class TestActionBean implements ActionBean {
    public enum Color {Red, Green, Blue, Yellow, Orange, Black, White }

    /** Pair of static classes used to check type finding via instances intead of type inference. */
    public static class PropertyLess { }
    public static class Item extends PropertyLess {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    private ActionBeanContext context;
    private List<Long> listOfLongs;
    private Set<String> setOfStrings;
    private List<TestBean> listOfBeans;
    private Map<String,Long> mapOfLongs;
    private Map<String,Object> mapOfObjects;
    private TestBean testBean;
    private String singleString;
    private Long singleLong;
    @SuppressWarnings("unchecked")
	private List nakedListOfLongs;
    private int[] intArray;
    private String setOnlyString;
    public Long publicLong;
    public Color[] colors;
    private PropertyLess item = new Item();

    /** A pretty ordinary list of longs, to test lists of primitive/simply objects. */
    public List<Long> getListOfLongs() { return listOfLongs; }
    public void setListOfLongs(List<Long> listOfLongs) { this.listOfLongs = listOfLongs; }

    /** A list of TestBean objects to test indexed and nested properties. */
    public List<TestBean> getListOfBeans() { return listOfBeans; }
    public void setListOfBeans(List<TestBean> listOfBeans) { this.listOfBeans = listOfBeans; }

    /** A map of longs to test Maps of type converted properties. */
    public Map<String, Long> getMapOfLongs() { return mapOfLongs; }
    public void setMapOfLongs(Map<String, Long> mapOfLongs) { this.mapOfLongs = mapOfLongs; }

    /** A map of Objects which should get populated as Strings because String extends Object. */
    public Map<String, Object> getMapOfObjects() { return mapOfObjects; }
    public void setMapOfObjects(Map<String, Object> mapOfObjects) { this.mapOfObjects = mapOfObjects; }

    /** A single test bean to test out basic nested properties. */
    public TestBean getTestBean() { return testBean; }
    public void setTestBean(TestBean testBean) { this.testBean = testBean; }

    /** Just a basic string property. */
    public String getSingleString() { return singleString; }
    public void setSingleString(String singleString) { this.singleString = singleString; }

    /** A Set of Strings, to test non List based collections. */
    public Set<String> getSetOfStrings() { return setOfStrings; }
    public void setSetOfStrings(Set<String> setOfStrings) { this.setOfStrings = setOfStrings; }

    /** Just a basic Long property. */
    public Long getSingleLong() { return singleLong; }
    public void setSingleLong(Long singleLong) { this.singleLong = singleLong; }

    /** A non-generic list to make sure specifying the converter is used properly. */
    @SuppressWarnings("unchecked")
	@Validate(converter=LongTypeConverter.class)
    public List getNakedListOfLongs() { return nakedListOfLongs; }
    @SuppressWarnings("unchecked")
	public void setNakedListOfLongs(List nakedListOfLongs) { this.nakedListOfLongs = nakedListOfLongs; }

    /** An array of primitive ints to test out Array binding. */
    public int[] getIntArray() { return intArray; }
    public void setIntArray(int[] intArray) { this.intArray = intArray; }

    /** A property with only a setter to test out setting when there's no getter. */
    public void setSetOnlyString(String setOnlyString) { this.setOnlyString = setOnlyString; }
    public boolean setOnlyStringIsNotNull() { return this.setOnlyString != null; }

    /** An array of enums to test out enums, and arrays of non-instantiable things. */
    public Color[] getColors() { return colors; }
    public void setColors(Color[] colors) { this.colors = colors; }

    /** Return type is a property-less class, but returns an instance of a subclass with an 'id' property. */
    public PropertyLess getItem() { return item; }
    public void setItem(PropertyLess item) { this.item = item; }

    ///////////////////////////////////////////////////////////////////////////
    // Dummied up ActionBean methods that aren't really used for much.
    ///////////////////////////////////////////////////////////////////////////
    public void setContext(ActionBeanContext context) { this.context = context; }
    public ActionBeanContext getContext() { return this.context; }

    @DefaultHandler
    public void doNothing() { /* Do Nothing. */ }
}
