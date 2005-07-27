package net.sourceforge.stripes.action;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * Marker annotation to specify that the event handled by the annotated method should not have
 * validation run on it before the handler is invoked.  In this case type conversion will still
 * occur, but all other forms of validation (including ActionBean.validate() if it exists) will
 * be by-passed.
 *
 * @author Tim Fennell
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface DontValidate {
}
