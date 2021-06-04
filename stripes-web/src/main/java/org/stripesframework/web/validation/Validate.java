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
package org.stripesframework.web.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.stripesframework.web.localization.LocalizationUtility;


/**
 * Primary annotation used to specify validations for form fields.  Allows quick and easy
 * specification of the most common types of validation logic, as well as a way to specify
 * custom validations.
 *
 * @author Tim Fennell
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Documented
public @interface Validate {

   /**
    * A type converter to use to convert this field from String to it's rich object type. If none
    * is specified (which should be very common) then the default converter for the target type of
    * object will be used.
    */
   @SuppressWarnings("unchecked") Class<? extends TypeConverter> converter() default TypeConverter.class;

   /**
    * If true, then a parameter value to be bound to this field must be an encrypted string. It
    * also implies that when the value of this field is rendered by certain tags (e.g.,
    * {@link InputHiddenTag}) that it is to be rendered as an encrypted string. This prevents
    * clients from injecting random values. Encryption is disabled in debug mode.
    */
   boolean encrypted() default false;

   /**
    * The name of the field to validate. This attribute is ignored when the validation is
    * attached to a simple (one form field to one ActionBean field) property, and is used only
    * when specifying nested validations.
    */
   String field() default "";

   /**
    * If set to true will cause the property to be ignore by binding and validation even if it
    * was somehow submitted in the request.
    *
    * @since Stripes 1.1
    */
   boolean ignore() default false;

   /**
    * The natural language name to use for the field when reporting validation errors, generating
    * form input labels, etc. This will only be used if a localized field name cannot be found in
    * the resource bundle.
    *
    * @see LocalizationUtility#getLocalizedFieldName(String, String, Class, java.util.Locale)
    */
   String label() default "";

   /**
    * Specifies a regular expression mask to be used to check the format of the String value
    * submitted. The mask will be compiled into a java.util.regex.Pattern for use.
    */
   String mask() default "";

   /**
    * Specifies a maximum length of characters that must be submitted. This validation is performed
    * on the String value before any other validations or conversions are made.
    */
   int maxlength() default -1;

   /**
    * Specifies the maximum numeric value acceptable for a numeric field. This validation is
    * performed after the field has been converted to it's java type. This validation is only
    * valid on numeric types (including BigInteger and BigDecimal).
    */
   double maxvalue() default Double.MAX_VALUE;

   /**
    * Specifies a minimum length of characters that must be submitted. This validation is performed
    * on the String value before any other validations or conversions are made.
    */
   int minlength() default -1;

   /**
    * Specifies the minimum numeric value acceptable for a numeric field. This validation is
    * performed after the field has been converted to it's java type. This validation is only
    * valid on numeric types (including BigInteger and BigDecimal).
    */
   double minvalue() default Double.MIN_VALUE;

   /**
    * <p>If required=true, restricts the set of events to which the required check is applied.
    * If required=false (or omitted) this setting has <i>no effect</i>. This setting is entirely
    * optional and if omitted then the field will simply be required for all events.</p>
    *
    * <p>Can be specified as either a positive list (e.g. on={"save, "update"}) in which case the
    * required check will be performed only on the listed events.  Can also be specified as an
    * inverted list (e.g. on="!new") in which case the check will be performed on all events that
    * are not listed.  Cannot contain a mix of "!event" and "event" since it does not make sense!
    * </p>
    */
   String[] on() default {};

   /**
    * If set to true, requires that a non-null, non-empty value must be submitted for the field.
    */
   boolean required() default false;

   /**
    * Trim white space from the beginning and end of request parameter values before attempting
    * validation, type conversion or binding.
    *
    * @see String#trim()
    */
   boolean trim() default true;

}
