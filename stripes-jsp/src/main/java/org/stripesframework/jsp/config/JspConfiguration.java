package org.stripesframework.jsp.config;

import org.stripesframework.jsp.tag.BeanFirstPopulationStrategy;
import org.stripesframework.jsp.tag.DefaultTagErrorRendererFactory;
import org.stripesframework.jsp.tag.PopulationStrategy;
import org.stripesframework.jsp.tag.TagErrorRendererFactory;
import org.stripesframework.web.config.ConfigurableComponent;
import org.stripesframework.web.config.Configuration;
import org.stripesframework.web.controller.StripesFilter;
import org.stripesframework.web.exception.StripesRuntimeException;


public class JspConfiguration {

   public static final String TAG_ERROR_RENDERER_FACTORY = "TagErrorRendererFactory.Class";
   public static final String POPULATION_STRATEGY        = "PopulationStrategy.Class";

   private static final JspConfiguration INSTANCE = new JspConfiguration();

   public static JspConfiguration getInstance() {
      return INSTANCE;
   }

   private PopulationStrategy      _populationStrategy;
   private TagErrorRendererFactory _tagErrorRendererFactory;

   private JspConfiguration() {
   }

   public PopulationStrategy getPopulationStrategy() {
      if ( _populationStrategy == null ) {
         _populationStrategy = create(PopulationStrategy.class, POPULATION_STRATEGY, BeanFirstPopulationStrategy.class);
      }
      return _populationStrategy;
   }

   public TagErrorRendererFactory getTagErrorRendererFactory() {
      if ( _tagErrorRendererFactory == null ) {
         _tagErrorRendererFactory = create(TagErrorRendererFactory.class, TAG_ERROR_RENDERER_FACTORY, DefaultTagErrorRendererFactory.class);
      }
      return _tagErrorRendererFactory;
   }

   private synchronized <ComponentInterface extends ConfigurableComponent, DefaultImplementation extends ComponentInterface> ComponentInterface create(
         Class<ComponentInterface> componentType, String propertyName, Class<DefaultImplementation> defaultType ) {
      Configuration configuration = StripesFilter.getConfiguration();

      ComponentInterface component = configuration.initializeComponent(componentType, propertyName);
      if ( component != null ) {
         return component;
      }

      try {
         DefaultImplementation defaultImplementation = defaultType.getConstructor().newInstance();
         defaultImplementation.init(configuration);
         return defaultImplementation;
      }
      catch ( Exception e ) {
         throw new StripesRuntimeException(
               "Could not instantiate default implementation " + defaultType.getSimpleName() + " of type [" + defaultType.getSimpleName() + "].", e);

      }
   }

}
