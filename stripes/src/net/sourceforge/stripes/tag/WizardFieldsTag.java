/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.util.CollectionUtil;

import javax.servlet.jsp.JspException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    @SuppressWarnings("unchecked")
	public int doEndTag() throws JspException {
        // Figure out the list of parameters we should not include
        FormTag form = getParentTag(FormTag.class);
        Set<String> excludes = new HashSet<String>();
        excludes.addAll( form.getRegisteredFields() );
        excludes.add( StripesConstants.URL_KEY_SOURCE_PAGE );
        excludes.add( StripesConstants.URL_KEY_FIELDS_PRESENT );
        excludes.add( StripesConstants.URL_KEY_EVENT_NAME );

        // Use the submitted action bean to eliminate any event related parameters
        ActionBean submittedActionBean = (ActionBean)
                getPageContext().getRequest().getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);

        if (submittedActionBean != null) {
            String eventName = submittedActionBean.getContext().getEventName();
            if (eventName != null) {
                excludes.add(eventName);
                excludes.add(eventName + ".x");
                excludes.add(eventName + ".y");
            }
        }

        // Now get the action bean on this form
        ActionBean actionBean = form.getActionBean();

        // If current form only is not specified, go ahead, otherwise check that
        // the current form had an ActionBean attached - which indicates that the
        // last submit was to the same form/action as this form
        if (!isCurrentFormOnly() || actionBean != null) {
            // Set up a hidde tag to do the writing for us
            InputHiddenTag hidden = new InputHiddenTag();
            hidden.setPageContext( getPageContext() );
            hidden.setParent( getParent() );

            // Loop through the request parameters and output the values
            Map<String,String[]> params = getPageContext().getRequest().getParameterMap();
            for (Map.Entry<String,String[]> entry : params.entrySet()) {
                String name = entry.getKey();
                String[] values = entry.getValue();

                if ( !excludes.contains(name) && !CollectionUtil.empty(values)  ) {
                    hidden.setName(name);
                    try {
                        hidden.doStartTag();
                        hidden.doAfterBody();
                        hidden.doEndTag();
                    }
                    catch (Throwable t) {
                        /** Catch whatever comes back out of the doCatch() method and deal with it */
                        try { hidden.doCatch(t); }
                        catch (Throwable t2) {
                            if (t2 instanceof JspException) throw (JspException) t2;
                            if (t2 instanceof RuntimeException) throw (RuntimeException) t2;
                            else throw new StripesJspException(t2);
                        }
                    }
                    finally {
                        hidden.doFinally();
                    }
                }
            }
        }

        return EVAL_PAGE;
    }
}
