/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.Wizard;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.CryptoUtil;
import net.sourceforge.stripes.util.HtmlUtil;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.util.bean.ExpressionException;
import net.sourceforge.stripes.util.bean.NoSuchPropertyException;
import net.sourceforge.stripes.util.bean.PropertyExpression;
import net.sourceforge.stripes.util.bean.PropertyExpressionEvaluation;
import net.sourceforge.stripes.util.bean.BeanUtil;
import net.sourceforge.stripes.validation.ScopedLocalizableError;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.Expression;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * <p>Implementation of the ActionBeanPropertyBinder interface that uses Stripes' built in
 * property expression support to perform JavaBean property binding. Several additions/enhancements
 * are available above and beyond the standard JavaBean syntax.  These include:</p>
 *
 * <ul>
 *   <li>The ability to instantiate and set null intermediate properties in a property chain</li>
 *   <li>The ability to use Lists and Maps directly for indexed properties</li>
 *   <li>The ability to infer type information from the generics information in classes</li>
 * </ul>
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class DefaultActionBeanPropertyBinder implements ActionBeanPropertyBinder {
    private static Log log = Log.getInstance(DefaultActionBeanPropertyBinder.class);
    private static Set<String> SPECIAL_KEYS = new HashSet<String>();

    static {
        SPECIAL_KEYS.add(StripesConstants.URL_KEY_SOURCE_PAGE);
        SPECIAL_KEYS.add(StripesConstants.URL_KEY_FIELDS_PRESENT);
        SPECIAL_KEYS.add(StripesConstants.URL_KEY_FLASH_SCOPE_ID);
    }

    /** Map of validation annotations that is built at startup. */
    private Map<Class<? extends ActionBean>, Map<String,Validate>> validations;

    /**
     * Map of validation annotations to the set of events they should be run on. Note that
     * the events may be prepended with "!", so watch out!
     */
    private Map<Validate,Set<String>> validationEventMap;

    /** Configuration instance passed in at initialization time. */
    private Configuration configuration;


    /**
     * Looks up and caches in a useful form the metadata necessary to perform validations as
     * properties are bound to the bean.
     */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
        Set<Class<? extends ActionBean>> beanClasses = ActionClassCache.getInstance().getActionBeanClasses();
        this.validations = new HashMap<Class<? extends ActionBean>, Map<String,Validate>>();
        this.validationEventMap = new HashMap<Validate,Set<String>>();

        for (Class<? extends ActionBean> beanClass : beanClasses) {
            Map<String, Validate> fieldValidations = new HashMap<String, Validate>();
            processClassAnnotations(beanClass, fieldValidations);
            this.validations.put(beanClass, fieldValidations);

            // Go through and put the list of events for this validation into
            // a quicker to access structure
            for (Validate info : fieldValidations.values()) {
                Set<String> events = null;
                if (info.on().length == 0) {
                    events = Collections.emptySet();
                }
                else {
                    events = new HashSet<String>();
                    for (String event : info.on()) {
                        events.add(event);
                    }
                }

                this.validationEventMap.put(info, events);
            }

            // Print out a pretty debug message showing what validations got configured
            StringBuilder builder = new StringBuilder(128);
            for (Map.Entry<String,Validate> entry : fieldValidations.entrySet()) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(entry.getKey());
                builder.append("->");
                builder.append(ReflectUtil.toString(entry.getValue()));
            }

            log.debug("Loaded validations for ActionBean ", beanClass.getSimpleName(), ": ",
                      builder.length() > 0 ? builder : "<none>");
        }
    }

    /**
     * Helper method that processes a class looking for validation annotations. Will recurse
     * and process the superclasses first in order to ensure that annotations lower down the
     * inheritance hierarchy take precedence over those higher up.
     *
     * @param clazz the ActionBean subclasses (or parent thereof) in question
     * @param fieldValidations a map of fieldname->Validate in which to store validations
     */
    protected void processClassAnnotations(Class clazz, Map<String,Validate> fieldValidations) {
        Class superclass = clazz.getSuperclass();
        if (superclass != null) {
            processClassAnnotations(superclass, fieldValidations);
        }

        // Process the methods on the class
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue; // only public methods!
            }

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
        Field[] fields = clazz.getDeclaredFields();
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
        ValidationErrors fieldErrors = context.getValidationErrors();

        // Take the ParameterMap and turn the keys into ParameterNames
        Map<ParameterName,String[]> parameters = getParameters(context);

        // Run the required validation first to catch fields that weren't even submitted
        if (validate) {
            validateRequiredFields(parameters, bean, fieldErrors);
        }

        // Converted values for all fields are accumulated in this map to make post-conversion
        // validation go a little easier
        Map<ParameterName,List<Object>> allConvertedFields = new TreeMap<ParameterName,List<Object>>();

        // First we bind all the regular parameters
        for (Map.Entry<ParameterName,String[]> entry : parameters.entrySet() ) {
            List<Object> convertedValues = null;
            ParameterName name = entry.getKey();

            try {
                String pname = name.getName(); // exact name of the param in the request

                if (!SPECIAL_KEYS.contains(pname) && !pname.equals(context.getEventName())
                        && !fieldErrors.containsKey(pname)
                        && !pname.toLowerCase().startsWith("context")) {
                    log.trace("Running binding for property with name: ", name);

                    // Determine the target type
                    Validate validationInfo = this.validations.get(bean.getClass()).get(name.getStrippedName());
                    PropertyExpressionEvaluation eval =
                            new PropertyExpressionEvaluation(PropertyExpression.getExpression(pname), bean);
                    Class type = eval.getType();
                    Class scalarType = eval.getScalarType();

                    if (type == null && (validationInfo == null || validationInfo.converter() == null)) {
                        log.trace("Could not find type for property '", name.getName(), "' of '",
                                  bean.getClass().getSimpleName(), "' probably because it's not ",
                                  "a property of the bean.  Skipping binding.");
                        continue;
                    }
                    String[] values = entry.getValue();

                    // Do Validation and type conversion
                    List<ValidationError> errors = new ArrayList<ValidationError>();

                    // If the property should be ignored, skip to the next property
                    if (validationInfo != null && validationInfo.ignore()) {
                        continue;
                    }

                    if (validate && validationInfo != null) {
                        doPreConversionValidations(name, values, validationInfo, errors);
                    }

                    convertedValues = convert(bean, name, values, scalarType, validationInfo, errors);
                    allConvertedFields.put(name, convertedValues);

                    // If we have errors, save them, otherwise bind the parameter to the form
                    if (errors.size() > 0) {
                        fieldErrors.addAll(name.getName(), errors);
                    }
                    else if (convertedValues.size() > 0) {
                        bindNonNullValue(bean, eval, convertedValues, type, scalarType);
                    }
                    else {
                        bindNullValue(bean, name.getName(), type);
                    }
                }
            }
            catch (Exception e) {
                handlePropertyBindingError(bean, name, convertedValues, e, fieldErrors);
            }
        }

        // Null out any values that were in the form, but values were not supplied
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

        // Run post-conversion validation after absolutely everything has been bound
        // and validated so that the expression validation can have access to the full
        // state of the bean
        if (validate) {
            doPostConversionValidations(bean, allConvertedFields, fieldErrors);
        }

        return fieldErrors;
    }

    /**
     * Invoked whenever an exception is thrown when attempting to bind a property to an
     * ActionBean.  By default logs some information about the occurrence, but could be overridden
     * to do more intelligent things based on the application.
     */
    protected void handlePropertyBindingError(ActionBean bean, ParameterName name, List<Object> values,
                                              Exception e, ValidationErrors errors) {
        if (e instanceof NoSuchPropertyException) {
            NoSuchPropertyException nspe = (NoSuchPropertyException) e;
            // No stack trace if it's a no such property exception
            log.debug("Could not bind property with name [", name, "] to bean of type: ",
                      bean.getClass().getSimpleName(), " : ", nspe.getMessage());
        }
        else {
            log.debug(e, "Could not bind property with name [", name, "] to bean of type: ",
                      bean.getClass().getSimpleName());
        }
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

        for (String name: getFieldsPresentInfo(bean)) {
            if (!paramatersSubmitted.contains(name)) {
                try {
                    BeanUtil.setPropertyToNull(name, bean);
                }
                catch (Exception e) {
                    log.warn(e, "Could not set property '", name, "' to null on ActionBean of",
                             "type '", bean.getClass(), "'.");
                }
            }
        }
    }

    /**
     * In a lot of cases (and specifically during wizards) the Stripes form field writes out
     * a hidden field containing a set of field names. This is encrypted to stop the user from
     * monkeying with it. This method retrieves the list of field names, decrypts it and splits
     * it out into a Collection of field names.

     * @param bean the current ActionBean
     * @return a non-null (though possibly empty) list of field names
     */
    protected Collection<String> getFieldsPresentInfo(ActionBean bean) {
        HttpServletRequest request = bean.getContext().getRequest();
        String fieldsPresent = request.getParameter(StripesConstants.URL_KEY_FIELDS_PRESENT);
        boolean isWizard = bean.getClass().getAnnotation(Wizard.class) != null;

        if (fieldsPresent == null || "".equals(fieldsPresent)) {
            if (isWizard) {
                //FIXME: might want to let the ActionBean handle the initial request somehow?
                throw new StripesRuntimeException(
                        "Submission of a wizard form in Stripes absolutely requires that " +
                                "the hidden field Stripes writes containing the names of the fields " +
                                "present on the form is present and encrypted (as Stripes write it). " +
                                "This is necessary to prevent a user from spoofing the system and " +
                                "getting around any security/data checks."
                );
            }
            else {
                return Collections.emptySet();
            }
        }
        else {
            try {
                fieldsPresent = CryptoUtil.decrypt(fieldsPresent, request);
            }
            catch (GeneralSecurityException gse) {
                if (isWizard) {
                    throw new StripesRuntimeException("Stripes attmpted and failed to decrypt " +
                            "the non-null value in the 'fields present' field. Because this form " +
                            "submission is a wizard this situation cannot be accepted as it could " +
                            "result in a security problem. It is usually the result of either " +
                            "tampering with hidden field values, or session expiration.", gse);
                }
                else {
                    return Collections.emptySet();
                }
            }
            return HtmlUtil.splitValues(fieldsPresent);
        }
    }

    /**
     * Internal helper method to bind one or more values to a single property on an
     * ActionBean. If the target type is an array of Collection, then all values are bound.
     * If the target type is a scalar type then the first value in the List of values is bound.
     *
     * @param bean the ActionBean instance to which the property is being bound
     * @param propertyEvaluation the property evaluation to be used to set the property
     * @param valueOrValues a List containing one or more values
     * @param targetType the declared type of the property on the ActionBean
     * @throws Exception if the property cannot be bound for any reason
     */
    protected void bindNonNullValue(ActionBean bean,
                                    PropertyExpressionEvaluation propertyEvaluation,
                                    List<Object> valueOrValues,
                                    Class targetType,
                                    Class scalarType) throws Exception {
        Class valueType = valueOrValues.iterator().next().getClass();

        // If the target type is an array, set it as one, otherwise set as scalar
        if (targetType.isArray() && !valueType.isArray()) {
            Object typedArray = Array.newInstance(scalarType, valueOrValues.size());
            for (int i = 0; i<valueOrValues.size(); ++i) {
                Array.set(typedArray, i, valueOrValues.get(i));
            }

            propertyEvaluation.setValue(typedArray);
        }
        else if (Collection.class.isAssignableFrom(targetType) &&
                !Collection.class.isAssignableFrom(valueType)) {
            Collection collection = null;
            if (targetType.isInterface()) {
                collection = (Collection) ReflectUtil.getInterfaceInstance(targetType);
            }
            else {
                collection = (Collection) targetType.newInstance();
            }

            collection.addAll(valueOrValues);
            propertyEvaluation.setValue(collection);
        }
        else {
            propertyEvaluation.setValue(valueOrValues.get(0));
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
     * @throws ExpressionException if the value cannot be manipulated for any reason
     */
    protected void bindNullValue(ActionBean bean, String property, Class type) throws ExpressionException {
        BeanUtil.setPropertyToNull(property, bean);
    }


    /**
     * Converts the map of parameters in the request into a Map of ParameterName to String[].
     * Returns a SortedMap so that when iterated over parameter names are accessed in order
     * of length of parameter name.
     */
    protected SortedMap<ParameterName, String[]> getParameters(ActionBeanContext context) {
        Map<String, String[]> requestParameters = context.getRequest().getParameterMap();
        SortedMap<ParameterName, String[]> parameters = new TreeMap<ParameterName,String[]>();

        for (Map.Entry<String,String[]> entry : requestParameters.entrySet()) {
            parameters.put(new ParameterName(entry.getKey().trim()), entry.getValue());
        }

        return parameters;
    }

    /**
     * Attempt to set the named property on the target bean.  If the binding
     * fails for any reason (property does not exist, type conversion not possible etc.) an
     * exception will be thrown.
     *
     * @param bean the ActionBean on to which the property is to be bound
     * @param propertyName the name of the property to be bound (simple or complex)
     * @param propertyValue the value of the target property
     * @throws Exception thrown if the property cannot be bound for any reason
     */
    public void bind(ActionBean bean, String propertyName, Object propertyValue) throws Exception {
        BeanUtil.setPropertyValue(propertyName, bean, propertyValue);
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
     * Validates that all required fields have been submitted. This is done by looping through
     * the set of validation annotations and checking that each field marked as required
     * was submitted in the request and submitted with a non-empty value.
     */
    protected void validateRequiredFields(Map<ParameterName,String[]> parameters,
                                          ActionBean bean,
                                          ValidationErrors errors) {

        log.debug("Running required field validation on bean class ", bean.getClass().getName());

        // Assemble a set of names that we know have indexed parameters, so we won't check
        // for required-ness the regular way
        Set<String> indexedParams = new HashSet<String>();
        for (ParameterName name : parameters.keySet()) {
            if (name.isIndexed()) {
                indexedParams.add(name.getStrippedName());
            }
        }

        Map<String,Validate> validationInfos = this.validations.get(bean.getClass());
        StripesRequestWrapper req = StripesRequestWrapper.findStripesWrapper(bean.getContext().getRequest());

        if (validationInfos != null) {
            boolean wizard = bean.getClass().getAnnotation(Wizard.class) != null;
            Collection<String> fieldsOnPage = getFieldsPresentInfo(bean);

            for (Map.Entry<String,Validate> entry : validationInfos.entrySet()) {
                String propertyName = entry.getKey();
                Validate validationInfo = entry.getValue();

                // If the field is required, and we don't have index params that collapse
                // to that property name, check that it was supplied
                if (validationInfo.required() && !indexedParams.contains(propertyName)
                        && applies(validationInfo, bean.getContext())) {

                    // Make the added check that if the form is a wizard, the required field is
                    // in the set of fields that were on the page
                    if (!wizard || fieldsOnPage.contains(propertyName)) {
                        String[] values = bean.getContext().getRequest().getParameterValues(propertyName);
                        log.debug("Checking required field: ", propertyName, ", with values: ", values);
                        checkSingleRequiredField(propertyName, propertyName, values, req, errors);
                    }
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

                        if (validationInfo != null && validationInfo.required()
                                && applies(validationInfo, bean.getContext())) {
                            checkSingleRequiredField
                                    (name.getName(), name.getStrippedName(), values, req, errors);
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
                                            StripesRequestWrapper req,
                                            ValidationErrors errors) {

        // Firstly if the post is a multipart request, check to see if a file was
        // sent under that parameter name
        FileBean file = null;
        if (req.isMultipart() && (file = req.getFileParameterValue(name)) != null) {
            if (file.getSize() <= 0) {
                errors.add(name, new ScopedLocalizableError("validation.required",
                                                            "valueNotPresent"));
            }
        }
        // And if not, see if any regular parameters were sent
        else if (values == null || values.length == 0) {
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
     * @param bean the ActionBean that is undergoing validation and binding
     * @param convertedValues a map of ParameterName to all converted values for each field
     * @param errors the validaiton errors object to put errors in to
     */
    protected void doPostConversionValidations(ActionBean bean,
                                               Map<ParameterName,List<Object>> convertedValues,
                                               ValidationErrors errors) {

        for (Map.Entry<ParameterName, List<Object>> entry : convertedValues.entrySet()) {
            // Sort out what we need to validate this field
            ParameterName name = entry.getKey();
            List<Object> values = entry.getValue();
            Validate validationInfo = this.validations.get(bean.getClass()).get(name.getStrippedName());

            if (values.size() == 0 || validationInfo == null) {
                continue;
            }

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
                        errors.add(name.getName(), error);
                    }

                    if (validationInfo.maxvalue() != Double.MAX_VALUE &&
                            number.doubleValue() > validationInfo.maxvalue() ) {
                        ValidationError error = new ScopedLocalizableError("validation.maxvalue",
                                                                           "valueAboveMaximum",
                                                                           validationInfo.maxvalue());
                        error.setFieldValue( String.valueOf(value) );
                        errors.add(name.getName(), error);
                    }
                }
            }

            // And then do any expression validation
            doExpressionValidation(bean, name, values, validationInfo, errors);
        }
    }

    /**
     * Performs validation of attribute values using a JSP EL expression if one is
     * defined in the {@literal @}Validate annotation.  The expression is evaluated
     * once for each value converted.  Makes use of a custom VariableResolver implemenation
     * to make properties of the ActionBean available.
     *
     * @param bean the ActionBean who's property is being validated
     * @param name the name of the property being validated
     * @param values the non-null post-conversion values for the property
     * @param validationInfo the validation annotation for the property
     * @param errors the validation errors object to add errors to
     */
    protected void doExpressionValidation(ActionBean bean,
                                          ParameterName name,
                                          List<Object> values,
                                          Validate validationInfo,
                                          ValidationErrors errors) {
        // If a validation expression was supplied, see if we can process it!
        Expression expr = null;
        DelegatingVariableResolver resolver = null;

        if ( !"".equals(validationInfo.expression()) ) {
            final PageContext context = DispatcherHelper.getPageContext();

            if (context == null) {
                log.error("Could not process expression based validation. It would seem that ",
                          "your servlet container is being mean and will not let the dispatcher ",
                          "servlet manufacture a PageContext object through the JSPFactory. The ",
                          "result of this is that expression validation will be disabled. Sorry.");
            }
            else {
                try {
                    // If this turns out to be slow we could probably cache the parsed expression
                    String expression = validationInfo.expression();
                    if (!expression.startsWith("${")) {
                        expression = "${" + expression + "}";
                    }

                    ExpressionEvaluator evaluator = context.getExpressionEvaluator();
                    expr = evaluator.parseExpression(expression, Boolean.class, null);
                    resolver = new DelegatingVariableResolver(bean, context.getVariableResolver());
                }
                catch (ELException ele) {
                    throw new StripesRuntimeException("Could not parse the EL expression being " +
                            "used to validate field " + name.getName() + ". This is " +
                            "not a transient error. Please double check the following expression " +
                            "for errors: " + validationInfo.expression(), ele);
                }
            }
        }

        for (Object value : values) {
            // And then if we have an expression to use
            if (expr != null) {
                try {
                    resolver.setCurrentValue(value);
                    Boolean result = (Boolean) expr.evaluate(resolver);
                    if (!Boolean.TRUE.equals(result)) {
                        ValidationError error = new ScopedLocalizableError("validation.expression",
                                                                           "valueFailedExpression");
                        error.setFieldValue(String.valueOf(value));
                        errors.add(name.getName(), error);
                    }
                }
                catch (ELException ele) {
                    log.error("Error evaluating expression for property ", name.getName(),
                              " of class ", bean.getClass().getSimpleName(), ". Expression: ",
                              validationInfo.expression());
                }
            }
        }
    }

    /**
     * Determines whether or not a specific validation applies to the current event or not.
     *
     * @param info the ValidationInfo being looked at
     * @param context the current ActionBeanContext
     * @return true if the Validation should be executed, false otherwise
     */
    protected boolean applies(Validate info, ActionBeanContext context) {
        Set<String> events = this.validationEventMap.get(info);
        String current = context.getEventName();

        if (info.on().length == 0 || current == null) {
            return true;
        }

        if (info.on()[0].startsWith("!")) {
            return !events.contains("!" + current);
        }
        else {
            return events.contains(current);
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
                    else if (propertyType.isAssignableFrom(String.class)) {
                        retval = values[i];
                    }
                    else {
                        Constructor constructor = propertyType.getConstructor(String.class);
                        if (constructor != null) {
                            retval = constructor.newInstance(values[i]);
                        }
                        else {
                            log.debug("Could not find a way to convert the parameter ",
                                      propertyName.getName(), " to a ", propertyType.getSimpleName(),
                                      ". No TypeConverter could be found and the class does not ",
                                      "have a constructor that takes a single String parameter.");
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

/**
 * A JSP EL VariableResolver that first attempts to look up the value of the
 * variable as a first level property on the ActionBean, and if does not exist
 * then delegates to the built in resolver.
 *
 * @author Tim Fennell
 * @since Stripes 1.3
 */
class DelegatingVariableResolver implements VariableResolver {
    private ActionBean bean;
    private VariableResolver delegate;
    private Object currentValue;

    /** Constructs a resolver based on the action bean and resolver supplied. */
    DelegatingVariableResolver(ActionBean bean, VariableResolver resolver) {
        this.bean = bean;
        this.delegate = resolver;
    }

    /** Sets the value that the 'this' variable will point at. */
    void setCurrentValue(Object value) { this.currentValue = value; }

    /**
     * First tries to fish the property off the ActionBean and if that fails
     * delegates to the contained variable resolver.
     *
     * @param property the name of the variable/property being looked for
     * @return
     * @throws ELException
     */
    public Object resolveVariable(String property) throws ELException {
        if ("this".equals(property)) {
            return this.currentValue;
        }
        else if ("actionBean".equals(property)) {
            return this.bean;
        }
        else {
            Object result = null;
            try { result = BeanUtil.getPropertyValue(property, bean); } catch (Exception e) {}

            if (result == null) {
                result = delegate.resolveVariable(property);
            }
            return result;
        }
    }
}