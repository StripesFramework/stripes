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
package net.sourceforge.stripes.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.stripes.action.ActionBean;


/**
 * Represents a URL binding as declared by a {@link net.sourceforge.stripes.action.UrlBinding}
 * annotation on an {@link ActionBean} class.
 *
 * @author Ben Gunter
 * @since Stripes 1.5
 */
public class UrlBinding {

   protected Class<? extends ActionBean> _beanType;
   protected String                      _path;
   protected String                      _suffix;
   protected List<Object>                _components;
   protected List<UrlBindingParameter>   _parameters;

   /**
    * Create a new instance with all its members. Collections passed in will be made immutable.
    *
    * @param beanType the {@link ActionBean} class to which this binding applies
    * @param path the path to which the action is mapped
    * @param components list of literal strings that separate the parameters
    */
   public UrlBinding( Class<? extends ActionBean> beanType, String path, List<Object> components ) {
      _beanType = beanType;
       _path = path;

      if ( components != null && !components.isEmpty() ) {
          _components = Collections.unmodifiableList(components);
         _parameters = new ArrayList<>(components.size());

         for ( Object component : components ) {
            if ( component instanceof UrlBindingParameter ) {
               _parameters.add((UrlBindingParameter)component);
            }
         }

         if ( !_parameters.isEmpty() ) {
            Object last = components.get(components.size() - 1);
            if ( last instanceof String ) {
               _suffix = (String)last;
            }
         }
      } else {
          _components = Collections.emptyList();
         _parameters = Collections.emptyList();
      }
   }

   /**
    * Create a new instance that takes no parameters.
    *
    * @param beanType
    * @param path
    */
   public UrlBinding( Class<? extends ActionBean> beanType, String path ) {
      _beanType = beanType;
       _path = path;
      _components = Collections.emptyList();
      _parameters = Collections.emptyList();
   }

   @Override
   public boolean equals( Object obj ) {
      if ( !(obj instanceof UrlBinding) ) {
         return false;
      }

      UrlBinding that = (UrlBinding)obj;
      return getBeanType() == that.getBeanType() && getComponents().equals(that.getComponents());
   }

   /**
    * Get the {@link ActionBean} class to which this binding applies.
    */
   public Class<? extends ActionBean> getBeanType() {
      return _beanType;
   }

   /**
    * Get the list of components that comprise this binding. The components are returned in the
    * order in which they appear in the binding definition.
    */
   public List<Object> getComponents() {
      return _components;
   }

   /**
    * Get the list of parameters for this binding.
    */
   public List<UrlBindingParameter> getParameters() {
      return _parameters;
   }

   /**
    * Get the path for this binding. The path is the string of literal characters in the pattern up
    * to the first parameter definition.
    */
   public String getPath() {
      return _path;
   }

   /**
    * If this binding includes one or more parameters and the last component is a {@link String},
    * then this method will return that last component. Otherwise, it returns null.
    */
   public String getSuffix() {
      return _suffix;
   }

   @Override
   public int hashCode() {
      return getPath() == null ? 0 : getPath().hashCode();
   }

   /**
    * Ensure the default event name is set if the binding uses the $event parameter.
    * Can only be done safely after the event mappings have been processed.
    * see http://www.stripesframework.org/jira/browse/STS-803
    */
   public void initDefaultValueWithDefaultHandlerIfNeeded( ActionResolver actionResolver ) {
      for ( UrlBindingParameter parameter : _parameters ) {
         parameter.initDefaultValueWithDefaultHandlerIfNeeded(actionResolver);
      }
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder(64).append(getPath());
      for ( Object component : getComponents() ) {
         if ( component instanceof UrlBindingParameter ) {
            buf.append('{').append(component).append('}');
         } else {
            buf.append(component);
         }
      }
      return buf.toString();
   }
}
