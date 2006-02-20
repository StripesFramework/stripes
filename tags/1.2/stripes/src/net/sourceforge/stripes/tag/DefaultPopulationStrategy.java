/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.util.OgnlUtil;
import net.sourceforge.stripes.util.HtmlUtil;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.validation.ValidationErrors;

import ognl.OgnlException;

/**
 * <p>Default implementation of the form input tag population strategy. First looks to see if there
 *  is a parameter with the same name as the tag submitted in the current request.  If there is,
 * it will be returned as a String[] in order to support multiple-value parameters.  Values that
 * are pulled from the request for re-population purposes will automatically be filtered of
 * special HTML characters, so that they re-display properly and do not allow the user to inject
 * HTML into the page.</p>
 *
 * <p>If there is no value in the request then an ActionBean bound to the current form will be
 * looked for.  If the ActionBean is found and the value is non-null it will be returned.
 * If no value can be found in either place, null will returned.
 *
 * @author Tim Fennell
 */
public class DefaultPopulationStrategy implements PopulationStrategy {
    /** Configuration object handed to the class at init time. */
    private Configuration config;

    /** Log used to log any errors that occur. */
    private static Log log = Log.getInstance(DefaultPopulationStrategy.class);

    /** Called by the Configuration to configure the component. */
    public void init(Configuration configuration) throws Exception {
        this.config = configuration;
    }

    /**
     * Implementation of the interface method that will follow the search described in the class
     * level JavaDoc and attempt to find a value for this tag.
     *
     * @param tag the form input tag whose value to populate
     * @return Object will be one of null, a single Object or an Array of Objects depending upon
     *         what was submitted in the prior request, and what is declard on the ActionBean
     */
    public Object getValue(InputTagSupport tag) throws StripesJspException {
        // Look first for something that the user submitted in the current request
        Object value = getValuesFromRequest(tag);

        // If that's not there, let's look on the ActionBean
        if (value == null) {
            value = getValueFromActionBean(tag);
        }

        // And if there's no value there, look at the tag's own value
        if (value == null) {
            value = getValueFromTag(tag);
        }

        return value;
    }

    /**
     * Helper method that will check the current request for user submitted values for the
     * tag supplied and return them as a String[] if there is one or more present. Values are
     * HTML encoded before being returned so that they are safe to be used directly in the tag or
     * JSP.
     *
     * @param tag the tag whose values to look for
     * @return a String[] if values are found, null otherwise
     */
    protected String[] getValuesFromRequest(InputTagSupport tag) throws StripesJspException {
        final String[] paramValues = tag.getPageContext().getRequest().getParameterValues(tag.getName());

        if (paramValues != null) {
            for (int i=0; i<paramValues.length; ++i) {
                paramValues[i] = HtmlUtil.encode(paramValues[i]);
            }
        }

        return paramValues;
    }

    /**
     * Helper method that will check to see if there is an ActionBean present in the request,
     * and if so, retrieve the value for this tag from the ActionBean.
     *
     * @param tag the tag whose values to look for
     * @return an Object, possibly null, representing the tag's value
     */
    protected Object getValueFromActionBean(InputTagSupport tag) throws StripesJspException {
        ActionBean actionBean = tag.getParentFormTag().getActionBean();
        Object value = null;

        if (actionBean != null) {
            try {
                value = OgnlUtil.getValue(tag.getName(), actionBean);
            }
            catch (OgnlException oe) {
                log.info("Could not locate property of name [" + tag.getName() + "] on ActionBean.", oe);
            }
        }

        return value;
    }

    /**
     * Helper method that will retreive the preferred value set on the tag in the JSP. For
     * most tags this is usually the body if it is present, or the value attribute.  In some
     * cases tags implement this differently, notably the radio and checkbox tags.
     *
     * @param tag the tag that is being repopulated
     * @return a value for the tag if one is specified on the JSP
     */
    protected Object getValueFromTag(InputTagSupport tag) {
        return tag.getValueOnPage();
    }

    /**
     * Helper method that will check to see if the form containing this tag is being renered
     * as a result of validation errors.  This is not actually used by the default strategy,
     * but is here to help subclasses provide different behaviour for when the form is rendering
     * normally vs. in error.
     *
     * @param tag the tag that is being repopulated
     * @return boolean true if the form is in error, false otherwise
     */
    protected boolean isFormInError(InputTagSupport tag) throws StripesJspException {
        boolean inError = false;

        ActionBean actionBean = tag.getParentFormTag().getActionBean();
        if (actionBean != null) {
            ValidationErrors errors = actionBean.getContext().getValidationErrors(); 
            inError = (errors != null && errors.size() > 0);
        }

        return inError;
    }
}
