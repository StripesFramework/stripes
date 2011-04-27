/* Copyright 2007 Ben Gunter
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
package net.sourceforge.stripes.validation;

import java.util.Map;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.config.ConfigurableComponent;
import net.sourceforge.stripes.controller.ParameterName;

/**
 * Provides a globally accessible source of validation metadata for properties and nested properties
 * of {@link ActionBean} classes.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5
 */
public interface ValidationMetadataProvider extends ConfigurableComponent {
    /**
     * Get a map of property names to {@link ValidationMetadata} for the given {@link ActionBean}
     * class.
     * 
     * @param beanType any class
     * @return A map of property names to {@link ValidationMetadata}. If no validation information
     *         is present for the given class, then an empty map will be returned.
     */
    Map<String, ValidationMetadata> getValidationMetadata(Class<?> beanType);

    /**
     * Get the validation metadata associated with the named {@code property} of the given
     * {@link ActionBean} class.
     * 
     * @param beanType any class
     * @param property a (possibly nested) property of {@code beanType}
     * @return A {@link ValidationMetadata} object, if there is one associated with the property. If
     *         the property is not to be validated, then null.
     */
    ValidationMetadata getValidationMetadata(Class<?> beanType, ParameterName property);
}
