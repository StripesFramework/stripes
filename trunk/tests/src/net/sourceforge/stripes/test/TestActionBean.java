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
    private ActionBeanContext context;
    private List<Long> listOfLongs;
    private Set<String> setOfStrings;
    private List<TestBean> listOfBeans;
    private Map<String,Long> mapOfLongs;
    private Map<String,Object> mapOfObjects;
    private TestBean testBean;
    private String singleString;
    private Long singleLong;
    private List nakedListOfLongs;

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
    @Validate(converter=LongTypeConverter.class)
    public List getNakedListOfLongs() { return nakedListOfLongs; }
    public void setNakedListOfLongs(List nakedListOfLongs) { this.nakedListOfLongs = nakedListOfLongs; }

    ///////////////////////////////////////////////////////////////////////////
    // Dummied up ActionBean methods that aren't really used for much.
    ///////////////////////////////////////////////////////////////////////////
    public void setContext(ActionBeanContext context) { this.context = context; }
    public ActionBeanContext getContext() { return this.context; }

    @DefaultHandler
    public void doNothing() { /* Do Nothing. */ }
}
