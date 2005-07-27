package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.util.OgnlUtil;
import net.sourceforge.stripes.validation.ScopedLocalizableError;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.TypeConverterFactory;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Implementation of the ActionBeanPropertyBinder interface that uses the OGNL toolkit to perform
 * the JavaBean property binding.  Uses a pair of helper classes (OgnlUtil and OgnlCustomNullHandler)
 * in order to efficiently manage the fetching and setting of simple, nested, indexed, mapped and
 * other properties (see Ognl documentation for full syntax and capabilities).  When setting
 * nested properties, if intermediary objects are null, they will be instantiated and linked in to
 * the object graph to allow the setting of the target property.
 *
 * @see net.sourceforge.stripes.util.OgnlCustomNullHandler
 * @see OgnlUtil
 * @author Tim Fennell
 */
public class OgnlActionBeanPropertyBinder implements ActionBeanPropertyBinder {
    private static Log log = LogFactory.getLog(OgnlActionBeanPropertyBinder.class);
    private static Set<String> SPECIAL_KEYS = new HashSet<String>();

    static {
        SPECIAL_KEYS.add(StripesConstants.URL_KEY_FORM_NAME);
        SPECIAL_KEYS.add(StripesConstants.URL_KEY_SOURCE_PAGE);
    }

    /** Map of validation annotations that is built at startup. */
    private Map<Class<ActionBean>, Map<String,Validate>> validations;


    /**
     * Looks up and caches in a useful form the metadata necessary to perform validations as
     * properties are bound to the bean.
     */
    public void init() throws Exception {
        Set<Class<ActionBean>> beanClasses = ActionClassCache.getInstance().getActionBeanClasses();
        this.validations = new HashMap<Class<ActionBean>, Map<String,Validate>>();

        for (Class<ActionBean> beanClass : beanClasses) {
            Map<String, Validate> fieldValidations = new HashMap<String, Validate>();

            // Process the methods on the class
            Method[] methods = beanClass.getMethods();
            for (Method method : methods) {
                String fieldName = getPropertyName(method.getName());
                Validate validation = method.getAnnotation(Validate.class);
                if (validation != null) {
                    fieldValidations.put(fieldName, validation);
                }

                ValidateNestedProperties nested = method.getAnnotation(ValidateNestedProperties.class);
                if (nested != null) {
                    Validate[] validations = nested.value();
                    for (Validate nestedValidate : validations) {
                        if ( "".equals(nestedValidate.field()) ) {
                            log.warn("Nested validation used without field name: " + validation);
                        }
                        else {
                            fieldValidations.put(fieldName + "." + nestedValidate.field(),
                                                 nestedValidate);
                        }
                    }
                }
            }


            // Process the fields for validation annotations
            Field[] fields = beanClass.getFields();
            for (Field field : fields) {
                Validate validation = field.getAnnotation(Validate.class);
                if (validation != null) {
                    fieldValidations.put(field.getName(), validation);
                }

                ValidateNestedProperties nested = field.getAnnotation(ValidateNestedProperties.class);
                if (nested != null) {
                    Validate[] validations = nested.value();
                    for (Validate nestedValidate : validations) {
                        if ( "".equals(nestedValidate.field()) ) {
                            log.warn("Nested validation used without field name: " + validation);
                        }
                        else {
                            fieldValidations.put(field.getName() + "." + nestedValidate.field(),
                                                 nestedValidate);
                        }
                    }
                }
            }

            this.validations.put(beanClass, fieldValidations);
            log.info("Loaded validations for ActionBean " + beanClass.getName() + ":" +
                     fieldValidations);
        }
    }

