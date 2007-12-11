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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;

/**
 * An implementation of {@link ValidationMetadataProvider} that scans classes and their superclasses
 * for properties annotated with {@link Validate} and/or {@link ValidateNestedProperties} and
 * exposes the validation metadata specified by those annotations. When searching for annotations,
 * this implementation looks first at the property's read method (getter), then its write method
 * (setter), and finally at the field itself.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5
 */
public class DefaultValidationMetadataProvider implements ValidationMetadataProvider {
    private static final Log log = Log.getInstance(DefaultValidationMetadataProvider.class);
    private Configuration configuration;

    /** Map class -> field -> validation meta data */
    private final Map<Class<?>, Map<String, ValidationMetadata>> cache = new HashMap<Class<?>, Map<String, ValidationMetadata>>();

    /** Currently does nothing except store a reference to {@code configuration}. */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
    }

    /** Get the {@link Configuration} object that was passed into {@link #init(Configuration)}. */
    public Configuration getConfiguration() {
        return configuration;
    }

    public Map<String, ValidationMetadata> getValidationMetadata(Class<?> beanType) {
        Map<String, ValidationMetadata> meta = cache.get(beanType);
        if (meta == null) {
            meta = loadForClass(beanType);
            cache.put(beanType, meta);
        }

        return meta;
    }

    public ValidationMetadata getValidationMetadata(Class<?> beanType, String field) {
        return getValidationMetadata(beanType).get(field);
    }

    /**
     * Get validation information for all the properties and nested properties of the given class.
     * The {@link Validate} and/or {@link ValidateNestedProperties} annotations may be applied to
     * the property's read method, write method, or field declaration. If a property has a
     * {@link ValidateNestedProperties} annotation, then the nested properties named in its
     * {@link Validate} annotations will be included as well.
     * 
     * @param clazz a class
     * @return A map of (possibly nested) property names to {@link ValidationMetadata} for the
     *         property.
     */
    protected Map<String, ValidationMetadata> loadForClass(Class<?> clazz) {
        Map<String, ValidationMetadata> meta = new HashMap<String, ValidationMetadata>();
        try {
            PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                String propertyName = pd.getName();
                Validate simple = null;
                ValidateNestedProperties nested = null;

                // check getter method
                Method method = pd.getReadMethod();
                if (method != null && Modifier.isPublic(method.getModifiers())) {
                    simple = method.getAnnotation(Validate.class);
                    nested = method.getAnnotation(ValidateNestedProperties.class);
                }

                // check setter method
                method = pd.getWriteMethod();
                if (method != null && Modifier.isPublic(method.getModifiers())) {
                    if (simple == null)
                        simple = method.getAnnotation(Validate.class);
                    if (nested == null)
                        nested = method.getAnnotation(ValidateNestedProperties.class);
                }

                // check the field (possibly declared in a superclass) with the same
                // name as the property for annotations
                Field field = null;
                for (Class<?> c = clazz; c != null && field == null; c = c.getSuperclass()) {
                    try {
                        field = c.getDeclaredField(propertyName);
                    }
                    catch (NoSuchFieldException e) {
                    }
                }
                if (field != null) {
                    if (simple == null)
                        simple = field.getAnnotation(Validate.class);
                    if (nested == null)
                        nested = field.getAnnotation(ValidateNestedProperties.class);
                }

                // add to allow list if @Validate present
                if (simple != null) {
                    if (simple.field() != null) {
                        meta.put(propertyName, new ValidationMetadata(propertyName, simple));
                    }
                    else {
                        log.warn("Field name present in @Validate but should be omitted: ", clazz,
                                ", property ", propertyName, ", given field name ", simple.field());
                    }
                }

                // add all sub-properties referenced in @ValidateNestedProperties
                if (nested != null) {
                    Validate[] validates = nested.value();
                    if (validates != null) {
                        for (Validate validate : validates) {
                            if (validate.field() != null) {
                                String fullName = propertyName + '.' + validate.field();
                                meta.put(fullName, new ValidationMetadata(fullName, validate));
                            }
                            else {
                                log.warn("Field name missing from nested @Validate: ", clazz,
                                        ", property ", propertyName);
                            }
                        }
                    }
                }
            }
        }
        catch (RuntimeException e) {
            log.error(e, "Failure checking @Validate annotations ", getClass().getName());
            throw e;
        }
        catch (Exception e) {
            log.error(e, "Failure checking @Validate annotations ", getClass().getName());
            StripesRuntimeException sre = new StripesRuntimeException(e.getMessage(), e);
            sre.setStackTrace(e.getStackTrace());
            throw sre;
        }

        // Print out a pretty debug message showing what validations got configured
        StringBuilder builder = new StringBuilder(128);
        for (Map.Entry<String, ValidationMetadata> entry : meta.entrySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(entry.getKey());
            builder.append("->");
            builder.append(entry.getValue());
        }
        log.debug("Loaded validations for ActionBean ", clazz.getSimpleName(), ": ", builder
                .length() > 0 ? builder : "<none>");

        return Collections.unmodifiableMap(meta);
    }
}
