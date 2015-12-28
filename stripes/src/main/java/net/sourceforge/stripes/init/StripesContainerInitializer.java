package net.sourceforge.stripes.init;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.config.ConfigurableComponent;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.ContentTypeRequestWrapper;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.ObjectPostProcessor;
import net.sourceforge.stripes.exception.AutoExceptionHandler;
import net.sourceforge.stripes.format.Formatter;
import net.sourceforge.stripes.tag.TagErrorRenderer;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.TypeConverter;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.util.HashSet;
import java.util.Set;

@HandlesTypes({
	ConfigurableComponent.class,
	Configuration.class,
	ObjectPostProcessor.class,
	ActionBean.class,
	AutoExceptionHandler.class,
	ActionBeanContext.class,
	TagErrorRenderer.class,
	ContentTypeRequestWrapper.class,
	Interceptor.class,
	Formatter.class,
	TypeConverter.class
})
public class StripesContainerInitializer implements ServletContainerInitializer {

	private static final Log log = Log.getInstance(StripesContainerInitializer.class);

	public static Set<Class<?>> LOADED_CLASSES = null;

	public static StripesContainerInitializer INSTANCE = null;

	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		LOADED_CLASSES = new HashSet<Class<?>>(c);
		INSTANCE = this;
		log.info(LOADED_CLASSES.size() + " classes loaded.");
		if (log.getRealLog().isDebugEnabled()) {
			for (Class<?> clazz : LOADED_CLASSES) {
				log.debug("  * " + clazz.getName());
			}
		}
	}

}
