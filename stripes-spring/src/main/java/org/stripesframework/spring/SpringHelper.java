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
package org.stripesframework.spring;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.stripesframework.web.controller.StripesFilter;
import org.stripesframework.web.exception.StripesRuntimeException;
import org.stripesframework.web.util.ReflectUtil;


/**
 * <p>Static helper class that is used to lookup Spring beans and inject them into objects
 * (often ActionBeans). Is capable of injecting beans through setter methods (property access)
 * and also through direct field access if the security policy allows it. Methods and fields
 * must be annotated using the {@code @Autowired} annotation.</p>
 *
 * <p>Methods and fields may be public, protected, package-access or private. If they are not
 * public an attempt is made to call {@link Method#setAccessible(boolean)} in order to make
 * them accessible from this class.  If the attempt fails, an exception will be thrown.</p>
 *
 * <p>Method names can take any form.  For example {@code setSomeBean(Bean b)} or
 * {@code someBean(bean b)}. In both cases, if a specific SpringBean name is not supplied,
 * the default name of {@code someBean} will be used.</p>
 *
 * <p>The value of the {@code @Qualifier} annotation should be the bean name in the Spring
 * application context if it is different from the field/property name.  If the value
 * is left blank, an attempt is made to auto-wire the bean; first by field/property name and
 * then by type. If the value is left blank and more than one bean of the same type is found,
 * an exception will be raised.</p>
 *
 * <p>The first time that any of the injection methods in this class is called with a specific type
 * of object, the object's class is examined for annotated fields and methods. The discovered
 * fields and methods are then cached for future usage.</p>
 *
 * @see Autowired
 * @see Qualifier
 * @author Dan Hayes, Tim Fennell
 */
public class SpringHelper {

   private static final Map<Class<?>, List<Injection>> injectionLookup = new ConcurrentHashMap<>();

   /**
    * Looks up a Spring managed bean from an Application Context. First looks for a bean
    * with name specified. If no such bean exists, looks for a bean by type. If there is
    * only one bean of the appropriate type, it is returned. If zero or more than one bean
    * of the correct type exists, an exception is thrown.
    *
    * @param ctx the Spring Application Context
    * @param name the name of the spring bean to look for
    * @param type the type of bean to look for
    * @param required true if this bean is required to be found
    * @exception RuntimeException various subclasses of RuntimeException are thrown if it
    *            is not possible to find a unique matching bean in the spring context given
    *            the constraints supplied.
    */
   public static Object findSpringBean( ApplicationContext ctx, String name, Class<?> type, boolean required ) {

      String[] beanNames = ctx.getBeanNamesForType(type);
      if ( beanNames.length == 0 ) {
         if ( required ) {
            throw new StripesRuntimeException(
                  "Unable to find SpringBean with name [" + name + "] or type [" + type.getName() + "] in the Spring application context.");
         } else {
            return null;
         }
      } else if ( beanNames.length > 1 ) {
         boolean found = false;
         for ( String beanName : beanNames ) {
            if ( beanName.equals(name) ) {
               found = true;
               break;
            }
         }
         if ( !found ) {
            if ( required ) {
               throw new StripesRuntimeException("Unable to find SpringBean with name [" + name + "] or unique bean with type [" + type.getName()
                     + "] in the Spring application context. Found " + beanNames.length + "beans of matching type.");
            } else {
               return null;
            }
         }
      } else {
         name = beanNames[0];
      }

      return ctx.getBean(name, type);
   }

   /**
    * Injects Spring managed beans using a Web Application Context derived from
    * the ServletContext.
    *
    * @param bean the object to have beans injected into
    * @param ctx the ServletContext to use to find the Spring ApplicationContext
    */
   public static void injectBeans( Object bean, ServletContext ctx ) {
      ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(ctx);

      if ( ac == null ) {
         final String name = ctx.getServletContextName();
         throw new IllegalStateException("No Spring application context was found in servlet context \"" + name + "\"");
      }

      injectBeans(bean, ac);
   }

   /**
    * Looks for all methods and fields annotated with {@code @Autowired} and attempts
    * to lookup and inject a managed bean into the field/property. If any annotated
    * element cannot be injected an exception is thrown.
    *
    * @param bean the bean into which to inject spring beans
    * @param ctx the Spring application context
    */
   public static void injectBeans( Object bean, ApplicationContext ctx ) {
      List<Injection> injections = injectionLookup.computeIfAbsent(bean.getClass(), SpringHelper::computeInjections);

      for ( Injection injection : injections ) {
         injection.inject(bean, ctx);
      }
   }

   /**
    * Injects Spring managed beans into using a Web Application Context that is
    * derived from the ServletContext, which is in turn looked up using the
    * Stripes configuration.
    *
    * @param bean    the object into which to inject spring managed bean
    */
   public static void injectBeans( Object bean ) {
      injectBeans(bean, StripesFilter.getConfiguration().getServletContext());
   }