    /**
     * <p>Loops through the parameters contained in the request and attempts to bind each one to the
     * supplied ActionBean.  Invokes validation for each of the properties on the bean before
     * binding is attempted.  Only fields which do not produce validation errors will be bound
     * to the ActionBean.</p>
     *
     * <p>Individual property binding is delegated to the other interface method,
     * bind(ActionBean, String, Object), in order to allow for easy extension of this class.</p>
     *
     * @param bean the ActionBean whose properties are to be validated and bound
     * @param context the ActionBeanContext of the current request
     */
    public ValidationErrors bind(ActionBean bean, ActionBeanContext context) {
        Map<String,String[]> parameters = context.getRequest().getParameterMap();
        ValidationErrors fieldErrors = new ValidationErrors();

        // First we bind all the regular parameters
        for (Map.Entry<String,String[]> entry : parameters.entrySet() ) {
            try {
                String parameter = entry.getKey();
                if (!SPECIAL_KEYS.contains(parameter) && !context.getEventName().equals(parameter)) {
                    log.trace("Attempting to bind property with name: " + parameter);

                    Class type = (Class) OgnlUtil.getValue(parameter + ".class", bean);
                    String[] values = entry.getValue();

                    List<ValidationError> errors = new ArrayList<ValidationError>();
                    List<Object> convertedValues = validate(bean, parameter, type, values, errors);

                    if (errors.size() > 0) {
                        fieldErrors.put(parameter, errors);
                    }
                    else if (convertedValues.size() > 0) {
                        // If the target type is an array, set it as one, otherwise set as scalar
                        if (type.isArray()) {
                            bind(bean, parameter, convertedValues.toArray());
                        }
                        else if (type.isAssignableFrom(Collection.class)) {
                            Collection collection = (Collection) type.newInstance();
                            collection.addAll(convertedValues);
                            bind(bean, parameter, collection);
                        }
                        else {
                            bind(bean, parameter, convertedValues.get(0));
                        }
                    }
                }
            }
            catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not bind property with name [" + entry.getKey() + "] and " +
                        "values " + Arrays.toString(entry.getValue()) + " to bean of type: "
                        + bean.getClass().getName(), e);
                }
            }
        }

        // Then we figure out if any files were uploaded and bind those too
        StripesRequestWrapper request = (StripesRequestWrapper) context.getRequest();
        if ( request.isMultipart() ) {
            Enumeration<String> fileParameterNames = request.getFileParameterNames();

            while (fileParameterNames.hasMoreElements()) {
                String fileParameterName = fileParameterNames.nextElement();
                FileBean fileBean = request.getFileParameterValue(fileParameterName);
                log.trace("Attempting to bind file parameter with name [" + fileParameterName +
                          "] and value: " + fileBean);

                if (fileBean != null) {
                    try {
                        bind(bean, fileParameterName, fileBean);
                    }
                    catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Could not bind file property with name [" + fileParameterName
                                + "] and value: " + fileBean, e);
                        }
                    }
                }
            }
        }

        return fieldErrors;
    }

    /**
     * Uses Ognl to attempt the setting of the named property on the target bean.  If the binding
     * fails for any reason (property does not exist, type conversion not possible etc.) an
     * exception will be thrown.
     *
     * @param bean the ActionBean on to which the property is to be bound
     * @param propertyName the name of the property to be bound (simple or complex)
     * @param propertyValue the value of the target property
     * @throws Exception thrown if the property cannot be bound for any reason
     */
    public void bind(ActionBean bean, String propertyName, Object propertyValue) throws Exception {
        OgnlUtil.setValue(propertyName, bean, propertyValue);
    }

    /**
     * Helper method that returns the name of the property when supplied with the corresponding
     * get or set method.  Does not do anything particularly intelligent, just drops the first
     * three characters and makes the next character lower case.
     */
    protected String getPropertyName(String methodName) {
        return methodName.substring(3,4).toLowerCase() + methodName.substring(4);
    }

    /**
     *
     * @param bean
     * @param propertyName
     * @param propertyType
     * @param values
     * @param errors
     * @return Object[]
     */
    public List<Object> validate(ActionBean bean,
                                String propertyName,
                                Class propertyType,
                                String[] values,
                                List<ValidationError> errors) throws Exception {

        Validate validate = this.validations.get(bean.getClass()).get(propertyName);
        log.debug("Validating attribute [" + propertyName + "], annotation is: " + validate);

        // If the target type is an array, try to figure out what the underlying type is and use
        // that for conversions etc.  If the target type is a collection type, for now, we rely on
        // the developer specifying a converter in the @Validate annotation.  If no converter is
        // specified, String is used as the property type, and this may lead to exceptions if the
        // collection is a generic collection with a type variable other than String.
        if (propertyType.isArray()) {
            propertyType = propertyType.getComponentType();
        }
        else if (propertyType.isAssignableFrom(Collection.class)) {
            if (validate != null && validate.converter() != TypeConverter.class) {
                TypeVariable[] types = validate.converter().getTypeParameters();
                propertyType = (Class) types[0].getBounds()[0];
            } else {
                propertyType = String.class;
            }
        }

        if (validate != null) {
            // Execute validations on the string form
            for (String value : values) {
                doPreConversionValidations(propertyName, value, validate, errors);
            }

            // Then convert the property using a configured or default converter
            if (validate.converter() != TypeConverter.class) {
                TypeConverter converter = TypeConverterFactory.getInstance(validate.converter());
                return convert(propertyName, values, converter, propertyType, errors);
            }
            else {
                TypeConverter converter = TypeConverterFactory.getTypeConverter(propertyType);
                return convert(propertyName, values, converter, propertyType, errors);
            }

            // Then execute post conversion validations
            // TODO: post conversion validations
        }
        else {
            // If there was no validation annotation, try using a default converter
            TypeConverter converter = TypeConverterFactory.getTypeConverter(propertyType);
            return convert(propertyName, values, converter, propertyType, errors);
        }
    }

    /**
     * Performs several basic validations on the String value supplied in the HttpServletRequest,
     * based on information provided in annotations on the ActionBean.
     *
     * @param propertyName the name of the property being validated (used for constructing errors)
     * @param value the value being validated
     * @param validate the Valiate annotation that was decorating the property being validated
     * @param errors a collection of errors to be populated with any validation errors discovered
     */
    protected void doPreConversionValidations(String propertyName,
                                              String value,
                                              Validate validate,
                                              List<ValidationError> errors) {

        if (validate.required() && value.length() == 0) {
            ValidationError error = new ScopedLocalizableError("validation.required",
                                                               "valueNotPresent");
            error.setFieldName(propertyName);
            error.setFieldValue(value);
            errors.add( error );
        }

        if (validate.minlength() != -1 && value.length() < validate.minlength()) {
            ValidationError error = new ScopedLocalizableError("validation.minlength",
                                                               "valueTooShort",
                                                               validate.minlength());
            error.setFieldName(propertyName);
            error.setFieldValue(value);
            errors.add( error );
        }

        if (validate.maxlength() != -1 && value.length() > validate.maxlength()) {
            ValidationError error = new ScopedLocalizableError("validation.maxlength",
                                                               "valueTooLong",
                                                               validate.maxlength());
            error.setFieldName(propertyName);
            error.setFieldValue(value);
            errors.add( error );
        }

        if (validate.mask().length() > 0 && !Pattern.compile(validate.mask()).matcher(value).matches() ) {
            ValidationError error = new ScopedLocalizableError("validation.mask",
                                                               "valueDoesNotMatch");
            error.setFieldName(propertyName);
            error.setFieldValue(value);
            errors.add( error );
        }
    }

    /**
     * <p>Converts the String[] of values for a given parameter in the HttpServletRequest into the
     * desired type of Object.  If a converter is decalred using an annotation for the property
     * (or getter/setter) then that converter will be used - if it does not convert to the right
     * type an exception will be logged and values will not be converted. If no Converter was
     * specified then a default converter will be looked up based on the target type of the
     * property.  If there is no default converter, then a Constructor will be looked for on the
     * target type which takes a single String parameter.  If such a Constructor exists it will be
     * invoked.</p>
     *
     * <p>Only parameter values that are non-null and do not equal the empty String will be converted
     * and returned. So an input array with one entry equalling the empty string, [""],  will result
     * in an <b>empty</b> List being returned.  Similarly, if a length three array is passed in
     * with one item equalling the empty String, a List of length two will be returned.</p>
     *
     * @param values a String array of values to attempt conversion of
     * @param converter the TypeConverter instance to use for the conversions, or null if no
     *        applicable TypeConverter is available.
     * @param errors a List into which ValidationError objects will be populated for any errors
     *        discovered during conversion.
     * @return List<Object> a List of objects containing only objects of the desired type. It is
     *         not guaranteed to be the same length as the values array passed in.
     */
    private List<Object> convert(String propertyName,
                                String[] values,
                                TypeConverter converter,
                                Class propertyType,
                                List<ValidationError> errors) {

        List<Object> returns = new ArrayList<Object>();

        log.debug("Converting values " + values + " using converter " + converter);

        for (int i=0; i<values.length; ++i) {
            if (!"".equals(values[i])) {
                try {
                    Object retval = null;
                    if (converter != null) {
                        retval = converter.convert(values[i], propertyType, errors);
                    }
                    else {
                        Constructor constructor = propertyType.getConstructor(String.class);
                        if (constructor != null) {
                            retval = constructor.newInstance(values[i]);
                        }
                    }

                    // If we managed to get a non-null converted value, add it to the return set
                    if (retval != null) {
                        returns.add(retval);
                    }

                    // Set the field name and value on the error
                    for (ValidationError error : errors) {
                        error.setFieldName(propertyName);
                        error.setFieldValue(values[i]);
                    }
                }
                catch(Exception e) {
                    //TODO: figure out what to do, if anything, with these exceptions
                    log.warn("Looks like type converter " + converter + " threw an exception.", e);
                }
            }
        }

        return returns;
    }
}
