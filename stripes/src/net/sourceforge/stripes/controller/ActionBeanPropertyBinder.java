package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.validation.ValidationErrors;

/**
 * <p>Interface for class(es) responsible for taking the String/String[] properties contained in the
 * HttpServletRequest and:
 * <ul>
 *     <li>Converting them to the rich type of the property on the target JavaBean</li>
 *     <li>Setting the properties on the JavaBean using the appropriate mechanism</li>
 * </ul>
 * </p>
 *
 * <p>Implementations may also perform validations of the fields during binding.  If validation
 * errors occur then the collection of ValidationErrors contained within the ActionBeanContext
 * should be populated before returning.</p>
 *
 * @author Tim Fennell
 */
public interface ActionBeanPropertyBinder {
    /**
     * Perform initialization actions specific to the class/instance.  This will be called once
     * before the property binder is put into use.
     */
    void init() throws Exception;

    /**
     * Populates all the properties in the request which have a matching property in the target
     * bean.  If additional properties exist in the request which are not present in the bean a
     * message should be logged, but binding should continue without throwing any errors.
     *
     * @param bean the ActionBean to bind properties to
     * @param context the ActionBeanContext containing the current request
     * @param validate true indicates that validation should be run, false indicates that only
     *        type conversion should occur
     */
    ValidationErrors bind(ActionBean bean, ActionBeanContext context, boolean validate);

    /**
     * Bind an individual property with the name specified to the bean supplied.
     *
     * @param bean the ActionBean to bind the property to
     * @param propertyName the name (including nested, indexed and mapped property names) of the
     *        property being bound
     * @param propertyValue the value to be bound to the property on the bean
     * @throws Exception thrown if the property cannot be bound for any reason
     */
    void bind(ActionBean bean, String propertyName, Object propertyValue) throws Exception;
}
