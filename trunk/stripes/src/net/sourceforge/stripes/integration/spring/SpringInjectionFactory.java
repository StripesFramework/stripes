/* Copyright 2009 Frederic Daoud
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
package net.sourceforge.stripes.integration.spring;

import javax.servlet.ServletContext;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.DefaultObjectFactory;
import net.sourceforge.stripes.util.Log;

/**
 * <p>
 * An extension of {@link DefaultObjectFactory} that calls
 * {@link SpringHelper#injectBeans((Object, ServletContext))} in the {@link #postProcess(Object)}
 * method, to inject dependencies marked with {@link SpringBean} in every type of object created
 * by Stripes (Action Beans, Interceptors, Type Converters, Formatters, etc.).
 * </p>
 * 
 * @author Freddy Daoud
 * @since Stripes 1.6
 */
public class SpringInjectionFactory extends DefaultObjectFactory {
    private static final Log log = Log.getInstance(SpringInjectionFactory.class);
    private ServletContext servletContext;

    /**
     * Gets the {@link ServletContext} so that it can be used to inject Spring beans.
     */
    @Override
    public void init(Configuration configuration) throws Exception {
        super.init(configuration);
        servletContext = configuration.getServletContext();
    }

    /**
     * Calls {@link SpringHelper#injectBeans((Object, ServletContext))} to inject dependencies
     * marked with {@link SpringBean} into the object before returning it.
     */
    @Override
    protected <T> T postProcess(T object) {
        log.debug("Running Spring dependency injection for instance of ",
            object.getClass().getSimpleName());
        SpringHelper.injectBeans(object, servletContext);
        return object;
    }
}
