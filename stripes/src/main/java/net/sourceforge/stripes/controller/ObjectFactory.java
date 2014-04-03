/* Copyright 2008 Ben Gunter
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

import java.lang.reflect.Constructor;

import net.sourceforge.stripes.config.ConfigurableComponent;

/**
 * Used throughout Stripes to instantiate classes. The default implementation is
 * {@link DefaultObjectFactory}. You can specify an alternate implementation to use by setting the
 * {@code ObjectFactory.Class} initialization parameter for {@link StripesFilter} or by placing your
 * implementation in one of the packages named in {@code Extension.Packages}.
 * 
 * <pre>
 * &lt;init-param&gt;
 *  &lt;param-name&gt;ObjectFactory.Class&lt;/param-name&gt;
 *  &lt;param-value&gt;com.mycompany.stripes.ext.MyObjectFactory&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre>
 * 
 * @author Ben Gunter
 * @since Stripes 1.5.1
 */
public interface ObjectFactory extends ConfigurableComponent {
    /**
     * <p>
     * A wrapper for a {@link Constructor}. This interface provides a builder-style API for
     * instantiating classes by invoking a specific constructor. Typical usage might look like:
     * </p>
     * <code>
     * configuration.getObjectFactory().constructor(targetType, String.class).newInstance("FOO");
     * </code>
     */
    public static interface ConstructorWrapper<T> {
        /** Get the {@link Constructor} object wrapped by this instance. */
        public Constructor<T> getConstructor();

        /** Invoke the constructor with the specified arguments and return the new object. */
        public T newInstance(Object... args);
    }

    /**
     * Create a new instance of {@code clazz} and return it.
     * 
     * @param clazz The class to instantiate.
     * @return A new instances of the class.
     */
    <T> T newInstance(Class<T> clazz);

    /**
     * Create a new instances of {@code T} by invoking the given constructor.
     * 
     * @return A new object instantiated by invoking the constructor.
     */
    <T> T newInstance(Constructor<T> constructor, Object... args);

    /**
     * Create a new instance of {@code clazz} by calling a specific constructor.
     * 
     * @param clazz The class to instantiate.
     * @param constructorArgTypes The type arguments of the constructor to be invoked. (See
     *            {@link Class#getConstructor(Class...)}.)
     * @param constructorArgs The arguments to pass to the constructor. (See
     *            {@link Constructor#newInstance(Object...)}.)
     * @return A new instance of the class.
     */
    <T> T newInstance(Class<T> clazz, Class<?>[] constructorArgTypes, Object[] constructorArgs);

    /**
     * <p>
     * Provides a builder-style interface for instantiating objects by calling specific
     * constructors. Typical usage might look like:
     * </p>
     * <code>
     * configuration.getObjectFactory().constructor(targetType, String.class).newInstance("FOO");
     * </code>
     * 
     * @param clazz The class whose constructor is to be looked up.
     * @param parameterTypes The types of the parameters to the constructor.
     * @return A {@link ConstructorWrapper} that allows for invoking the constructor.
     */
    <T> ConstructorWrapper<T> constructor(Class<T> clazz, Class<?>... parameterTypes);
}