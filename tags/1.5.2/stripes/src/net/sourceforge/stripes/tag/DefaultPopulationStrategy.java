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
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.ParameterName;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.util.CryptoUtil;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.bean.BeanUtil;
import net.sourceforge.stripes.util.bean.ExpressionException;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMetadata;

/**
 * <p>Default implementation of the form input tag population strategy. First looks to see if there
 * is a parameter with the same name as the tag submitted in the current request.  If there is,
 * it will be returned as a String[] in order to support multiple-value parameters.</p>
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
    private static final Log log = Log.getInstance(DefaultPopulationStrategy.class);

    /** Called by the Configuration to configure the component. */
    public void init(Configuration configuration) throws Exception {
        this.config = configuration;
    }

    /** Accessor for the configuration supplied when the population strategy is initialized. */
    protected Configuration getConfiguration() {
        return this.config;
    }

    /**
     * Implementation of the interface method that will follow the search described in the class
     * level JavaDoc and attempt to find a value for this tag.
     *
     * @param tag the form input tag whose value to populate
     * @return Object will be one of null, a single Object or an Array of Objects depending upon
     *         what was submitted in the prior request, and what is declared on the ActionBean
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
     * tag supplied and return them as a String[] if there is one or more present.
     *
     * @param tag the tag whose values to look for
     * @return a String[] if values are found, null otherwise
     */
    protected String[] getValuesFromRequest(InputTagSupport tag) throws StripesJspException {
        String[] value = tag.getPageContext().getRequest().getParameterValues(tag.getName());

        /*
         * If the value was pulled from a request parameter and the ActionBean property it would
         * bind to is flagged as encrypted, then the value needs to be decrypted now.
         */
        if (value != null) {
            // find the action bean class we're dealing with
            Class<? extends ActionBean> beanClass = tag.getParentFormTag().getActionBeanClass();
            if (beanClass != null) {
                ValidationMetadata validate = config.getValidationMetadataProvider()
                        .getValidationMetadata(beanClass, new ParameterName(tag.getName()));
                if (validate != null && validate.encrypted()) {
                    String[] copy = new String[value.length];
                    for (int i = 0; i < copy.length; i++) {
                        copy[i] = CryptoUtil.decrypt(value[i]);
                    }
                    value = copy;
                }
            }
        }

        return value;
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
                value = BeanUtil.getPropertyValue(tag.getName(), actionBean);
            }
            catch (ExpressionException ee) {
                log.info("Could not locate property of name [" + tag.getName() + "] on ActionBean.", ee);
            }
        }

        return value;
    }

    /**
     * Helper method that will retrieve the preferred value set on the tag in the JSP. For
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
     * Helper method that will check to see if the form containing this tag is being rendered
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
