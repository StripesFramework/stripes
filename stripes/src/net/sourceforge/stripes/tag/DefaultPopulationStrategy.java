package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.util.OgnlUtil;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.exception.StripesJspException;

import ognl.OgnlException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation of the form input tag population strategy. First looks to see if there is
 * a parameter with the same name as the tag submitted in the current request.  If there is, it will
 * be returned as a String[] in order to support multiple-value parameters.  If there is no value in
 * the request then it will look for an ActionBean bound to the current form, and look for an
 * attribute of the form by name.  If found it will return the ActionBean attribute as whatever type
 * it is declared as.  If no value can be found in either place, it will return null.
 *
 * @author Tim Fennell
 */
public class DefaultPopulationStrategy implements PopulationStrategy {
    /** Log used to log any errors that occur. */
    private static Log log = LogFactory.getLog(DefaultPopulationStrategy.class);

    /**
     * Implementation of the interface method that will follow the search described in the class
     * level JavaDoc and attempt to find a value for this tag.
     *
     * @param tag the form input tag whose value to populate
     * @return Object will be one of null, a single Object or an Array of Objects depending upon
     *         what was submitted in the prior request, and what is declard on the ActionBean
     */
    public Object getValue(InputTagSupport tag) throws StripesJspException {
        Object returnValue;

        // Look first for something that the user submitted in the current request
        returnValue = tag.getPageContext().getRequest().getParameterValues(tag.getName());

        // If that's not there, let's look on the ActionBean
        ActionBean action = tag.getParentFormTag().getActionBean();

        if (returnValue == null && action != null) {
            try {
                returnValue = OgnlUtil.getValue(tag.getName(), action);
            }
            catch (OgnlException oe) {
                log.debug("Could not locate property of name [" + tag.getName() + "] on ActionBean.", oe);
            }
        }

        return returnValue;
    }
}
