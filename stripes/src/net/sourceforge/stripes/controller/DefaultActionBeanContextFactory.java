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
package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implements an ActionBeanContextFactory that allows for instantiation of application specific
 * ActionBeanContext classes. Looks for a configuration parameters called "ActionBeanContext.Class".
 * If the property is present, the named class with be instantiated and returned from the
 * getContextInstance() method.  If no class is named, then the default class, ActionBeanContext
 * will be instantiated.
 *
 * @author Tim Fennell
 */
public class DefaultActionBeanContextFactory implements ActionBeanContextFactory {
    private static final Log log = Log.getInstance(DefaultActionBeanContextFactory.class);

    /** The name of the configuration property used for the context class name. */
    public static final String CONTEXT_CLASS_NAME = "ActionBeanContext.Class";

    private Configuration configuration;
    private Class<? extends ActionBeanContext> contextClass;

    /** Stores the configuration, and looks up the ActionBeanContext class specified. */
    public void init(Configuration configuration) throws Exception {
        setConfiguration(configuration);

        Class<? extends ActionBeanContext> clazz = configuration.getBootstrapPropertyResolver()
                .getClassProperty(CONTEXT_CLASS_NAME, ActionBeanContext.class);
        if (clazz == null) {
            clazz = ActionBeanContext.class;
        }
        else {
            log.info(DefaultActionBeanContextFactory.class.getSimpleName(), " will use ",
                    ActionBeanContext.class.getSimpleName(), " subclass ", clazz.getName());
        }
        this.contextClass = clazz;
    }

    /**
     * Returns a new instance of the configured class, or ActionBeanContext if a class is
     * not specified.
     */
    public ActionBeanContext getContextInstance(HttpServletRequest request,
                                                HttpServletResponse response) throws ServletException {
        try {
            ActionBeanContext context = this.contextClass.newInstance();
            context.setRequest(request);
            context.setResponse(response);
            return context;
        }
        catch (Exception e) {
            throw new StripesServletException("Could not instantiate configured " +
            "ActionBeanContext class: " + this.contextClass, e);
        }
    }

	protected Configuration getConfiguration()
	{
		return configuration;
	}

	protected void setConfiguration(Configuration configuration)
	{
		this.configuration = configuration;
	}
}
