/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.util.HtmlUtil;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.OgnlUtil;
import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.validation.ScopedLocalizableError;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrors;
import ognl.NoSuchPropertyException;
import ognl.OgnlException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private static Log log = Log.getInstance(OgnlActionBeanPropertyBinder.class);
    private static Set<String> SPECIAL_KEYS = new HashSet<String>();

    static {
        SPECIAL_KEYS.add(StripesConstants.URL_KEY_SOURCE_PAGE);
        SPECIAL_KEYS.add(StripesConstants.URL_KEY_FIELDS_PRESENT);
    }

    /** Map of validation annotations that is built at startup. */
    private Map<Class<ActionBean>, Map<String,Validate>> validations;

    /** Configuration instance passed in at initialization time. */
    private Configuration configuration;


    /**
     * Looks up and caches in a useful form the metadata necessary to perform validations as
     * properties are bound to the bean.
     */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
        Set<Class<ActionBean>> beanClasses = ActionClassCache.getInstance().getActionBeanClasses();
        this.validations = new HashMap<Class<ActionBean>, Map<String,Validate>>();

        for (Class<ActionBean> beanClass : beanClasses) {
            Map<String, Validate> fieldValidations = new HashMap<String, Validate>();

            // Process the methods on the class
            Method[] methods = beanClass.getMethods();
            for (Method method : methods) {
                Validate validation = method.getAnnotation(Validate.class);
                if (validation != null) {
                    String fieldName = getPropertyName(method.getName());
                    fieldValidations.put(fieldName, validation);
                }

                ValidateNestedProperties nested = method.getAnnotation(ValidateNestedProperties.class);
                if (nested != null) {
                    String fieldName = getPropertyName(method.getName());
                    Validate[] validations = nested.value();
                    for (Validate nestedValidate : validations) {
                        if ( "".equals(nestedValidate.field()) ) {
                            log.warn("Nested validation used without field name: ", validation);
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
                            log.warn("Nested validation used without field name: ", validation);
                        }
                        else {
                            fieldValidations.put(field.getName() + "." + nestedValidate.field(),
                                                 nestedValidate);
                        }
                    }
                }
            }

            this.validations.put(beanClass, fieldValidations);
            log.info("Loaded validations for ActionBean ", beanClass.getName(), ":",
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
     * @param validate true indicates that validation should be run, false indicates that only
     *        type conversion should occur
     */
    public ValidationErrors bind(ActionBean bean, ActionBeanContext context, boolean validate) {
        ValidationErrors fieldErrors = new ValidationErrors();

        // Take the ParameterMap and turn the keys into ParameterNames
        Map<ParameterName,String[]> parameters = getParameters(context);

        // Run the required validation first to catch fields that weren't even submitted
        if (validate) {
            validateRequiredFields(parameters, context, bean.getClass(), fieldErrors);
        }

        // First we bind all the regular parameters
        for (Map.Entry<ParameterName,String[]> entry : parameters.entrySet() ) {
            try {
                ParameterName name = entry.getKey();
                if (!SPECIAL_KEYS.contains(name.getName()) && !name.getName().equals(context.getEventName())
                        && !fieldErrors.containsKey(name.getName()) ) {
                    log.trace("Running binding for property with name: ", name);

                    Class type = OgnlUtil.getPropertyClass(name.getName(), bean);
                    String[] values = entry.getValue();

                    // Do Validation and type conversion
                    List<ValidationError> errors = new ArrayList<ValidationError>();
                    Validate validationInfo =
                            this.validations.get(bean.getClass()).get(name.getStrippedName());

                    // If the property should be ignored, skip to the next property
                    if (validationInfo != null && validationInfo.ignore()) {
                        continue;
                    }

                    if (validate && validationInfo != null) {
                        doPreConversionValidations(name, values, validationInfo, errors);
                    }

                    List<Object> convertedValues =
                        convert(bean, name, values, type, validationInfo, errors);

                    if (validate && validationInfo != null) {
                        doPostConversionValidations(name, convertedValues, validationInfo, errors);
                    }

                    // If we have errors, save them, otherwise bind the parameter to the form
                    if (errors.size() > 0) {
                        fieldErrors.addAll(name.getName(), errors);
                    }
                    else if (convertedValues.size() > 0) {
                        bindNonNullValue(bean, name.getName(), convertedValues, type);
                    }
                    else {
                        bindNullValue(bean, name.getName(), type);
                    }
                }
            }
            catch (NoSuchPropertyException nspe) {
                log.debug("Could not bind property with name [", entry.getKey(),
                          "] to bean of type: ", bean.getClass().getName(), " : ",
                          nspe.getMessage());
            }
            catch (Exception e) {
                log.debug(e, "Could not bind property with name [", entry.getKey(), "] and values ",
                          Arrays.toString(entry.getValue()), " to bean of type: ",
                          bean.getClass().getName());
            }
        }

        bindMissingValuesAsNull(bean, context);

        // Then we figure out if any files were uploaded and bind those too
        StripesRequestWrapper request = StripesRequestWrapper.findStripesWrapper(context.getRequest());
        if ( request.isMultipart() ) {
            Enumeration<String> fileParameterNames = request.getFileParameterNames();

            while (fileParameterNames.hasMoreElements()) {
                String fileParameterName = fileParameterNames.nextElement();
                FileBean fileBean = request.getFileParameterValue(fileParameterName);
                log.trace("Attempting to bind file parameter with name [", fileParameterName,
                          "] and value: ", fileBean);

                if (fileBean != null) {
                    try {
                        bind(bean, fileParameterName, fileBean);
                    }
                    catch (Exception e) {
                        log.debug(e, "Could not bind file property with name [", fileParameterName,
                                  "] and value: ", fileBean);
                    }
                }
            }
        }

        return fieldErrors;
    }

    /**
     * Uses a hidden field to deterine what (if any) fields were present in the form but did
     * not get submitted to the server. For each such field the value is "softly" set to null
     * on the ActionBean.  This is not uncommon for checkboxes, and also for multi-selects.
     *
     * @param bean the ActionBean being bound to
     * @param context the current ActionBeanContext
     */
    protected void bindMissingValuesAsNull(ActionBean bean, ActionBeanContext context) {
        HttpServletRequest request = context.getRequest();
        Set<String> paramatersSubmitted = request.getParameterMap().keySet();
        String fieldsPresent = request.getParameter(StripesConstants.URL_KEY_FIELDS_PRESENT);

        for (String name: HtmlUtil.splitValues(fieldsPresent)) {
            if (!paramatersSubmitted.contains(name)) {
                try {
                    bindNullValue(bean, name, OgnlUtil.getPropertyClass(name, bean));
                }
                catch (Exception e) {
                    log.warn(e, "Could not set property '", name, "' to null on ActionBean of",
                             "type '", bean.getClass(), "'.");
                }
            }
        }
    }

    /**
     * Internal helper method to bind one or more values to a single property on an
     * ActionBean. If the target type is an array of Collection, then all values are bound.
     * If the target type is a scalar type then the first value in the List of values is bound.
     *
     * @param bean the ActionBean instance to which the property is being bound
     * @param property the name of the property being bound
     * @param valueOrValues a List containing one or more values
     * @param targetType the declared type of the property on the ActionBean
     * @throws Exception if the property cannot be bound for any reason
     */
    protected void bindNonNullValue(ActionBean bean,
                                    String property,
                                    List<Object> valueOrValues,
                                    Class targetType) throws Exception {
        // If the target type is an array, set it as one, otherwise set as scalar
        if (targetType.isArray()) {
            OgnlUtil.setValue(property, bean, valueOrValues.toArray());
        }
        else if (Collection.class.isAssignableFrom(targetType)) {
            Collection collection = null;
            if (targetType.isInterface()) {
                collection = (Collection) ReflectUtil.getInterfaceInstance(targetType);
            }
            else {
                collection = (Collection) targetType.newInstance();
            }

            collection.addAll(valueOrValues);
            OgnlUtil.setValue(property, bean, collection);
        }
        else {
            OgnlUtil.setValue(property, bean, valueOrValues.get(0));
        }
    }


    /**
     * Internal helper method that determines what to do when no value was supplied for a
     * given form field (but the field was present on the page). In all cases if the property
     * is already null, or intervening objects in a nested property are null, nothing is done.
     * If the property is non-null, it will be set to null.  Unless the property is a collection,
     * in which case it will be clear()'d.
     *
     * @param bean the ActionBean to which properties are being bound
     * @param property the name of the property  being bound
     * @param type the declared type of the property on the ActionBean
     * @throws OgnlException if the value cannot be manipulated for any reason
     */
    protected void bindNullValue(ActionBean bean, String property, Class type) throws OgnlException {
        // If the class is a collection, try fetching it and setting it and clearing it
        if (Collection.class.isAssignableFrom(type)) {
            Collection collection = (Collection) OgnlUtil.getValue(property, bean);
            if (collection != null) {
                collection.clear();
            }
        }
        else {
            OgnlUtil.setNullValue(property, bean);
        }
    }


    /**
     * Converts the map of parameters in the request into a Map of ParameterName to String[].
     */
    protected Map<ParameterName, String[]> getParameters(ActionBeanContext context) {
        Map<String, String[]> requestParameters = context.getRequest().getParameterMap();
        Map<ParameterName, String[]> parameters = new HashMap<ParameterName,String[]>();

        for (Map.Entry<String,String[]> entry : requestParameters.entrySet()) {
            parameters.put(new ParameterName(entry.getKey()), entry.getValue());
        }

        return parameters;
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
     * Figures out what is the real type that TypeConversions should target for this property. For
     * a simple property this will be just the type of the property as declared in the ActionBean.
     * For Arrays the returned type will be the component type of the array.  For collections, if
     * a TypeConverter has been specified, the target type of the TypeConverter will be returned,
     * otherwise we will assume that it is a collection of Strings and hope for the best.
     *
     * @param bean the ActionBean on which the property exists
     * @param propertyType the declared type of the property
     * @param propertyName the name of the property
     *
     */
    protected Class getRealType(ActionBean bean, Class propertyType, ParameterName propertyName)
        throws Exception {

        if (propertyType.isArray()) {
            propertyType = propertyType.getComponentType();
        }
        else if (Collection.class.isAssignableFrom(propertyType)) {
            // Try to get the information from the property's return type...
            propertyType = OgnlUtil.getCollectionPropertyComponentClass(bean, propertyName.getName());

            if (propertyType == null) {
                // We couldn't figure it out from generics, so see if it was specified
                Map<String, Validate> map = this.validations.get(bean.getClass());
                Validate validationInfo = map.get(propertyName.getStrippedName());

                if (validationInfo != null && validationInfo.converter() != TypeConverter.class) {
                    Method method = validationInfo.converter().getMethod
                            ("convert", String.class, Class.class, Collection.class);
                    propertyType = method.getReturnType();
                }
                else {
                    log.warn("Unable to determine type of objects held in collection, on ",
                             "ActionBean class [", bean.getClass(), "] property [",
                             propertyName.getName(), "]. To fix this either modify the getter ",
                             "method for this property to use generics, e.g. List<Foo> get(), ",
                             "or specify the appropriate converter in the @Validate annotation ",
                             "for this property on the ActionBean. Assuming type is String, ",
                             "which may or may not work!");

                    propertyType = String.class;
                }
            }
        }

        return propertyType;
    }


    /**
     * Validates that all required fields have been submitted. This is done by looping through
     * throw the set of validation annotations and checking that each field marked as required
     * was submitted in the request and submitted with a non-empty value.
     */
    protected void validateRequiredFields(Map<ParameterName,String[]> parameters,
                                          ActionBeanContext context,
                                          Class beanClass,
                                          ValidationErrors errors) {

        log.debug("Running required field validation on bean class ", beanClass.getName());

        // Assemble a set of names that we know have indexed parameters, so we won't check
        // for required-ness the regular way
        Set<String> indexedParams = new HashSet<String>();
        for (ParameterName name : parameters.keySet()) {
            if (name.isIndexed()) {
                indexedParams.add(name.getStrippedName());
            }
        }

        Map<String,Validate> validationInfos = this.validations.get(beanClass);
        if (validationInfos != null) {

            for (Map.Entry<String,Validate> entry : validationInfos.entrySet()) {
                String propertyName = entry.getKey();
                Validate validationInfo = entry.getValue();

                // If the field is required, and we don't have index params that collapse
                // to that property name, check that it was supplied
                if (validationInfo.required() && !indexedParams.contains(propertyName)) {
                    String[] values = context.getRequest().getParameterValues(propertyName);
                    log.debug("Checking required field: ", propertyName, ", with values: ", values);
                    checkSingleRequiredField(propertyName, propertyName, values, errors);
                }
            }
        }

        // Now the easy work is done, figure out which rows of indexed props had values submitted
        // and what to flag up as failing required field validation
        if (indexedParams.size() > 0) {
            Map<String,Row> rows = new HashMap<String,Row>();

            for (Map.Entry<ParameterName,String[]> entry : parameters.entrySet()) {
                ParameterName name = entry.getKey();
                String[] values = entry.getValue();

                if (name.isIndexed()) {
                    String rowKey = name.getName().substring(0, name.getName().indexOf(']')+1);
                    if (!rows.containsKey(rowKey)) {
                        rows.put(rowKey, new Row());
                    }

                    rows.get(rowKey).put(name, values);
                }
            }

            for (Row row : rows.values()) {
                if (row.hasNonEmptyValues()) {
                    for (Map.Entry<ParameterName,String[]> entry : row.entrySet()) {
                        ParameterName name = entry.getKey();
                        String[] values = entry.getValue();
                        Validate validationInfo = validationInfos.get(name.getStrippedName());

                        if (validationInfo != null && validationInfo.required()) {
                            checkSingleRequiredField
                                    (name.getName(), name.getStrippedName(), values, errors);
                        }
                    }
                }
                else {
                    // If the row is full of empty data, get rid of it all to
                    // prevent problems in downstream validation
                    for (ParameterName name : row.keySet()) {
                        parameters.remove(name);
                    }
                }
            }

        }
    }

    /**
     * <p>Checks to see if a single field's set of values are 'present', where that is defined
     * as having one or more values, and where each value is a non-empty String after it has
     * had white space trimmed from each end.<p>
     *
     * <p>For any fields that fail validation, creates a ScopedLocaliableError that uses the
     * stripped name of the field to find localized info (e.g. foo.bar instead of foo[1].bar).
     * The error is bound to the actual field on the form though, e.g. foo[1].bar.</p>
     *
     * @param name the name of the parameter verbatim from the request
     * @param strippedName the name of the parameter with any indexing removed from it
     * @param values the String[] of values that was submitted in the request
     * @param errors a ValidationErrors object into which errors can be placed
     */
    protected void checkSingleRequiredField(String name,
                                            String strippedName,
                                            String[] values,
                                            ValidationErrors errors) {
        if (values == null || values.length == 0) {
            ValidationError error = new ScopedLocalizableError("validation.required",
                                                               "valueNotPresent");
            error.setFieldValue(null);
            errors.add( name, error );
        }
        else {
            for (String value : values) {
                if (value.length() == 0) {
                    ValidationError error = new ScopedLocalizableError("validation.required",
                                                                       "valueNotPresent");
                    error.setFieldValue(value);
                    errors.add( name, error );
                }
            }
        }
    }

    /**
     * Performs several basic validations on the String value supplied in the HttpServletRequest,
     * based on information provided in annotations on the ActionBean.
     *
     * @param propertyName the name of the property being validated (used for constructing errors)
     * @param values the String[] of values from the request being validated
     * @param validationInfo the Valiate annotation that was decorating the property being validated
     * @param errors a collection of errors to be populated with any validation errors discovered
     */
    protected void doPreConversionValidations(ParameterName propertyName,
                                              String[] values,
                                              Validate validationInfo,
                                              List<ValidationError> errors) {

        for (String value : values) {
            // Only run validations when there are non-empty values
            if (value != null && value.length() > 0) {
                if (validationInfo.minlength() != -1 && value.length() < validationInfo.minlength()) {
                    ValidationError error =
                            new ScopedLocalizableError ("validation.minlength",
                                                        "valueTooShort",
                                                        validationInfo.minlength());

                    error.setFieldValue(value);
                    errors.add( error );
                }

                if (validationInfo.maxlength() != -1 && value.length() > validationInfo.maxlength()) {
                    ValidationError error =
                            new ScopedLocalizableError("validation.maxlength",
                                                       "valueTooLong",
                                                       validationInfo.maxlength());
                    error.setFieldValue(value);
                    errors.add( error );
                }

                if ( validationInfo.mask().length() > 0 &&
                    !Pattern.compile(validationInfo.mask()).matcher(value).matches() ) {

                    ValidationError error =
                            new ScopedLocalizableError("validation.mask",
                                                       "valueDoesNotMatch");

                    error.setFieldValue(value);
                    errors.add( error );
                }
            }
        }
    }

    /**
     * Performs basic post-conversion validations on the properties of the ActionBean after they
     * have been converted to their rich type by the type conversion system.  Validates single
     * properties in isolation from other properties.
     *
     * @param propertyName the name of the property being validated (used for constructing errors)
     * @param values the List of converted values - possibly empty but never null
     * @param validationInfo the Valiate annotation that was decorating the property being validated
     * @param errors a collection of errors to be populated with any validation errors discovered
     */
    protected void doPostConversionValidations(ParameterName propertyName,
                                              List<Object> values,
                                              Validate validationInfo,
                                              List<ValidationError> errors) {

        for (Object value : values) {
            // If the value is a number then we should check to see if there are range boundaries
            // established, and check them.
            if (value instanceof Number) {
                Number number = (Number) value;

                if (validationInfo.minvalue() != Double.MIN_VALUE &&
                        number.doubleValue() < validationInfo.minvalue() ) {
                    ValidationError error = new ScopedLocalizableError("validation.minvalue",
                                                                       "valueBelowMinimum",
                                                                       validationInfo.minvalue());
                    error.setFieldValue( String.valueOf(value) );
                    errors.add(error);
                }

                if (validationInfo.maxvalue() != Double.MAX_VALUE &&
                        number.doubleValue() > validationInfo.maxvalue() ) {
                    ValidationError error = new ScopedLocalizableError("validation.maxvalue",
                                                                       "valueAboveMaximum",
                                                                       validationInfo.maxvalue());
                    error.setFieldValue( String.valueOf(value) );
                    errors.add(error);
                }
            }
        }
    }

    /**
     * <p>Converts the String[] of values for a given parameter in the HttpServletRequest into the
     * desired type of Object.  If a converter is declared using an annotation for the property
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
     * @param bean the ActionBean on which the property to convert exists
     * @param values a String array of values to attempt conversion of
     * @param errors a List into which ValidationError objects will be populated for any errors
     *        discovered during conversion.
     * @param validationInfo the @Validate annotation for the property if one exists
     * @return List<Object> a List of objects containing only objects of the desired type. It is
     *         not guaranteed to be the same length as the values array passed in.
     */
    private List<Object> convert(ActionBean bean,
                                 ParameterName propertyName,
                                 String[] values,
                                 Class propertyType,
                                 Validate validationInfo,
                                 List<ValidationError> errors) throws Exception {

        List<Object> returns = new ArrayList<Object>();
        propertyType = getRealType(bean, propertyType, propertyName);

        // Dig up the type converter
        TypeConverter converter = null;
        if (validationInfo !=  null && validationInfo.converter() != TypeConverter.class) {
            converter = this.configuration.getTypeConverterFactory()
                .getInstance(validationInfo.converter(), bean.getContext().getRequest().getLocale());
        }
        else {
            converter = this.configuration.getTypeConverterFactory()
                    .getTypeConverter(propertyType, bean.getContext().getRequest().getLocale());
        }

        log.debug("Converting ", values.length, " value(s) using converter ", converter);

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
                        error.setFieldName(propertyName.getStrippedName());
                        error.setFieldValue(values[i]);
                    }
                }
                catch(Exception e) {
                    //TODO: figure out what to do, if anything, with these exceptions
                    log.warn(e, "Looks like type converter ", converter, " threw an exception.");
                }
            }
        }

        return returns;
    }
}

class Row extends HashMap<ParameterName,String[]> {
    private boolean hasNonEmptyValues = false;

    /**
     * Adds the value to the map, along the way checking to see if there are any
     * non-null values for the row so far.
     */
    public String[] put(ParameterName key, String[] values) {
        if (!hasNonEmptyValues) {
            hasNonEmptyValues =  (values != null) &&
                                 (values.length > 0) &&
                                 (values[0] != null) &&
                                 (values[0].trim().length() > 0);


        }
        return super.put(key, values);
    }

    /** Returns true if the row had any non-empty values in it, otherwise false. */
    public boolean hasNonEmptyValues() {
        return this.hasNonEmptyValues;
    }
}