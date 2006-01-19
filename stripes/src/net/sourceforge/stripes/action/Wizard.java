package net.sourceforge.stripes.action;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;

/**
 * <p>Annotation that marks an ActionBean as representing a wizard user interface (i.e. one logical
 * form or operation spread across several pages/request cycles). ActionBeans that are marked
 * as Wizards are treated differently in the following ways:</p>
 *
 * <ul>
 *   <li>Data from previous request cycles is maintained automatically through hidden fields</li>
 *   <li>Required field validation is performed only on those fields present on the page</li>
 * </ul>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.3
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Wizard {
}
