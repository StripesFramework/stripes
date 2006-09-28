/* Copyright 2005-2006 Tim Fennell
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

import net.sourceforge.stripes.config.ConfigurableComponent;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.Interceptor;

import javax.servlet.ServletContext;

/**
 * <p>Base class for developing Interceptors with dependencies on Spring managed beans. <b>Not</b>
 * to be confused with {@link SpringInterceptor} which injects Spring managed beans into
 * ActionBeans.  For example, you may wish to sublcass this class in order to write an
 * interceptor with access to Spring managed DAOs or security information.</p>
 *
 * <p>Since Interceptors are long-lived objects that are instantiated at application startup
 * time, and not per-request, the spring wiring takes place in the init() method and happens
 * only once when the interceptor is first created and initialized.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.4 
 */
public abstract class SpringInterceptorSupport implements Interceptor, ConfigurableComponent {

    /**
     * Fetches the ServletContext and invokes SpringHelper.injectBeans() to auto-wire any
     * Spring dependencies prior to being placed into service.
     *
     * @param configuration the Stripes Configuration
     * @throws Exception if there are problems with the Spring configuraiton/wiring
     */
    public void init(Configuration configuration) throws Exception {
        ServletContext ctx = configuration.getBootstrapPropertyResolver()
                                    .getFilterConfig().getServletContext();

        SpringHelper.injectBeans(this, ctx);
    }
}
