package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.exception.StripesJspException;

import javax.servlet.jsp.JspException;
import java.util.Collection;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Parent class for all input tags in stripes.  Provides support methods for retrieving all the
 * attributes that are shared across form input tags.  Also provides accessors for finding the
 * specified &quot;override&quot; value and for finding the enclosing support tag.
 *
 * @author Tim Fennell
 */
public abstract class InputTagSupport extends HtmlTagSupport {
    /** PopulationStrategy used to find non-default values for input tags. */
    private static PopulationStrategy populationStrategy = new DefaultPopulationStrategy();

    public void setDisabled(String disabled) { set("disabled", disabled); }
    public String getDisabled() { return get("disabled"); }

    public void setName(String name) { set("name", name); }
    public String getName() { return get("name"); }

    public void setSize(String size) { set("size", size); }
    public String getSize() { return get("size"); }

    /**
     * Gets the value for this tag based on the current population strategy.  The value returned
     * could be a scalar value, or it could be an array or collection depending on what the
     * population strategy finds.  For example, if the user submitted multiple values for a
     * checkbox, the default population strategy would return a String[] containg all submitted
     * values.
     *
     * @return Object either a value/values for this tag or null
     * @throws StripesJspException if the enclosing form tag (which is required at all times, and
     *         necessary to perform repopulation) cannot be located
     */
    protected Object getOverrideValueOrValues() throws StripesJspException {
        return populationStrategy.getValue(this);
    }

    /**
     * Returns a single value for the the value of this field.  This can be used to ensure that
     * only a single value is returned by the population strategy, which is useful in the case
     * of text inputs etc. which can have only a single value.
     *
     * @return Object either a single value or null
     * @throws StripesJspException if the enclosing form tag (which is required at all times, and
     *         necessary to perform repopulation) cannot be located
     */
    protected Object getSingleOverrideValue() throws StripesJspException {
        Object unknown = getOverrideValueOrValues();
        Object returnValue = null;

        if (unknown != null && unknown instanceof Object[]) {
            Object[] array = (Object[]) unknown;
            if (array.length > 0) {
                returnValue = array[0];
            }
        }
        else if (unknown != null && unknown instanceof Collection) {
            Collection collection = (Collection) unknown;
            if (collection.size() > 0) {
                returnValue = collection.iterator().next();
            }
        }
        else {
            returnValue = unknown;
        }

        return returnValue;
    }

    /**
     * <p>Locates the enclosing stripes form tag. If no form tag can be found, because the tag
     * was not enclosed in one on the JSP, an exception is thrown.</p>
     *
     * @return FormTag the enclosing form tag on the JSP
     * @throws StripesJspException if an enclosing form tag cannot be found
     */
    protected FormTag getParentFormTag() throws StripesJspException {
        FormTag parent = getParentTag(FormTag.class);

        if (parent == null) {
            throw new StripesJspException
                ("InputTag of type [" + getClass().getName() + "] must be enclosed inside a " +
                    "stripes form tag.");
        }

        return parent;
    }

    /**
     * Utility method for determining if a String value is contained within an Object, where the
     * object may be either a String, String[], Object, Object[] or Collection.  Used primarily
     * by the InputCheckBoxTag and InputSelectTag to determine if specific check boxes or
     * options should be selected based on the values contained in the JSP, HttpServletRequest and
     * the ActionBean.
     *
     * @param value the value that we are searching for
     * @param selected a String, String[], Object, Object[] or Collection (of scalars) denoting the
     *        selected items
     * @return boolean true if the String can be found, false otherwise
     */
    protected boolean isItemSelected(Object value, Object selected) {
        // Since this is a checkbox, there could be more than one checked value, which means
        // this could be a single value type, array or collection
        if (selected != null) {
            String stringValue = (value == null) ? "" : value.toString();

            if (selected instanceof Object[]) {
                Object[] selectedIf = (Object[]) selected;
                for (Object item : selectedIf) {
                    if ( (item.toString().equals(stringValue)) ) {
                        return true;
                    }
                }
            }
            else if (selected instanceof Collection) {
                Collection selectedIf = (Collection) selected;
                for (Object item : selectedIf) {
                    if ( (item.toString().equals(stringValue)) ) {
                        return true;
                    }
                }
            }
            else {
                if( selected.toString().equals(stringValue) ) {
                    return true;
                }
            }
        }

        // If we got this far without returning, then this is not a selected item
        return false;
    }

    /**
     * Fetches the localized name for this field if one exists in the resource bundle. Relies on
     * there being a "name" attribute on the tag, and the pageContext being set on the tag. First
     * checks for a value of {formName}.{fieldName} in the specified bundle, and if that exists,
     * returns it.  If that does not exist, will look for just "fieldName".
     *
     * @return a localized field name if one can be found, or null if one cannot be found.
     */
    protected String getLocalizedFieldName() throws StripesJspException {
        String name = getAttributes().get("name").toString();
        Locale locale = getPageContext().getRequest().getLocale();
        String localizedValue = null;
        String formName = getParentFormTag().getAttributes().get("name").toString();
        ResourceBundle bundle = null;
        try {
            bundle = StripesFilter.getConfiguration().getLocalizationBundleFactory()
                    .getFormFieldBundle(getPageContext().getRequest().getLocale());
        }
        catch (MissingResourceException mre) {
            return null; // not much we can do without a bundle
        }

        try {
            localizedValue = bundle.getString(formName + "." + name);
        }
        catch (MissingResourceException mre) {
            try {
                localizedValue = bundle.getString(name);
            }
            catch (MissingResourceException mre2) {
                // do nothing
            }
        }

        return localizedValue;
    }

    /**
     * Final implementation of the doStartTag() method that allows the base InputTagSupport class
     * to insert functionality before and after the tag performs it's doStartTag equivelant
     * method.
     *
     * @return int the value returned by the child class from doStartInputTag()
     */
    public final int doStartTag() throws JspException {
        return doStartInputTag();
    }

    /** Abstract method implemented in child classes instead of doStartTag(). */
    public abstract int doStartInputTag() throws JspException;

    /**
     * Final implementation of the doEndTag() method that allows the base InputTagSupport class
     * to insert functionality before and after the tag performs it's doEndTag equivelant
     * method.
     *
     * @return int the value returned by the child class from doStartInputTag()
     */
    public final int doEndTag() throws JspException {
        return doEndInputTag();
    }

    /** Abstract method implemented in child classes instead of doEndTag(). */
    public abstract int doEndInputTag() throws JspException;
}
