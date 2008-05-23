/* Copyright 2008 Tim Fennell
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

import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.ValidationMessage;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Validates that the mutually exclusive attribute pairs of the tag are provided correctly
 * and attempts to provide type information to the container for the bean assigned
 * to the variable named by the {@code var} or {@code id} attribute. The latter can only be done
 * when the {@code beanclass} attribute is used instead of the {@code binding} attribute
 * because runtime information is needed to translate {@code binding} into a class name.
 *
 * @author tfenne
 * @since Stripes 1.5
 */
public class UseActionBeanTagExtraInfo extends TagExtraInfo {
    private static final VariableInfo[] NO_INFO = new VariableInfo[0];

    /**
     * Attempts to return type information so that the container can create a
     * named variable for the action bean.
     */
    @Override public VariableInfo[] getVariableInfo(final TagData tag) {
        // We can only provide the type of 'var' if beanclass was used because
        // if binding was used we need runtime information!
        Object beanclass = tag.getAttribute("beanclass");
        if (beanclass != null) {
            String var = tag.getAttributeString("var");
            if (var == null) var = tag.getAttributeString("id");

            // Make sure we have the class name, not the class
            if (beanclass instanceof Class) beanclass = ((Class<?>) beanclass).getName();

            // Return the variable info
            return new VariableInfo[] { new VariableInfo(var, (String) beanclass, true, VariableInfo.AT_BEGIN) };
        }
        else {
            return NO_INFO;
        }
    }

    /**
     * Checks to ensure that where the tag supports providing one of two attributes
     * that one and only one is provided.
     */
    @Override public ValidationMessage[] validate(final TagData tag) {
        Collection<ValidationMessage> errors = new ArrayList<ValidationMessage>();

        Object beanclass = tag.getAttribute("beanclass");
        String binding   = tag.getAttributeString("binding");
        if (!(beanclass != null ^ binding != null)) {
            errors.add(new ValidationMessage(tag.getId(), "Exactly one of 'beanclass' or 'binding' must be supplied."));
        }

        String var = tag.getAttributeString("var");
        String id  = tag.getAttributeString("id");
        if (!(var != null ^ id != null)) {
            errors.add(new ValidationMessage(tag.getId(), "Exactly one of 'var' or 'id' must be supplied."));
        }

        return errors.toArray(new ValidationMessage[errors.size()]);
    }
}
