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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.StrictBinding.Policy;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.bean.Node;
import net.sourceforge.stripes.util.bean.PropertyExpression;
import net.sourceforge.stripes.util.bean.PropertyExpressionEvaluation;

/**
 * The policies observed by {@link DefaultActionBeanPropertyBinder} when binding properties to an
 * {@link ActionBean}.
 * 
 * @author Ben Gunter
 */
@StrictBinding(defaultPolicy = Policy.ALLOW)
public class BindingPolicyManager {
    /** The regular expression that a property name must match */
    private static final String PROPERTY_REGEX = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

    /** The compiled form of {@link #PROPERTY_REGEX} */
    private static final Pattern PROPERTY_PATTERN = Pattern.compile(PROPERTY_REGEX);

    /** Log */
    private static final Log log = Log.getInstance(BindingPolicyManager.class);

    /** Singleton instance */
    private static final BindingPolicyManager instance = new BindingPolicyManager();

    /** Get the singleton instance of the class */
    public static BindingPolicyManager getInstance() {
        return instance;
    }

    /** Maps classes to a default policy */
    private Map<Class<?>, Policy> policy = new HashMap<Class<?>, Policy>();

    /** Maps classes to a regex that matches the expressions that are allowed to bind */
    private Map<Class<?>, Pattern> allow = new HashMap<Class<?>, Pattern>();

    /** Maps classes to a regex that matches the expressions that are not allowed to bind */
    private Map<Class<?>, Pattern> deny = new HashMap<Class<?>, Pattern>();

    /** Does nothing */
    protected BindingPolicyManager() {
    }

    /**
     * Indicates if binding is allowed for the given expression.
     * 
     * @param eval a property expression that has been evaluated against an {@link ActionBean}
     * @return true if binding is allowed; false if not
     */
    public boolean isBindingAllowed(PropertyExpressionEvaluation eval) {
        PropertyExpression expression = eval.getExpression();
        Node node = expression.getRootNode();
        StringBuilder buf = new StringBuilder();
        do {
            buf.append(node.getStringValue()).append('.');
        } while ((node = node.getNext()) != null);
        if (buf.length() > 0)
            buf.setLength(buf.length() - 1);

        Class<?> beanType = eval.getBean().getClass();
        Pattern denyPattern = getDeniedPattern(beanType);
        Pattern allowPattern = getAllowedPattern(beanType);
        boolean deny = denyPattern != null && denyPattern.matcher(buf.toString()).matches();
        boolean allow = allowPattern != null && allowPattern.matcher(buf.toString()).matches();

        /*
         * if path appears on neither or both lists ( i.e. !(allow ^ deny) ) and default policy is
         * to deny access, then fail
         */
        if (getDefaultPolicy(beanType) == Policy.DENY && !(allow ^ deny))
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
     * necessary.
     * 
     * @param beanType the class to get the {@link StrictBinding} annotation for
     * @return
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
     * Get the default policy for the given class.
     * 
     * @param beanType the class whose policy is to be looked up
     * @return the policy
     */
    protected Policy getDefaultPolicy(Class<?> beanType) {
        if (policy.containsKey(beanType))
            return policy.get(beanType);

        Policy defaultPolicy = getAnnotation(beanType).defaultPolicy();
        policy.put(beanType, defaultPolicy);
        return defaultPolicy;
    }

    /**
     * Get the {@link Pattern} against which property names that are allowed to bind will be
     * matched.
     * 
     * @param beanType {@link ActionBean} type
     * @return A pattern against which property names will be matched. This method must not return
     *         null.
     */
    protected Pattern getAllowedPattern(Class<?> beanType) {
        if (allow.containsKey(beanType))
            return allow.get(beanType);

        StrictBinding annotation = getAnnotation(beanType);
        Pattern pattern = globToPattern(annotation.allow());
        allow.put(beanType, pattern);
        return pattern;
    }

    /**
     * Get the {@link Pattern} against which property names that are not allowed to bind will be
     * matched.
     * 
     * @param beanType {@link ActionBean} type
     * @return A pattern against which property names will be matched. This method must not return
     *         null.
     */
    protected Pattern getDeniedPattern(Class<?> beanType) {
        if (deny.containsKey(beanType))
            return deny.get(beanType);

        StrictBinding annotation = getAnnotation(beanType);
        Pattern pattern = globToPattern(annotation.deny());
        deny.put(beanType, pattern);
        return pattern;
    }

    /**
     * Converts a glob to a regex {@link Pattern}.
     * 
     * @param globArray an array of property name globs, each of which may be a comma separated list
     *            of globs
     * @return
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
                    if (i > 0)
                        buf.append("\\.");
                    buf.append(PROPERTY_REGEX);
                }
                else if ("**".equals(property)) {
                    if (i > 0)
                        buf.append("\\.");
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
