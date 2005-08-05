package net.sourceforge.stripes.action;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * Annotation used to bind ActionBean classes to a specific path within the web appliction.
 * The AnnotatedClassActionResolver will examine the URL submitted and extract the section
 * that is relative to the web-app root.  That will be compared with the URL specified in
 * the UrlBinding annotation, to find the ActionBean that should process the chosen request.
 *
 * @author Tim Fennell
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface UrlBinding {
    /** The web-app relative URL that the ActionBean will respond to. */
    String value();
}
