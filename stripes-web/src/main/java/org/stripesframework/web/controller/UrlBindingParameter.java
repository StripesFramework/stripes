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

import java.lang.reflect.Method;

import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.HandlesEvent;
import org.stripesframework.web.exception.StripesRuntimeException;
import org.stripesframework.web.exception.StripesServletException;


/**
 * A parameter to a clean URL.
 *
 * @author Ben Gunter
 * @since Stripes 1.5
 */
public class UrlBindingParameter {

   /** The special parameter name for the event to execute */
   public static final String PARAMETER_NAME_EVENT = "$event";

   protected Class<? extends ActionBean> _beanClass;
   protected String                      _name;
   protected String                      _value;
   protected String                      _defaultValue;

   /**
    * Create a new {@link UrlBindingParameter} with the given name and value. The
    * {@link #_defaultValue} will be null.
    *
    * @param name parameter name
    * @param value parameter value
    */
   public UrlBindingParameter( Class<? extends ActionBean> beanClass, String name, String value ) {
      this(beanClass, name, value, null);
   }

   /**
    * Create a new {@link UrlBindingParameter} with the given name, value and default value.
    *
    * @param name parameter name
    * @param value parameter value
    * @param defaultValue default value to use if value is null
    */
   public UrlBindingParameter( Class<? extends ActionBean> beanClass, String name, String value, String defaultValue ) {
      _beanClass = beanClass;
      _name = name;
      _value = value;
      _defaultValue = defaultValue;
   }

   /**
    * Make an exact copy of the given {@link UrlBindingParameter}.
    *
    * @param prototype a parameter
    */
   public UrlBindingParameter( UrlBindingParameter prototype ) {
      this(prototype._beanClass, prototype._name, prototype._value, prototype._defaultValue);
   }

   /**
    * Make a copy of the given {@link UrlBindingParameter} except that the parameter's value will
    * be set to <code>value</code>.
    *
    * @param prototype a parameter
    * @param value the new parameter value
    */
   public UrlBindingParameter( UrlBindingParameter prototype, String value ) {
      this(prototype._beanClass, prototype._name, value, prototype._defaultValue);
   }

   @Override
   public boolean equals( Object o ) {
      if ( !(o instanceof UrlBindingParameter) ) {
         return false;
      }

      UrlBindingParameter that = (UrlBindingParameter)o;
      return _value == null ? that._value == null : _value.equals(that._value);
   }

   /** Get the {@link ActionBean} class to which the {@link UrlBinding} applies. */
   public Class<? extends ActionBean> getBeanClass() {
      return _beanClass;
   }

   /**
    * Get the parameter's default value, which may be null.
    *
    * @return the default value
    */
   public String getDefaultValue() {
      return _defaultValue;
   }

   /**
    * Get the parameter name.
    *
    * @return parameter name
    */
   public String getName() {
      return _name;
   }

   /**
    * Return the parameter value that was extracted from a URI.
    *
    * @return parameter value
    */
   public String getValue() {
      return _value;
   }

   @Override
   public int hashCode() {
      return getName().hashCode();
   }

   @Override
   public String toString() {
      if ( _defaultValue == null ) {
         return _name;
      } else {
         return _name + "=" + _defaultValue;
      }
   }

   /**
    * Ensure the default event name is set if the binding uses the $event parameter.
    * Can only be done safely after the event mappings have been processed.
    * see http://www.stripesframework.org/jira/browse/STS-803
    */
   void initDefaultValueWithDefaultHandlerIfNeeded( ActionResolver actionResolver ) {
      if ( PARAMETER_NAME_EVENT.equals(_name) ) {
         Method defaultHandler;
         try {
            defaultHandler = actionResolver.getDefaultHandler(_beanClass);
         }
         catch ( StripesServletException e ) {
            throw new StripesRuntimeException("Caught an exception trying to get default handler for ActionBean '" + _beanClass.getName()
                  + "'. Make sure this ActionBean has a default handler.", e);
         }
         HandlesEvent annotation = defaultHandler.getAnnotation(HandlesEvent.class);
         if ( annotation != null ) {
            _defaultValue = annotation.value();
         } else {
            _defaultValue = defaultHandler.getName();
         }
      }
   }
}
