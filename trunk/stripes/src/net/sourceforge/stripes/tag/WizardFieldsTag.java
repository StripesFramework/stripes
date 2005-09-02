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

import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.action.ActionBean;

import javax.servlet.jsp.JspException;
import java.util.Set;
import java.util.HashSet;
import java.util.Enumeration;

/**
 * <p>Examines the request and include hidden fields for all parameters that have do
 * not have form fields in the current form. Will include multiple values for
 * parameters that have them.  Excludes 'special' parameters like the source
 * page parameter, and the paramter that conveyed the event name.</p>
 *
 * <p>Very useful for implementing basic wizard flow without relying on session
 * scoping of ActionBeans, and without having to name all the parameters that
 * should be carried forward in the form.</p>
 *
 * @author Tim Fennell
 */
public class WizardFieldsTag extends StripesTagSupport {
    private boolean currentFormOnly = false;

    /**
     * Sets whether or not the parameters should be output only if the form matches the current
     * request.  Defaults to false.
     */
    public void setCurrentFormOnly(boolean currentFormOnly) { this.currentFormOnly = currentFormOnly; }

    /** Gets whether the tag will output fields for the current form only, or in all cases. */
    public boolean isCurrentFormOnly() { return currentFormOnly; }

    /** Skips over the body because there shouldn't be one. */
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Performs the main work of the tag, as described in the class level javadoc.
     * @return EVAL_PAGE in all cases.
     */
    public int doEndTag() throws JspException {
        // Figure out the list of parameters we should not include
        FormTag form = getParentTag(FormTag.class);
        Set<String> excludes = new HashSet<String>();
        excludes.addAll( form.getRegisteredFields() );
        excludes.add( StripesConstants.URL_KEY_SOURCE_PAGE );

        ActionBean actionBean = form.getActionBean();
        if (actionBean != null) {
            String eventName = form.getActionBean().getContext().getEventName();
            if (eventName != null) {
                excludes.add(eventName);
                excludes.add(eventName + ".x");
                excludes.add(eventName + ".y");
            }
        }

        // If current form only is not specified, go ahead, otherwise check that
        // the current form had an ActionBean attached - which indicates that the
        // last submit was to the same form/action as this form
        if (!isCurrentFormOnly() || actionBean != null) {
            // Set up a hidde tag to do the writing for us
            InputHiddenTag hidden = new InputHiddenTag();
            hidden.setPageContext( getPageContext() );
            hidden.setParent( getParent() );

            // Loop through the request parameters and output the values
            Enumeration<String> parameterNames = getPageContext().getRequest().getParameterNames();
            while ( parameterNames.hasMoreElements() ) {
                String name = parameterNames.nextElement();

                if ( !excludes.contains(name) ) {
                    hidden.setName(name);
                    hidden.doStartTag();
                    hidden.doAfterBody();
                    hidden.doEndTag();
                }
            }
        }

        return EVAL_PAGE;
    }
}
