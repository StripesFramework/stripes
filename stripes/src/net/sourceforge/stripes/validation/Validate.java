/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
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
     * A type converter to use to convert this field from String to it's rich object type. If none
     * is specified (which should be very common) then the default converter for the target type of
     * object will be used.
     */
    Class<? extends TypeConverter> converter() default TypeConverter.class;

}
