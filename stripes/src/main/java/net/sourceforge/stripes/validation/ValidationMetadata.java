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
package net.sourceforge.stripes.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * <p>Encapsulates the validation metadata for a single property of a single class. Structure
 * is purposely very similar to the @Validate annotation. This class is used internally
 * to store and manipulate validation metadata, the source of which is often validation
 * annotations.</p>
 *
 * <p>However, since this class is not an annotation it has the added benefits of being
 * able to contain behaviour, being subclassable, and of being able to be instantiated at
 * runtime - i.e. it can contain non-static validation information.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.5
 */
public class ValidationMetadata {

   private final String                         _property;
   private       boolean                        _encrypted;
   private       boolean                        _required;
   private       boolean                        _trim;
   private       Set<String>                    _on;
   private       boolean                        _onIsPositive;
   private       boolean                        _ignore;
   private       Integer                        _minlength;
   private       Integer                        _maxlength;
   private       Double                         _minvalue;
   private       Double                         _maxvalue;
   private       Pattern                        _mask;
   private       String                         _expression;
   @SuppressWarnings("rawtypes")
   private       Class<? extends TypeConverter> _converter;
   private       String                         _label;

   /**
    * Constructs a ValidationMetadata object for the specified property. Further constraints
    * can be specified by calling individual methods, e.g.
    * {@code  new ValidationMetadata("username").minlength(5).maxlength(10);}
    *
    * @param property the name of the property to be validated. If the property is a nested
    *        property then the fully path should be included, e.g. {@code user.address.city}
    */
   public ValidationMetadata( String property ) {
      _property = property;
   }

   /**
    * Essentially a copy constructor that constructs a ValidationMetadata object from
    * an @Validate annotation declared on a property.
    *
    * @param validate
    */
   public ValidationMetadata( String property, Validate validate ) {
      // Copy over all the simple values
      _property = property;
      encrypted(validate.encrypted());
      required(validate.required());
      trim(validate.trim());
      ignore(validate.ignore());
      if ( validate.minlength() != -1 ) {
         minlength(validate.minlength());
      }
      if ( validate.maxlength() != -1 ) {
         maxlength(validate.maxlength());
      }
      if ( validate.minvalue() != Double.MIN_VALUE ) {
         minvalue(validate.minvalue());
      }
      if ( validate.maxvalue() != Double.MAX_VALUE ) {
         maxvalue(validate.maxvalue());
      }
      if ( !"".equals(validate.mask()) ) {
         mask(validate.mask());
      }
      if ( validate.converter() != TypeConverter.class ) {
         converter(validate.converter());
      }
      if ( !"".equals(validate.expression()) ) {
         expression(validate.expression());
      }
      if ( validate.on().length > 0 ) {
         on(validate.on());
      }
      if ( !"".equals(validate.label()) ) {
         _label = validate.label();
      }
   }

   /** Sets the overridden TypeConveter to use to convert values. */
   @SuppressWarnings("rawtypes")
   public ValidationMetadata converter( Class<? extends TypeConverter> converter ) {
       _converter = converter;
      return this;
   }

   /** Returns the overridden TypeConverter if there is one, or null. */
   @SuppressWarnings("rawtypes")
   public Class<? extends TypeConverter> converter() { return _converter; }

   /** Sets the encrypted flag for this field. True = encrypted, false = plain text. */
   public ValidationMetadata encrypted( boolean encrypted ) {
      _encrypted = encrypted;
      return this;
   }

   /** Returns true if the field in question is encrypted. */
   public boolean encrypted() { return _encrypted; }

   /** Sets the expression that should be used to validate values. */
   public ValidationMetadata expression( String expression ) {
       _expression = expression;
      if ( !_expression.startsWith("${") ) {
          _expression = "${" + _expression + "}";
      }
      return this;
   }

   /** Returns the overridden TypeConverter if there is one, or null. */
   public String expression() { return _expression; }

   /** Returns the name of the property this validation metadata represents. */
   public String getProperty() {
      return _property;
   }

   /** Sets whether or not this field should be ignored during binding and validation. */
   public ValidationMetadata ignore( boolean ignore ) {
      _ignore = ignore;
      return this;
   }

   /** Returns true if this field should be ignored in binding and validation. */
   public boolean ignore() { return _ignore; }

