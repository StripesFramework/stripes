package net.sourceforge.stripes.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Documented;

/**
 * Annotation used to capture the validation needs of nested properties within an ActionBean. It
 * contains a simple array of the Validate annotations.  Each Validate annotation must have its
 * field property set to the name of the field within the annotated property that is to be validated.
 *
 * @author Tim Fennell
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface ValidateNestedProperties {
    Validate[] value();
}
