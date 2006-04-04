/* Copyright (C) 2006 Tim Fennell
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
package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.After;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Interceptor that inspects ActionBeans for {@link Before} and {@link After} annotations and
 * runs the annotated methods at the requested point in the request lifecycle. There is no limit
 * on the number of methods within an ActionBean that can be marked with {@code @Before} and
 * {@code @After} annotations, and individual methods may be marked with one or both annotations.</p>
 *
 * <p>To configure the BeforeAfterMethodInterceptor for use you will need to add the following to
 * your {@code web.xml} (assuming no other interceptors are yet configured):</p>
 *
 * <pre>
 * &lt;init-param&gt;
 *     &lt;param-name&gt;Interceptor.Classes&lt;/param-name&gt;
 *     &lt;param-value&gt;net.sourceforge.stripes.action.filter.BeforeAfterMethodInterceptor&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre>
 *
 * <p>If one or more interceptors are already configured in your {@code web.xml} simply separate
 * the fully qualified names of the interceptors with commas (additional whitespace is ok).</p>
 * 
 * @see net.sourceforge.stripes.action.Before
 * @see net.sourceforge.stripes.action.After
 * @author Jeppe Cramon
 * @since Stripes 1.3
 */
@Intercepts({LifecycleStage.ActionBeanResolution,
             LifecycleStage.HandlerResolution,
             LifecycleStage.BindingAndValidation,
             LifecycleStage.CustomValidation,
             LifecycleStage.EventHandling})
public class BeforeAfterMethodInterceptor implements Interceptor {
	/** Log used throughout the intercetor */
	private static final Log log = Log.getInstance(BeforeAfterMethodInterceptor.class);

    /** No arguments array used for filter method invocations */
	private static final Object[] NO_ARGUMENTS = new Object[] {};

    /** Cache of the FilterMethods for the different ActionBean classes */
	private Map<Class<? extends ActionBean>, FilterMethods> filterMethodsCache =
            new ConcurrentHashMap<Class<? extends ActionBean>, FilterMethods>();

    /**
     * Does the main work of the interceptor as decsribed in the class level javadoc.
     * Executed the before and after methods for the ActionBean as appropriate for the
     * current lifecycle stage.  Lazily examines the ActionBean to determine the set
     * of methods to execute, if it has not yet been examined.
     *
     * @param context the current ExecutionContext
     * @return a resolution if one of the Before or After methods returns one, or if the
     *         nested interceptors return one
     * @throws Exception if one of the before/after methods raises an exception
     */
	public Resolution intercept(ExecutionContext context) throws Exception {
		LifecycleStage stage = context.getLifecycleStage();
        Resolution resolution = null;

		// Run @Before methods, as long as there's a bean to run them on
		if (context.getActionBean() != null) {
            ActionBean bean = context.getActionBean();
            FilterMethods filterMethods = getFilterMethods(bean.getClass());
			List<Method> beforeMethods = filterMethods.getBeforeMethods(stage);

            for (Method method : beforeMethods) {
                resolution = invoke(bean, method, stage, Before.class);
                if (resolution != null) {
                    return resolution;
                }
            }
        }

        // Continue on and execute other filters and the lifecycle code
        resolution = context.proceed();

        // Run After filter methods (if any)
        ActionBean bean = context.getActionBean();
		FilterMethods filterMethods = getFilterMethods(bean.getClass());
		List<Method> afterMethods = filterMethods.getAfterMethods(stage);

        Resolution overrideResolution = null;
        for (Method method : afterMethods) {
            overrideResolution = invoke(bean, method, stage, After.class);
            if (overrideResolution != null) {
                return overrideResolution;
            }
        }

		return resolution;
	}

    /**
     * Helper method that will invoke the supplied method and manage any exceptions and
     * returns from the object.  Specifically it will log any exceptions except for
     * InvocationTargetExceptions which it will attempt to unwrap and rethrow.  If the method
     * returns a Resolution it will be returned; returns of other types will be ignored.
     */
    protected Resolution invoke(ActionBean bean, Method m, LifecycleStage stage,
                                Class<? extends Annotation> when) throws Exception {
        Class beanClass = bean.getClass();
        Object retval = null;

        log.debug("Calling @Before method '", m.getName(), "' at LifecycleStage '",
                  stage, "' on ActionBean '", beanClass.getSimpleName(), "'");
        try {
            retval = m.invoke(bean);
        }
        catch (IllegalArgumentException e) {
            log.error(e, "An InvalidArgumentException was raised when calling @",
                      when.getSimpleName(), " method '", m.getName(), "' at LifecycleStage '",
                      stage, "' on ActionBean '", beanClass.getSimpleName(),
                      "'. See java.lang.reflect.Method.invoke() for possible reasons.");
        }
        catch (IllegalAccessException e) {
            log.error(e, "An IllegalAccessException was raised when calling @",
                      when.getSimpleName(), " method '", m.getName(), "' at LifecycleStage '",
                      stage, "' on ActionBean '", beanClass.getSimpleName(), "'");
        }
        catch (InvocationTargetException e) {
            // Method threw an exception, so throw the real cause of it
            if (e.getCause() != null && e.getCause() instanceof Exception) {
                throw (Exception)e.getCause();
            }
            else {
                throw e;
            }
        }

        // If we got a return value and it is a resolution, return it
        if (retval != null && retval instanceof Resolution) {
            return (Resolution) retval;
        }
        else {
            return null;
        }

    }

