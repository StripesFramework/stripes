/* Copyright (C) 2005 Tim Fennell
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
    protected ActionBean makeNewActionBean(Class<? extends ActionBean> type,
                                           ActionBeanContext context) throws Exception {

        ActionBean bean = super.makeNewActionBean(type, context);
        SpringHelper.injectBeans(bean, context);
        return bean;
    }
}