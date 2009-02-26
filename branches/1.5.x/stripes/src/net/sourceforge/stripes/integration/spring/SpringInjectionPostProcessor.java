/* Copyright 2009 Frederic Daoud, Ben Gunter
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
import net.sourceforge.stripes.controller.ObjectPostProcessor;
import net.sourceforge.stripes.util.Log;

/**
 * <p>
 * An implementation of {@link ObjectPostProcessor} that calls {@link
 * SpringHelper#injectBeans((Object, ServletContext))} to inject dependencies marked with
 * {@link SpringBean} in every type of object created by Stripes (Action Beans, Interceptors, Type
 * Converters, Formatters, etc.).
 * </p>
 * 
 * @author Freddy Daoud, Ben Gunter
 * @since Stripes 1.6
 */
public class SpringInjectionPostProcessor implements ObjectPostProcessor<Object> {
    private static final Log log = Log.getInstance(SpringInjectionPostProcessor.class);
    private ServletContext servletContext;

    /** Get the servlet context from the object factory's configuration. */
    public void setObjectFactory(DefaultObjectFactory factory) {
        Configuration configuration = factory.getConfiguration();
        if (configuration == null) {
            final String name = getClass().getSimpleName();
            throw new IllegalStateException("The object factory passed to " + name
                    + " has no configuration. The configuration is required by " + name
                    + " to get the servlet context.");
        }

        ServletContext servletContext = configuration.getServletContext();
        if (this.servletContext != null && this.servletContext != servletContext) {
            final String name = getClass().getSimpleName();
            throw new IllegalStateException("An attempt was made to use a single instance of "
                    + name + " in two different servlet contexts. " + name + " instances "
                    + "cannot be shared across servlet contexts.");
        }

        this.servletContext = servletContext;
    }

    /**
     * Calls {@link SpringHelper#injectBeans((Object, ServletContext))} to inject dependencies
     * marked with {@link SpringBean} into the object before returning it.
     */
    public Object postProcess(Object object) {
        log.debug("Running Spring dependency injection for instance of ", object.getClass()
                .getSimpleName());
        SpringHelper.injectBeans(object, servletContext);
        return object;
    }
}
