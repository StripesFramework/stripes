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

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.util.bean.BeanUtil;
import net.sourceforge.stripes.util.bean.ExpressionException;
import net.sourceforge.stripes.util.Log;

/**
 * <p>An alternative tag population strategy that will normally prefer the value from the ActionBean
 * over values from the request - even when the ActionBean returns null!  Only if the ActionBean
 * is not present, or does not define an attribute with the name supplied to the tag will other
 * population sources be examined.  When that happens, the strategy will check the value
 * specified on the page next, and finally the value(s) in the request.</p>
 *
 * <p>If the field represented by the tag is determined to be in error (i.e. the ActionBean is
 * present and has validation errors for the matching field) then the repopulation behaviour
 * will revert to the default behaviour of preferring the request parameters.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class BeanFirstPopulationStrategy extends DefaultPopulationStrategy {
    private static final Log log = Log.getInstance(BeanFirstPopulationStrategy.class);

    /**
     * Implementation of the interface method that will follow the search described in the class
     * level JavaDoc and attempt to find a value for this tag.
     *
     * @param tag the form input tag whose value to populate
     * @return Object will be one of null, a single Object or an Array of Objects depending upon
     *         what was submitted in the prior request, and what is declared on the ActionBean
     */
    public Object getValue(InputTagSupport tag) throws StripesJspException {
        // If the specific tag is in error, grab the values from the request
        if (tag.hasErrors()) {
            return super.getValue(tag);
        }
        else {
            // Try getting from the ActionBean.  If the bean is present and the property
            // is defined, then the value from the bean takes precedence even if it's null
            ActionBean bean = tag.getActionBean();
            Object value = null;
            boolean kaboom = false;
            if (bean != null) {
                try {
                    value = BeanUtil.getPropertyValue(tag.getName(), bean);
                }
                catch (ExpressionException ee) {
                    log.info("Could not locate property of name [" + tag.getName() + "] on ActionBean.", ee);
                    kaboom = true;
                }
            }

            // If there's no matching bean property, then look elsewhere
            if (bean == null || kaboom) {
                value = getValueFromTag(tag);

                if (value == null) {
                    value = getValuesFromRequest(tag);
                }
            }

            return value;
        }
    }

}
