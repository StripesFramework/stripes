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

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesRuntimeException;

/**
 * <p>
 * An implementation of {@link ObjectFactory} that simply calls {@link Class#newInstance()} to
 * obtain a new instance.
 * </p>
 * 
 * @author Ben Gunter
 * @since Stripes 1.5.1
 */
public class DefaultObjectFactory implements ObjectFactory {
    private Configuration configuration;

    /** Does nothing. */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
    }

    /** Get the {@link Configuration} that was passed into {@link #init(Configuration)}. */
    protected Configuration getConfiguration() {
        return configuration;
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
