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
package net.sourceforge.stripes.controller;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.StrictBinding.Policy;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.bean.PropertyExpressionEvaluation;
import net.sourceforge.stripes.validation.ValidationMetadata;
import net.sourceforge.stripes.validation.ValidationMetadataProvider;

/**
 * Manages the policies observed by {@link DefaultActionBeanPropertyBinder} when binding properties
 * to an {@link ActionBean}.
 * 
 * @author Ben Gunter
 * @see StrictBinding
 */
@StrictBinding(defaultPolicy = Policy.ALLOW)
public class BindingPolicyManager {
    /** The regular expression that a property name must match */
    private static final String PROPERTY_REGEX = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

    /** The compiled form of {@link #PROPERTY_REGEX} */
    private static final Pattern PROPERTY_PATTERN = Pattern.compile(PROPERTY_REGEX);

    /** Log */
    private static final Log log = Log.getInstance(BindingPolicyManager.class);

    /** Cached instances */
    private static final Map<Class<?>, BindingPolicyManager> instances = new HashMap<Class<?>, BindingPolicyManager>();

    /**
     * Get the policy manager for the given class. Instances are cached and returned on subsequent
     * calls.
     * 
     * @param beanType the class whose policy manager is to be retrieved
     * @return a policy manager
     */
    public static BindingPolicyManager getInstance(Class<?> beanType) {
        if (instances.containsKey(beanType))
            return instances.get(beanType);

        BindingPolicyManager instance = new BindingPolicyManager(beanType);
        instances.put(beanType, instance);
        return instance;
    }

    /** The class to which the binding policy applies */
    private Class<?> beanClass;

    /** The default policy to honor, in case of conflicts */
    private Policy defaultPolicy;

    /** The regular expression that allowed properties must match */
    private Pattern allowPattern;

    /** The regular expression that denied properties must match */
    private Pattern denyPattern;

    /** The regular expression that matches properties with {@literal @Validate} */
    private Pattern validatePattern;

    /**
     * Create a new instance to handle binding security for the given type.
     * 
     * @param beanClass the class to which the binding policy applies
     */
    protected BindingPolicyManager(Class<?> beanClass) {
        try {
            log.debug("Creating ", getClass().getName(), " for ", beanClass,
                    " with default policy ", defaultPolicy);
            this.beanClass = beanClass;

            // process the annotation
            StrictBinding annotation = getAnnotation(beanClass);
            if (annotation != null) {
                // set default policy
                this.defaultPolicy = annotation.defaultPolicy();

                // construct the allow pattern
                this.allowPattern = globToPattern(annotation.allow());

                // construct the deny pattern
                this.denyPattern = globToPattern(annotation.deny());

                // construct the validated properties pattern
                this.validatePattern = globToPattern(getValidatedProperties(beanClass));
            }
        }
        catch (Exception e) {
            log.error(e, "%%% Failure instantiating ", getClass().getName());
            StripesRuntimeException sre = new StripesRuntimeException(e.getMessage(), e);
            sre.setStackTrace(e.getStackTrace());
            throw sre;
        }
    }

    /**
     * Indicates if binding is allowed for the given expression.
     * 
     * @param eval a property expression that has been evaluated against an {@link ActionBean}
     * @return true if binding is allowed; false if not
     */
    public boolean isBindingAllowed(PropertyExpressionEvaluation eval) {
        // Ensure no-one is trying to bind into the ActionBeanContext!!
        Type firstNodeType = eval.getRootNode().getValueType();
        if (firstNodeType instanceof Class
                && ActionBeanContext.class.isAssignableFrom((Class<?>) firstNodeType)) {
            return false;
        }

        // check parameter name against access lists
        String paramName = new ParameterName(eval.getExpression().getSource()).getStrippedName();
        boolean deny = denyPattern != null && denyPattern.matcher(paramName).matches();
        boolean allow = (allowPattern != null && allowPattern.matcher(paramName).matches())
                || (validatePattern != null && validatePattern.matcher(paramName).matches());

        /*
         * if path appears on neither or both lists ( i.e. !(allow ^ deny) ) and default policy is
         * to deny access, then fail
         */
        if (defaultPolicy == Policy.DENY && !(allow ^ deny))
            return false;

        /*
         * regardless of default policy, if it's in the deny list but not in the allow list, then
         * fail
         */
        if (!allow && deny)
            return false;

        // any other conditions pass the test
        return true;
    }

