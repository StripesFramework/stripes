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

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.controller.NameBasedActionResolver;

/**
 * <p>An extension of {@link net.sourceforge.stripes.controller.NameBasedActionResolver} that
 * uses a Spring context to inject Spring beans into newly created ActionBeans before handing
 * them back for processing.</p>
 *
 * <p>This can be configured through web.xml by adding the following init parameters to the
 * Stripes filter:</p>
 *
 * <pre>
 * &lt;init-param&gt;
 *     &lt;param-name&gt;ActionResolver.Class&lt;/param-name&gt;
 *     &lt;param-value&gt;net.sourceforge.stripes.integration.spring.SpringAwareActionResolver&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre>
 *
 * @see SpringBean
 * @author Tim Fennell
 * @deprecated Use {@link SpringInterceptor} instead.
 */
@Deprecated
public class SpringAwareActionResolver extends NameBasedActionResolver {

    /**
     * Overridden method to inject Spring beans into ActionBeans after instantiation.
     *
     * @param type the Class of ActionBean being created
     * @param context the current ActionBeanContext
     * @return the newly instantiated ActionBean
     * @throws Exception if configured SpringBeans cannot be located, or are ambiguous
     */
    @Override
    protected ActionBean makeNewActionBean(Class<? extends ActionBean> type,
                                           ActionBeanContext context) throws Exception {

        ActionBean bean = super.makeNewActionBean(type, context);
        SpringHelper.injectBeans(bean, context);
        return bean;
    }
}