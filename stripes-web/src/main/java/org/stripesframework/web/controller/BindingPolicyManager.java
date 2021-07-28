/* Copyright 2007 Ben Gunter
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
package org.stripesframework.web.controller;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.exception.StripesRuntimeException;
import org.stripesframework.web.util.Log;
import org.stripesframework.web.util.ReflectUtil;
import org.stripesframework.web.util.bean.NodeEvaluation;
import org.stripesframework.web.util.bean.PropertyExpressionEvaluation;
import org.stripesframework.web.validation.ValidationMetadata;
import org.stripesframework.web.validation.ValidationMetadataProvider;


/**
 * Manages the policies observed by {@link DefaultActionBeanPropertyBinder} when binding properties
 * to an {@link ActionBean}.
 *
 * @author Ben Gunter
 */
public class BindingPolicyManager {

   /** List of classes that, for security reasons, are not allowed as a {@link NodeEvaluation} value type. */
   private static final List<Class<?>> ILLEGAL_NODE_VALUE_TYPES = Arrays.asList(ActionBeanContext.class, Class.class, ClassLoader.class, HttpSession.class,
         ServletRequest.class, ServletResponse.class);

   /** Log */
   private static final Log log = Log.getInstance(BindingPolicyManager.class);

   /** Cached instances */
   private static final Map<Class<?>, BindingPolicyManager> instances = new ConcurrentHashMap<>();

   /**
    * Get the policy manager for the given class. Instances are cached and returned on subsequent
    * calls.
    *
    * @param beanType the class whose policy manager is to be retrieved
    * @return a policy manager
    */
   public static BindingPolicyManager getInstance( Class<?> beanType ) {
      return instances.computeIfAbsent(beanType, BindingPolicyManager::new);
   }

   /** The class to which the binding policy applies */
   private final Class<?> _beanClass;

   /** The set of properties with {@literal @Validate} */
   private final Set<String> _validatedProperties;

   /**
    * Create a new instance to handle binding security for the given type.
    *
    * @param beanClass the class to which the binding policy applies
    */
   protected BindingPolicyManager( Class<?> beanClass ) {
      try {
         _beanClass = beanClass;
         _validatedProperties = getValidatedProperties(beanClass);
      }
      catch ( Exception e ) {
         log.error(e, "%%% Failure instantiating ", getClass().getName());
         StripesRuntimeException sre = new StripesRuntimeException(e.getMessage(), e);
         sre.setStackTrace(e.getStackTrace());
         throw sre;
      }
   }

   /**
    * Get the bean class.
    *
    * @return the bean class
    */
   public Class<?> getBeanClass() {
      return _beanClass;
   }

   /**
    * Indicates if binding is allowed for the given expression.
    *
    * @param eval a property expression that has been evaluated against an {@link ActionBean}
    * @return true if binding is allowed; false if not
    */
   public boolean isBindingAllowed( PropertyExpressionEvaluation eval ) {
      // Ensure no-one is trying to bind into a protected type
      if ( usesIllegalNodeValueType(eval) ) {
         return false;
      }

      // check parameter name against access lists
      String paramName = new ParameterName(eval.getExpression().getSource()).getStrippedName();
      if ( _validatedProperties.contains(paramName) ) {
         return true;
      }

      if ( isBindingDeniedLoggingRequired(eval.getBean(), paramName) ) {
         log.warn("Binding denied for action ", eval.getBean().getClass(), ", param ", paramName,
               " has no @Validate annotation. CAUTION: Before you allow binding with @Validate, take a step back and make sure that binding this parameter is safe.");
      }

      return false;
   }

   /**
    * Get all the properties and nested properties of the given class for which there is a
    * corresponding {@link ValidationMetadata}, as returned by
    * {@link ValidationMetadataProvider#getValidationMetadata(Class, ParameterName)}. The idea
    * here is that if the bean property must be validated, then it is expected that the property
    * may be bound to the bean.
    *
    * @param beanClass a class
    * @return The validated properties. If no properties are annotated then the set is empty.
    * @see ValidationMetadataProvider#getValidationMetadata(Class)
    */
   protected Set<String> getValidatedProperties( Class<?> beanClass ) {
      Set<String> properties = StripesFilter.getConfiguration().getValidationMetadataProvider().getValidationMetadata(beanClass).keySet();
      return new HashSet<>(properties);
   }

   protected boolean isBindingDeniedLoggingRequired( Object actionBean, String paramName ) {
      // Only track violation if there's an actual property on this actionBean we would bind...
      if ( paramName.contains(".") ) {
         return true; // Nested property, this definitely needs to be logged
      }

      PropertyDescriptor propertyDescriptor = ReflectUtil.getPropertyDescriptor(actionBean.getClass(), paramName);
      if ( propertyDescriptor == null ) {
         return false; // Not a property of this action, do not log
      }

      if ( propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() == null ) {
         return false; // No setter, do not log
      }

      return true;
   }

   /**
    * Indicates if any node in the given {@link PropertyExpressionEvaluation} has a value type that is assignable from
    * any of the classes listed in {@link #ILLEGAL_NODE_VALUE_TYPES}.
    *
    * @param eval a property expression that has been evaluated against an {@link ActionBean}
    * @return true if the expression uses an illegal node value type; false otherwise
    */
   protected boolean usesIllegalNodeValueType( PropertyExpressionEvaluation eval ) {
      for ( NodeEvaluation node = eval.getRootNode(); node != null; node = node.getNext() ) {
         Type type = node.getValueType();
         if ( type instanceof ParameterizedType ) {
            type = ((ParameterizedType)type).getRawType();
         }
         if ( type instanceof Class ) {
            final Class<?> nodeClass = (Class<?>)type;
            for ( Class<?> protectedClass : ILLEGAL_NODE_VALUE_TYPES ) {
               if ( protectedClass.isAssignableFrom(nodeClass) ) {
                  return true;
               }
            }
         }
      }
      return false;
   }
}
