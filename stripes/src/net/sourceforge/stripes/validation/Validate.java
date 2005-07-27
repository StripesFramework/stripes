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