    /**
     * Get the {@link StrictBinding} annotation for a class, checking all its superclasses if
     * necessary. If no annotation is found, then one will be returned whose default policy is to
     * allow binding to all properties.
     * 
     * @param beanType the class to get the {@link StrictBinding} annotation for
     * @return An annotation. This method never returns null.
     */
    protected StrictBinding getAnnotation(Class<?> beanType) {
        StrictBinding annotation;
        do {
            annotation = beanType.getAnnotation(StrictBinding.class);
        } while (annotation == null && (beanType = beanType.getSuperclass()) != null);
        if (annotation == null) {
            annotation = getClass().getAnnotation(StrictBinding.class);
        }
        return annotation;
    }

    /**
     * Get all the properties and nested properties of the given class for which there is a
     * corresponding {@link ValidationMetadata}, as returned by
     * {@link ValidationMetadataProvider#getValidationMetadata(Class, String)}. The idea here is
     * that if the bean property must be validated, then it is expected that the property may be
     * bound to the bean.
     * 
     * @param beanClass a class
     * @return The validated properties. If no properties are annotated then null.
     * @see ValidationMetadataProvider#getValidationMetadata(Class)
     */
    protected String[] getValidatedProperties(Class<?> beanClass) {
        Set<String> properties = StripesFilter.getConfiguration().getValidationMetadataProvider()
                .getValidationMetadata(beanClass).keySet();
        return new ArrayList<String>(properties).toArray(new String[properties.size()]);
    }

    /**
     * Get the bean class.
     * 
     * @return the bean class
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * Get the default policy.
     * 
     * @return the policy
     */
    public Policy getDefaultPolicy() {
        return defaultPolicy;
    }

    /**
     * Converts a glob to a regex {@link Pattern}.
     * 
     * @param globArray an array of property name globs, each of which may be a comma separated list
     *            of globs
     * @return the pattern
     */
    protected Pattern globToPattern(String... globArray) {
        if (globArray == null || globArray.length == 0)
            return null;

        // things are much easier if we convert to a single list
        List<String> globs = new ArrayList<String>();
        for (String glob : globArray) {
            String[] subs = glob.split("(\\s*,\\s*)+");
            for (String sub : subs) {
                globs.add(sub);
            }
        }

        List<String> subs = new ArrayList<String>();
        StringBuilder buf = new StringBuilder();
        for (String glob : globs) {
            buf.setLength(0);
            String[] properties = glob.split("\\.");
            for (int i = 0; i < properties.length; i++) {
                String property = properties[i];
                if ("*".equals(property)) {
                    buf.append(PROPERTY_REGEX);
                }
                else if ("**".equals(property)) {
                    buf.append(PROPERTY_REGEX).append("(\\.").append(PROPERTY_REGEX).append(")*");
                }
                else if (property.length() > 0) {
                    Matcher matcher = PROPERTY_PATTERN.matcher(property);
                    if (matcher.matches()) {
                        buf.append(property);
                    }
                    else {
                        log.warn("Invalid property name: " + property);
                        return null;
                    }
                }

                // add a literal dot after all but the last
                if (i < properties.length - 1)
                    buf.append("\\.");
            }

            // add to the list of subs
            if (buf.length() != 0)
                subs.add(buf.toString());
        }

        // join subs together with pipes and compile
        buf.setLength(0);
        for (String sub : subs) {
            buf.append(sub).append('|');
        }
        if (buf.length() > 0)
            buf.setLength(buf.length() - 1);
        log.debug("Translated globs ", Arrays.toString(globArray), " to regex ", buf);

        // return null if pattern is empty
        if (buf.length() == 0)
            return null;
        else
            return Pattern.compile(buf.toString());
    }
}
