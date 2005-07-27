package net.sourceforge.stripes.action;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * Marker annotation to specify that a method within an ActionBean is the default handler for
 * events if a specific event cannot be identified.
 *
 * @author Tim Fennell
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface DefaultHandler {

}