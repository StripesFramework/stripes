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

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * <p>Static helper class that is used to lookup Spring beans and inject them into objects
 * (usually ActionBeans). Setter methods must be annotated using the {@code @SpringBean annotation}.
 * The value of the annotation should be the bean name in the Spring application context.  If value
 * is left blank, an attempt is made to auto-wire the bean; first by method name then by type. If
 * the value is left blank and more than one bean of the same type is found, an exception will be
 * raised.</p>
 *
 * @see SpringBean
 * @author Dan Hayes, Tim Fennell
 */
public class SpringHelper {
    private static Log log = Log.getInstance(SpringHelper.class);

    /**
     * Injects Spring managed beans into ActionBeans via the SpringBean annotation.
     * It first looks for the value attribute of the annotation for the name of the
     * Spring managed bean to inject.  If this is empty, it will attempt to "auto-wire"
     * the bean from the method name (i.e. "setSomeBean()" will resolve to "someBean").
     * Finally, it will try to look for a bean of the type expected by the method. In
     * the event that more than one Spring managed bean meets this criteria, an exception
     * will be logged and thrown.
     *
     * @param bean    the object into which to inject spring managed bean
     * @param context the ActionBeanContext represented by the current request
     * @throws Exception
     */
    public static void injectBeans(Object bean, ActionBeanContext context) throws Exception {
        HttpServletRequest request = context.getRequest();
        ApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(
                request.getSession().getServletContext());

        for (Method m : bean.getClass().getMethods()) {

            if (m.isAnnotationPresent(SpringBean.class)) {

                String beanName = m.getAnnotation(SpringBean.class).value();
                if (beanName != null && !beanName.equals("")) {
                    // use the value to lookup the bean
                    m.invoke(bean, springContext.getBean(beanName));
                    log.debug("Injected ActionBean [",  bean.getClass().getName(), "] property [",
                              m.getName(), "] with Spring bean [", beanName, "].");
                }
                else {
                    // else, try to auto-wire by property name
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod(m);
                    if ( springContext.containsBean(pd.getName()) ) {
                        m.invoke( bean, springContext.getBean(pd.getName()) );
                        log.debug("Injected ActionBean [", bean.getClass().getName(),
                                  "] property [",  pd.getName(), "] with Spring bean [",
                                  pd.getName(), "]");
                    }
                    else {
                        // or try to find by type
                        String[] beanNames = springContext.getBeanNamesForType(pd.getPropertyType());
                        if (beanNames == null || beanNames.length == 0) {
                            // didn't find any beans of that type
                            StripesServletException sse = new StripesServletException(
                                "Unable to inject ActionBean [" + bean.getClass().getName() +
                                "] property [" + pd.getName() + "]. No matching Spring beans " +
                                "could be found with the name [" + pd.getName() + "] or type [" +
                                pd.getPropertyType().getName() + "].");
                            log.error(sse);
                            throw sse;
                        }
                        else if (beanNames.length > 1) {
                            // more than one bean found of this type
                            StripesServletException sse = new StripesServletException(
                                    "Unable to inject ActionBean [" + bean.getClass().getName() +
                                    "] property [" + pd.getName() + "]. No matching Spring beans " +
                                    "could be found with the name [" + pd.getName() + "]. " +
                                    beanNames.length + " found with  type [" +
                                    pd.getPropertyType().getName() + "].");
                            log.error(sse);
                            throw sse;
                        }
                        else {
                            m.invoke(bean, springContext.getBean(beanNames[0]));
                            log.warn("Injecting ActionBean [", bean.getClass().getName(),
                                    "] property [", pd.getName(),"] with Spring bean name [",
                                    beanNames[0], "] based upon type match. Matching on type is ",
                                    "a little risky so watch out!");
                        }
                    }
                }
            }
        }
    }
}
