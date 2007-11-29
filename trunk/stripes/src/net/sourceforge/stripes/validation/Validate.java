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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Documented;

/**
 * Primary annotation used to specify validations for form fields.  Allows quick and easy
 * specifiction of the most common types of validation logic, as well as a way to specify
 * custom validations.
 *
 * @author Tim Fennell
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface Validate {
    /**
     * The name of the field to validate. This attribute is ignored when the validation is
     * attached to a simple (one form field to one ActionBean field) property, and is used only
     * when specifying nested validations.
     */
    String field() default "";

    /**
     * If set to true, requires that a non-null, non-empty value must be submitted for the field.
     */
    boolean required() default false;

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
     * If set to true will cause the property to be ignore by binding and validation even if it
     * was somehow submitted in the request.
     *
     * @since Stripes 1.1
     */
    boolean ignore() default false;

    /**
     * Specifies a minimum length of characters that must be submitted. This validation is performed
     * on the String value before any other validations or conversions are made.
     */
    int minlength() default -1;

    /**
     * Specifies a maximum length of characters that must be submitted. This validation is performed
     * on the String value before any other validations or conversions are made.
     */
    int maxlength() default -1;

    /**
     * Specifies the minimum numeric value acceptable for a numeric field. This validation is
     * performed after the field has been converted to it's java type. This validation is only
     * valid on numeric types (including BigInteger and BigDecimal).
     */
    double minvalue() default Double.MIN_VALUE;

    /**
     * Specifies the maximum numeric value acceptable for a numeric field. This validation is
     * performed after the field has been converted to it's java type. This validation is only
     * valid on numeric types (including BigInteger and BigDecimal).
     */
    double maxvalue() default Double.MAX_VALUE;

    /**
     * Specifies a regular expression mask to be used to check the format of the String value
     * submitted. The mask will be compiled into a java.util.regex.Pattern for use.
     */
    String mask() default "";

    /**
     * <p>Specifies an expression in the JSP expression language that should be evaluated
     * to check the validity of this field.  In the case of lists, arrays and maps the expression
     * is evaluated once for each value supplied.  The expression is evaluated <i>only if
     * a value is supplied</i> - it will not be evaluated if the user did not supply a value for the
     * field.</p>
     *
     * <p>The value being validated is available in the EL variable 'this'.  Properties of the
     * ActionBean (including the context) can be referenced directly, as can values in request
     * and session scope if necessary.</p>
     *
     * <p>Note: it is not necessary to encapsulate the expression in ${} as on a JSP page.</p>
     */
    String expression() default "";

    /**
     * A type converter to use to convert this field from String to it's rich object type. If none
     * is specified (which should be very common) then the default converter for the target type of
     * object will be used.
     */
    @SuppressWarnings("unchecked")
	Class<? extends TypeConverter> converter() default TypeConverter.class;

}