   /** Set the field label. */
   public void label( String label ) { _label = label;}

   /** Get the field label. */
   public String label() { return _label; }

   /** Sets the mask which the String form of the property must match. */
   public ValidationMetadata mask( String mask ) {
       _mask = Pattern.compile(mask);
      return this;
   }

   /** Returns the mask Pattern property values must match, or null if there is none. */
   public Pattern mask() { return _mask; }

   /** Sets the maximum acceptable length for property values. */
   public ValidationMetadata maxlength( Integer maxlength ) {
      _maxlength = maxlength;
      return this;
   }

   /** Returns the maximum acceptable length for values, or null if there is none. */
   public Integer maxlength() { return _maxlength; }

   /** Sets the maximum acceptable <b>value</b> for numeric property values. */
   public ValidationMetadata maxvalue( Double maxvalue ) {
      _maxvalue = maxvalue;
      return this;
   }

   /** Returns the maximum acceptable value for numeric properties, or null if there is none. */
   public Double maxvalue() { return _maxvalue; }

   /** Sets the minimum acceptable length for property values. */
   public ValidationMetadata minlength( Integer minlength ) {
      _minlength = minlength;
      return this;
   }

   /** Returns the minimum acceptable length for values, or null if there is none. */
   public Integer minlength() { return _minlength; }

   /** Sets the minimum acceptable <b>value</b> for numeric property values. */
   public ValidationMetadata minvalue( Double minvalue ) {
      _minvalue = minvalue;
      return this;
   }

   /** Returns the minimum acceptable value for numeric properties, or null if there is none. */
   public Double minvalue() { return _minvalue; }

   /** Sets the set of events for which the field in question is required, if it is at all. */
   public ValidationMetadata on( String... on ) {
      if ( on.length == 0 ) {
         _on = null;
      } else {
         // Check for empty strings in the "on" element
         for ( String s : on ) {
            if ( s.length() == 0 || "!".equals(s) ) {
               throw new IllegalArgumentException("@Validate's \"on\" element must not contain empty strings");
            }
         }

         _on = new HashSet<>();
         _onIsPositive = !(on[0].charAt(0) == '!');
         for ( String s : on ) {
            if ( _onIsPositive ) {
               _on.add(s);
            } else {
               _on.add(s.substring(1));
            }
         }
      }

      return this;
   }

   /** Returns the set of events for which the field in question is required. May return null. */
   public Set<String> on() { return _on; }

   /** Sets the required-ness of this field. True = required, false = not required. */
   public ValidationMetadata required( boolean required ) {
      _required = required;
      return this;
   }

   /** Returns true if the field in question is required. */
   public boolean required() { return _required; }

   /** Returns true if the field is required when processing the specified event. */
   public boolean requiredOn( String event ) {
      return _required && !_ignore && ((_on == null) || (_onIsPositive && _on.contains(event)) || (!_onIsPositive && !_on.contains(event)));
   }

   /**
    * Overidden toString() that only outputs the constraints that are specified by
    * the instance of validation metadata (i.e. omits nulls, defaults etc.)
    *
    * @return a human readable string form of the metadata
    */
   @Override
   public String toString() {
      return "ValidationMetadata{" + (_required ? "required=" + _required : "") + (_encrypted ? "encrypted=" + _encrypted : "") + (_ignore ?
            ", ignore=" + _ignore :
            "") + (_minlength != null ? ", minlength=" + _minlength : "") + (_maxlength != null ? ", maxlength=" + _maxlength : "") + (_minvalue != null ?
            ", minvalue=" + _minvalue :
            "") + (_maxvalue != null ? ", maxvalue=" + _maxvalue : "") + (_mask != null ? ", mask=" + _mask : "") + (_expression != null ?
            ", expression='" + _expression + '\'' :
            "") + (_converter != null ? ", converter=" + _converter.getSimpleName() : "") + (_label != null ? ", label='" + _label + '\'' : "") + '}';
   }

   /** Sets the trim flag of this field. True = trim, false = don't trim. */
   public ValidationMetadata trim( boolean trim ) {
      _trim = trim;
      return this;
   }

   /** Returns true if the field should be trimmed before validation or type conversion. */
   public boolean trim() { return _trim; }
}

