package net.sourceforge.stripes.init;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.config.ConfigurableComponent;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.AutoExceptionHandler;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.util.HashSet;
import java.util.Set;

@HandlesTypes({
	ConfigurableComponent.class,
	Configuration.class,
	ActionBean.class,
	AutoExceptionHandler.class,
	ActionBeanContext.class
})
public class StripesContainerInitializer implements ServletContainerInitializer {

	public static Set<Class<?>> LOADED_CLASSES = null;

	public static StripesContainerInitializer INSTANCE = null;

	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		LOADED_CLASSES = new HashSet<Class<?>>(c);
		INSTANCE = this;
	}

}
