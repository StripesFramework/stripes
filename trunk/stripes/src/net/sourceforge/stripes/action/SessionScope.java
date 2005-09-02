package net.sourceforge.stripes.action;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Documented;

/**
 * <p>Annotation that is used to specify that an ActionBean should be instantiated and stored across
 * requests in the Session scope.  By default ActionBeans are instantiated per-request, populated,
 * used and then discarded at the end of the request cycle.  Using this annotation causes an
 * ActionBean to live for multiple request cycles.  It will be instantiated and put into session
 * on the first request that references the ActionBean.  A reference to the bean will also be
 * placed into RequestScope for each request that references the bean, thereby allowing the rest
 * of Stripes to treat it like any other ActionBean.</p>
 *
 * <p>Since session scope ActionBeans are not generally encouraged by the author, very few
 * allowances will be made in Stripes to accomodate session scope beans.  This means that
 * additional mechanisms to handle session scope beans do not exist.  However, there are
 * general mechanisms built in to Stripes that will allow you to overcome most if not all issues
 * that arise from Session scoping ActionBeans.</p>
 *
 * <p>One major issue is how to clear out values from an ActionBean before the next request cycle.
 * It is suggested that this be done in the ActionBean.setContext() method, which is guaranteed to
 * be invoked before any binding occurs.  Note that this problem is two-fold.  Firstly the browser
 * does not submit values for checkboxes that are de-selected.  Secondly Stripes does not invoke
 * setters for parameters submitted in the request with values equal to the empty-string. You may
 * choose to simply null out such fields in setContext() or use the available reference to the
 * HttpServletRequest to find out if empty values were submitted for fields, and null out just
 * those fields.</p>
 *
 * <p>A second major issue is in using the validation service.  The validation service validates
 * <em>what was submitted in the request</em>.  Therefore if a property is marked are required,
 * is present in the session scope bean, but is not submitted by the user, it will generate a
 * required field error.  This may or may not be desired behaviour.  If it is not, it is suggested
 * that the ActionBean implement the ValidationErrorHandler interface to find out about the
 * validation errors generated, and take action accordingly.</p>
 *
 * <p>Lastly, an alternative to session scoping for wizard pattern/page-spanning forms that
 * ActionBean authors may wish to consider is the use of the
 * {@link net.sourceforge.stripes.tag.WizardFieldsTag} which will carry all the fields submitted
 * in the request into the next request by writing hidden form fields.</p>
 *
 * @see net.sourceforge.stripes.validation.ValidationErrorHandler
 * @author Tim Fennell
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface SessionScope {
}