   /**
    * A slightly unusual, and somewhat "loose" conversion of a method name to a property
    * name. Assumes that the name is in fact a mutator for a property and will do the
    * usual {@code setFoo} to {@code foo} conversion if the method follows the normal
    * syntax, otherwise will just return the method name.
    *
    * @param m the method to determine the property name of
    * @return a String property name
    */
   protected static String methodToPropertyName( Method m ) {
      String name = m.getName();
      if ( name.startsWith("set") && name.length() > 3 ) {
         String ret = name.substring(3, 4).toLowerCase();
         if ( name.length() > 4 ) {
            ret += name.substring(4);
         }
         return ret;
      } else {
         return name;
      }
   }

   private static void addFields( Class<?> clazz, List<Injection> injections ) {
      Collection<Field> fields = ReflectUtil.getFields(clazz);

      for ( Field field : fields ) {
         Autowired autowired = field.getAnnotation(Autowired.class);
         if ( autowired == null ) {
            continue;
         }

         if ( !field.isAccessible() ) {
            // If the field isn't public, try to make it accessible
            try {
               field.setAccessible(true);
            }
            catch ( SecurityException se ) {
               throw new StripesRuntimeException(
                     "Field " + clazz.getName() + "." + field.getName() + "is marked " + "with @Autowired and is not public. An attempt to call "
                           + "setAccessible(true) resulted in a SecurityException. Please " + "either make the field public, annotate a public setter instead "
                           + "or modify your JVM security policy to allow Stripes to " + "setAccessible(true).", se);
            }
         }

         injections.add(new FieldInjection(field, getQualifier(field), autowired.required()));
      }
   }

   private static void addMethods( Class<?> clazz, List<Injection> injections ) {
      Collection<Method> methods = ReflectUtil.getMethods(clazz);
      for ( Method method : methods ) {
         Autowired autowired = method.getAnnotation(Autowired.class);
         if ( autowired == null ) {
            continue;
         }

         // If the method isn't public, try to make it accessible
         if ( !method.isAccessible() ) {
            try {
               method.setAccessible(true);
            }
            catch ( SecurityException se ) {
               throw new StripesRuntimeException(
                     "Method " + clazz.getName() + "." + method.getName() + "is marked " + "with @Autowired and is not public. An attempt to call "
                           + "setAccessible(true) resulted in a SecurityException. Please " + "either make the method public or modify your JVM security "
                           + "policy to allow Stripes to setAccessible(true).", se);
            }
         }

         // Ensure the method has only the one parameter
         if ( method.getParameterTypes().length != 1 ) {
            throw new StripesRuntimeException(
                  "A method marked with @Autowired must have exactly one parameter: " + "the bean to be injected. Method [" + method.toGenericString()
                        + "] has " + method.getParameterTypes().length + " parameters.");
         }

         injections.add(new MethodInjection(method, getQualifier(method), autowired.required()));
      }
   }

   private static List<Injection> computeInjections( Class<?> clazz ) {
      List<Injection> injections = new ArrayList<>();
      addMethods(clazz, injections);
      addFields(clazz, injections);

      return injections;
   }

   private static String getQualifier( AccessibleObject fieldOrMethod ) {
      Qualifier qualifier = fieldOrMethod.getAnnotation(Qualifier.class);
      if ( qualifier == null ) {
         return null;
      }

      return qualifier.value();
   }

   private interface Injection {

      void inject( Object bean, ApplicationContext ctx );
   }


   private static class FieldInjection implements Injection {

      private final Field    _field;
      private final boolean  _nameSupplied;
      private final boolean  _required;
      private final String   _name;
      private final Class<?> _beanType;

      public FieldInjection( Field field, String qualifier, boolean required ) {
         _field = field;
         _nameSupplied = qualifier != null && !qualifier.isEmpty();
         _required = required;
         _name = _nameSupplied ? qualifier : field.getName();
         _beanType = field.getType();
      }

      @Override
      public void inject( Object bean, ApplicationContext ctx ) {
         try {
            Object managedBean = findSpringBean(ctx, _name, _beanType, _required);
            if ( managedBean != null ) {
               _field.set(bean, managedBean);
            }
         }
         catch ( Exception e ) {
            throw new StripesRuntimeException(
                  "Exception while trying to lookup and inject " + "a Spring bean into a bean of type " + bean.getClass().getSimpleName()
                        + " using field access on field " + _field.toString(), e);
         }
      }
   }


   private static class MethodInjection implements Injection {

      private final Method   _method;
      private final boolean  _nameSupplied;
      private final boolean  _required;
      private final String   _name;
      private final Class<?> _beanType;

      public MethodInjection( Method method, String qualifier, boolean required ) {
         _method = method;
         _nameSupplied = qualifier != null && !qualifier.isEmpty();
         _required = required;
         _name = _nameSupplied ? qualifier : methodToPropertyName(method);
         _beanType = method.getParameterTypes()[0];
      }

      @Override
      public void inject( Object bean, ApplicationContext ctx ) {
         try {
            Object managedBean = findSpringBean(ctx, _name, _beanType, _required);
            _method.invoke(bean, managedBean);
         }
         catch ( Exception e ) {
            throw new StripesRuntimeException(
                  "Exception while trying to lookup and inject " + "a Spring bean into a bean of type " + bean.getClass().getSimpleName() + " using method "
                        + _method.toString(), e);
         }
      }
   }
}
