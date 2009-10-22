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

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.ParameterName;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ReflectUtil;

/**
 * An implementation of {@link ValidationMetadataProvider} that scans classes and their superclasses
 * for properties annotated with {@link Validate} and/or {@link ValidateNestedProperties} and
 * exposes the validation metadata specified by those annotations. When searching for annotations,
 * this implementation looks first at the property's read method (getter), then its write method
 * (setter), and finally at the field itself.
 * 
 * @author Ben Gunter, Freddy Daoud
 * @since Stripes 1.5
 */
public class DefaultValidationMetadataProvider implements ValidationMetadataProvider {
    private static final Log log = Log.getInstance(DefaultValidationMetadataProvider.class);
    private Configuration configuration;

    /** Map class -> field -> validation meta data */
    private final Map<Class<?>, Map<String, ValidationMetadata>> cache = new ConcurrentHashMap<Class<?>, Map<String, ValidationMetadata>>();

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
            logDebugMessageForConfiguredValidations(beanType, meta);
            cache.put(beanType, meta);
        }

        return meta;
    }

    public ValidationMetadata getValidationMetadata(Class<?> beanType, ParameterName field) {
        return getValidationMetadata(beanType).get(field.getStrippedName());
    }

    /**
     * Get validation information for all the properties and nested properties of the given class.
     * The {@link Validate} and/or {@link ValidateNestedProperties} annotations may be applied to
     * the property's read method, write method, or field declaration. If a property has a
     * {@link ValidateNestedProperties} annotation, then the nested properties named in its
     * {@link Validate} annotations will be included as well.
     * 
     * @param beanType a class
     * @return A map of (possibly nested) property names to {@link ValidationMetadata} for the
     *         property.
     * @throws StripesRuntimeException if conflicts are found in the validation annotations
     */
    protected Map<String, ValidationMetadata> loadForClass(Class<?> beanType) {
        Map<String, ValidationMetadata> meta = new HashMap<String, ValidationMetadata>();

        @SuppressWarnings("unchecked")
        Map<String, AnnotationInfo> annotationInfoMap =
            getAnnotationInfoMap(beanType, Validate.class, ValidateNestedProperties.class);

        for (String propertyName : annotationInfoMap.keySet()) {
            AnnotationInfo annotationInfo = annotationInfoMap.get(propertyName);

            // get the @Validate and/or @ValidateNestedProperties
            Validate simple = annotationInfo.getAnnotation(Validate.class);
            ValidateNestedProperties nested = annotationInfo.getAnnotation(ValidateNestedProperties.class);
            Class<?> clazz = annotationInfo.getTargetClass();

            // add to allow list if @Validate present
            if (simple != null) {
                if (simple.field() == null || "".equals(simple.field())) {
                    meta.put(propertyName, new ValidationMetadata(propertyName, simple));
                }
                else {
                    log.warn("Field name present in @Validate but should be omitted: ",
                        clazz, ", property ", propertyName, ", given field name ",
                        simple.field());
                }
            }

            // add all sub-properties referenced in @ValidateNestedProperties
            if (nested != null) {
                Validate[] validates = nested.value();
                if (validates != null) {
                    for (Validate validate : validates) {
                        if (validate.field() != null && !"".equals(validate.field())) {
                            String fullName = propertyName + '.' + validate.field();
                            if (meta.containsKey(fullName)) {
                                log.warn("More than one nested @Validate with same field name: "
                                    + validate.field() + " on property " + propertyName);
                            }
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

        return Collections.unmodifiableMap(meta);
    }

    /**
     * Looks at a class's properties, searching for the specified annotations on the properties
     * (field, getter method, or setter method). An exception is thrown if annotations are found
     * in more than one of those three places.
     *
     * @param beanType the class on which to look for annotations.
     * @param annotationClasses the classes of the annotations for which to look for.
     * @return a map of property names to AnnotationInfo objects, which contain the class on which
     * the annotations were found (if any), and the annotation objects that correspond to the
     * annotation classes.
     */
    protected Map<String, AnnotationInfo> getAnnotationInfoMap(Class<?> beanType,
        Class<? extends Annotation>... annotationClasses)
    {
        Map<String, AnnotationInfo> annotationInfoMap = new HashMap<String, AnnotationInfo>();

        Set<String> seen = new HashSet<String>();
        try {
            for (Class<?> clazz = beanType; clazz != null; clazz = clazz.getSuperclass()) {
                List<PropertyDescriptor> pds = new ArrayList<PropertyDescriptor>(
                        Arrays.asList(ReflectUtil.getPropertyDescriptors(clazz)));

                // Also look at public fields
                Field[] publicFields = clazz.getFields();
                for (Field field : publicFields) {
                    pds.add(new PropertyDescriptor(field.getName(), null, null));
                }

                for (PropertyDescriptor pd : pds) {
                    String propertyName = pd.getName();
                    Method accessor = pd.getReadMethod();
                    Method mutator = pd.getWriteMethod();
                    Field field = null;
                    try {
                        field = clazz.getDeclaredField(propertyName);
                    }
                    catch (NoSuchFieldException e) {
                    }

                    // this method throws an exception if there are conflicts
                    AnnotationInfo annotationInfo = getAnnotationInfo(clazz, propertyName,
                        new PropertyWrapper[] {
                            new PropertyWrapper(accessor),
                            new PropertyWrapper(mutator),
                            new PropertyWrapper(field),
                        },
                        annotationClasses);

                    // after the conflict check, stop processing fields we've already seen
                    if (seen.contains(propertyName))
                        continue;

                    if (annotationInfo.atLeastOneAnnotationFound()) {
                        annotationInfoMap.put(propertyName, annotationInfo);
                        seen.add(propertyName);
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
        return annotationInfoMap;
    }

    /**
     * Looks at a class's properties, searching for the specified annotations on the given property
     * objects. An exception is thrown if annotations are found in more than one of the specified
     * property accessors (normally field, getter method, and setter method).
     *
     * @param clazz the class on which to look for annotations.
     * @param propertyName the name of the property.
     * @param propertyWrappers the property accessors.
     * @param annotationClasses the classes of the annotations for which to look for.
     * @return an AnnotationInfo object, which contains the class on which the annotations were found
     * (if any), and the annotation objects that correspond to the annotation classes.
     */
    protected AnnotationInfo getAnnotationInfo(Class<?> clazz, String propertyName,
        PropertyWrapper[] propertyWrappers, Class<? extends Annotation>... annotationClasses)
    {
        AnnotationInfo annotationInfo = new AnnotationInfo(clazz);

        Map<PropertyWrapper, Map<Class<? extends Annotation>, Annotation>> map =
            new HashMap<PropertyWrapper, Map<Class<? extends Annotation>, Annotation>>();

        for (PropertyWrapper property : propertyWrappers) {
            Map<Class<? extends Annotation>, Annotation> annotationMap =
                new HashMap<Class<? extends Annotation>, Annotation>();

            for (Class<? extends Annotation> annotationClass : annotationClasses)  {
                Annotation annotation = findAnnotation(clazz, property, annotationClass);
                if (annotation != null) {
                    annotationMap.put(annotationClass, annotation);
                }
            }
            if (!annotationMap.isEmpty()) {
                map.put(property, annotationMap);
            }
        }

        // must be 0 or 1
        if (map.size() > 1) {
            StringBuilder buf = new StringBuilder(
                "There are conflicting @Validate and/or @ValidateNestedProperties annotations in ")
                .append(clazz)
                .append(". The following elements are improperly annotated for the '")
                .append(propertyName)
                .append("' property:\n");

            for (PropertyWrapper property : map.keySet()) {
                Map<Class<? extends Annotation>, Annotation> annotationMap = map.get(property);

                buf.append("--> ").append(property.getType()).append(' ')
                   .append(property.getName()).append(" is annotated with ");

                for (Class<?> cls : annotationMap.keySet()) {
                    buf.append('@').append(cls.getSimpleName()).append(' ');
                }
                buf.append('\n');
            }
            throw new StripesRuntimeException(buf.toString());
        }
        if (!map.isEmpty()) {
            annotationInfo.setAnnotationMap(map.entrySet().iterator().next().getValue());
        }
        return annotationInfo;
    }

    /**
     * Returns an annotation (or <code>null</code> if none is found) for the given property
     * accessor of a class. The property object must not be <code>null</code>, must be declared on
     * the class, must be public if it is a method, and must not be static if it is a field, for it
     * to be considered eligible to having the annotation.
     *
     * @param clazz the class on which to look for the annotation.
     * @param property the property accessor.
     * @param annotationClass the class of the annotation to look for.
     * @return the annotation object, or <code>null</code> if no annotation was found.
     */
    protected Annotation findAnnotation(Class<?> clazz, PropertyWrapper property,
        Class<? extends Annotation> annotationClass)
    {
        AccessibleObject accessible = property.getAccessibleObject(); 
        if (accessible != null
            && property.getDeclaringClass().equals(clazz)
            && ( (accessible.getClass().equals(Method.class) && Modifier.isPublic(property.getModifiers()))
              || (accessible.getClass().equals(Field.class) && !Modifier.isStatic(property.getModifiers()))
            ))
        {
            return accessible.getAnnotation(annotationClass);
        }
        return null;
    }

    /**
     * Prints out a pretty debug message showing what validations got configured.
     */
    protected void logDebugMessageForConfiguredValidations(Class<?> beanType, Map<String, ValidationMetadata> meta) {
        StringBuilder builder = new StringBuilder(128);
        for (Map.Entry<String, ValidationMetadata> entry : meta.entrySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(entry.getKey());
            builder.append("->");
            builder.append(entry.getValue());
        }
        log.debug("Loaded validations for ActionBean ", beanType.getSimpleName(), ": ",
            builder.length() > 0 ? builder : "<none>");
    }

    /**
     * Contains the class on which the annotations were found (if any), and the annotation objects
     * that correspond to the annotation classes.
     */
    protected class AnnotationInfo {
        private Class<?> targetClass;
        private Map<Class<? extends Annotation>, Annotation> annotationMap;

        public AnnotationInfo(Class<?> targetClass) {
            this.targetClass = targetClass;
        }

        public Class<?> getTargetClass() {
            return targetClass;
        }

        public void setAnnotationMap(Map<Class<? extends Annotation>, Annotation> annotationMap) {
            this.annotationMap = annotationMap;
        }

        @SuppressWarnings("unchecked")
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return (T) annotationMap.get(annotationClass);
        }

        public boolean atLeastOneAnnotationFound() {
            return !(annotationMap == null || annotationMap.isEmpty());
        }
    }

    /**
     * For some reason, methods common to both the Field and Method classes are not in their parent
     * class, AccessibleObject, so this class works around that limitation.
     */
    protected class PropertyWrapper {
        private Field field;
        private Method method;
        private String type;

        public PropertyWrapper(Field field) {
            this.field = field;
            this.type = "Field";
        }

        public PropertyWrapper(Method method) {
            this.method = method;
            this.type = "Method";
        }

        public AccessibleObject getAccessibleObject() {
            return field != null ? field : method;
        }

        public String getName() {
            return field != null ? field.getName() : method.getName();
        }

        public Class<?> getDeclaringClass() {
            return field != null ? field.getDeclaringClass() : method.getDeclaringClass();
        }

        public int getModifiers() {
            return field != null ? field.getModifiers() : method.getModifiers();
        }

        public String getType() {
            return type;
        }
    }
}
