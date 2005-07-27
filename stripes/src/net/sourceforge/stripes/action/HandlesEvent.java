package net.sourceforge.stripes.action;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * Annotation used by ActionBean to declare that a method is capable of handling a named event
 * being submitted by a client.  Used by the AnnotatedClassActionResolver to map requests to the
 * appropriate method to handle them at run time.
 *
 * @author Tim Fennell
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface HandlesEvent {
    /** The name of the event that will be handled by the annotated method. */
    String value();
}
