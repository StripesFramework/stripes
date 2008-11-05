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
package net.sourceforge.stripes.util;

import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.exception.StripesRuntimeException;

/**
 * <p>
 * This class is responsible for creating new objects during the binding stage of the request cycle.
 * This implementation simply calls {@link Class#newInstance()} to obtain a new instance.
 * </p>
 * <p>
 * You can use an alternate implementation by specifying the class name in the {@link StripesFilter}
 * init-param {@code ObjectFactory.Class} or by placing your implementation in one of the packages
 * named in {@code Extension.Packages}. The specified class must, of course, be a subclass of
 * {@link ObjectFactory}.
 * </p>
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
public class ObjectFactory {
    private static final Log log = Log.getInstance(ObjectFactory.class);

    /**
     * Name of the {@link StripesFilter} init-param that can be used to indicate the class name of
     * the preferred subclass of {@link ObjectFactory} to use.
     */
    public static final String CONFIGURATION_PARAMETER = "ObjectFactory.Class";

    /** Singleton instance */
    private static ObjectFactory instance;

    /**
     * Get the singleton object factory. By default, this method returns an instance of
     * {@link ObjectFactory}. The {@link StripesFilter} init-param named {@code ObjectFactory.Class}
     * can be used to provide an alternate implementation.
     */
    public static ObjectFactory getInstance() {
        if (instance == null) {
            Class<? extends ObjectFactory> type = StripesFilter.getConfiguration()
                    .getBootstrapPropertyResolver().getClassProperty(CONFIGURATION_PARAMETER,
                            ObjectFactory.class);

            if (type == null) {
                instance = new ObjectFactory();
            }
            else {
                try {
                    instance = type.newInstance();
                }
                catch (InstantiationException e) {
                    throw new StripesRuntimeException(e);
                }
                catch (IllegalAccessException e) {
                    throw new StripesRuntimeException(e);
                }
            }

            log.info("ObjectFactory implementation is " + instance.getClass().getName());
        }

        return instance;
    }

    /** Set the singleton instance. */
    public static void setInstance(ObjectFactory instance) {
        ObjectFactory.instance = instance;
    }

    /**
     * Calls {@link Class#newInstance()} and returns the newly created object.
     * 
     * @param clazz The class to instantiate.
     * @return The new object
     */
    public <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        }
        catch (InstantiationException e) {
            throw new StripesRuntimeException("Could not instantiate " + clazz, e);
        }
        catch (IllegalAccessException e) {
            throw new StripesRuntimeException("Could not instantiate " + clazz, e);
        }
    }
}
