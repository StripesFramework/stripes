package net.sourceforge.stripes.action;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * Annotation used to give ActionBean classes a short name or synonym.  The short name can be used
 * in JSPs to identify the form, and is used by the AnnotatedClassActionResolver in order to locate the
 * right ActionBean to instantiate in order to handle a request.
 *
 * @author Tim Fennell
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface FormName {
    /** The name of the form to which the ActionBean will be bound. */
    String value();
}