    /**
	 * Gets the Before/After methods for the ActionBean. Lazily examines the ActionBean
     * and stores the information in a cache.  Looks for all non-abstract, no-arg methods
     * that are annotated with either {@code @Before} or {@code @After}.
     *
	 * @param beanClass The action bean class to get methods for.
	 * @return The before and after methods for the ActionBean
	 */
	protected FilterMethods getFilterMethods(Class<? extends ActionBean> beanClass) {
		FilterMethods filterMethods = filterMethodsCache.get(beanClass);
		if (filterMethods == null) {
			filterMethods = new FilterMethods();
			filterMethodsCache.put(beanClass, filterMethods);
		
			// Look for @Before and @After annotations on the methods in the ActionBean class
			Method[] methods = beanClass.getMethods();
            for (Method method : methods) {
                int mods = method.getModifiers();

                if (method.isAnnotationPresent(Before.class)) {
                    if (method.getParameterTypes().length == 0 && !Modifier.isAbstract(mods)) {
                        Before annotation = method.getAnnotation(Before.class);
                        filterMethods.addBeforeMethod(annotation.value(), method);
                    }
                    else {
                        log.warn("Method '", beanClass.getName(), ".", method.getName(), "' is ",
                                 "annotated with @Before but has an incompatible signature. ",
                                 "@Before methods must be non-abstract zero-argument methods.");
                    }
                }

                if (method.isAnnotationPresent(After.class)) {
                    if (method.getParameterTypes().length == 0 && !Modifier.isAbstract(mods)) {
                        After annotation = method.getAnnotation(After.class);
                        filterMethods.addAfterMethod(annotation.value(), method);
                    }
                    else {
                        log.warn("Method '", beanClass.getName(), ".", method.getName(), "' is ",
                                 "annotated with @After but has an incompatible signature. ",
                                 "@After methods must be non-abstract zero-argument methods.");
                    }
                }

            }
		}
		return filterMethods;
	}
	
	/**
	 * Helper class used to collect Before and After methods for a class and provide easy
     * and rapid access to them by LifecycleStage.
     *
	 * @author Jeppe Cramon
	 */
	protected static class FilterMethods {
		/** Map of Before methods, keyed by the LifecycleStage that they should be invoked before. */
		private Map<LifecycleStage, List<Method>> beforeMethods = new HashMap<LifecycleStage, List<Method>>();

        /** Map of After methods, keyed by the LifecycleStage that they should be invoked after. */
		private Map<LifecycleStage, List<Method>> afterMethods = new HashMap<LifecycleStage, List<Method>>();
		
		/**
		 * Adds a method to be executed before the supplied LifecycleStages.
         *
		 * @param stages All the LifecycleStages that the given filter method should be invoked before
		 * @param method The filter method to be invoked before the given LifecycleStage(s)
		 */
		public void addBeforeMethod(LifecycleStage[] stages, Method method) {
			for (LifecycleStage stage : stages) {
                if (stage == LifecycleStage.ActionBeanResolution) {
                    log.warn("LifecycleStage.ActionBeanResolution is unsupported for @Before ",
                             "methods. Method '", method.getDeclaringClass().getName(), ".",
                             method.getName(), "' will not be invoked for this stage.");
                }
                else {
                    addFilterMethod(beforeMethods, stage, method);
                }
            }
		}
		
		/**
         * Adds a method to be executed after the supplied LifecycleStages.
         *
         * @param stages All the LifecycleStages that the given filter method should be invoked after
         * @param method The filter method to be invoked after the given LifecycleStage(s)
		 */
		public void addAfterMethod(LifecycleStage[] stages, Method method) {
			for (LifecycleStage stage : stages) {
                addFilterMethod(afterMethods, stage, method);
            }
		}

        /**
         * Helper method to add methods to a method map keyed by the LifecycleStage.
         *
         * @param methodMap The map of methods
         * @param stage The LifecycleStage under which to put the method
         * @param method The method that should be added to the method map
         */
        private void addFilterMethod(Map<LifecycleStage, List<Method>> methodMap,
                                     LifecycleStage stage, Method method) {
            List<Method> methods = methodMap.get(stage);
            if (methods == null) {
                methods = new ArrayList<Method>();
                methodMap.put(stage, methods);
            }
            methods.add(method);
        }
		
		/**
		 * Gets the Before methods for the given LifecycleStage.
         *
		 * @param stage The LifecycleStage to find Before methods for.
		 * @return A List of before methods, possibly zero length but never null
		 */
		public List<Method> getBeforeMethods(LifecycleStage stage) {
            List<Method> methods = beforeMethods.get(stage);
            if (methods == null) methods = Collections.emptyList();
            return methods;
        }

        /**
         * Gets the Before methods for the given LifecycleStage.
         *
         * @param stage The LifecycleStage to find Before methods for.
         * @return A List of before methods, possibly zero length but never null
         */
		public List<Method> getAfterMethods(LifecycleStage stage) {
            List<Method> methods = afterMethods.get(stage);
            if (methods == null) methods = Collections.emptyList();
            return methods;
		}
	}
}
