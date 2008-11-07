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
import net.sourceforge.stripes.util.CollectionUtil;
import net.sourceforge.stripes.util.CryptoUtil;
import net.sourceforge.stripes.util.HtmlUtil;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.util.bean.BeanUtil;
import net.sourceforge.stripes.util.bean.ExpressionException;
import net.sourceforge.stripes.util.bean.NoSuchPropertyException;
import net.sourceforge.stripes.util.bean.PropertyExpression;
import net.sourceforge.stripes.util.bean.PropertyExpressionEvaluation;
import net.sourceforge.stripes.validation.ScopedLocalizableError;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.TypeConverterFactory;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMetadata;
import net.sourceforge.stripes.validation.expression.ExpressionValidator;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * <p>
 * Implementation of the ActionBeanPropertyBinder interface that uses Stripes' built in property
 * expression support to perform JavaBean property binding. Several additions/enhancements are
 * available above and beyond the standard JavaBean syntax. These include:
 * </p>
 * 
 * <ul>
 * <li>The ability to instantiate and set null intermediate properties in a property chain</li>
 * <li>The ability to use Lists and Maps directly for indexed properties</li>
 * <li>The ability to infer type information from the generics information in classes</li>
 * </ul>
 * 
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class DefaultActionBeanPropertyBinder implements ActionBeanPropertyBinder {
    private static final Log log = Log.getInstance(DefaultActionBeanPropertyBinder.class);

    /** Configuration instance passed in at initialization time. */
    private Configuration configuration;

    /**
     * Looks up and caches in a useful form the metadata necessary to perform validations as
     * properties are bound to the bean.
     */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
    }

    /** Returns the Configuration object that was passed to the init() method. */
    protected Configuration getConfiguration() { return configuration; }

    /**
     * <p>
     * Loops through the parameters contained in the request and attempts to bind each one to the
     * supplied ActionBean. Invokes validation for each of the properties on the bean before binding
     * is attempted. Only fields which do not produce validation errors will be bound to the
     * ActionBean.
     * </p>
     * 
     * <p>
     * Individual property binding is delegated to the other interface method, bind(ActionBean,
     * String, Object), in order to allow for easy extension of this class.
     * </p>
     * 
     * @param bean the ActionBean whose properties are to be validated and bound
     * @param context the ActionBeanContext of the current request
     * @param validate true indicates that validation should be run, false indicates that only type
     *            conversion should occur
     */
    public ValidationErrors bind(ActionBean bean, ActionBeanContext context, boolean validate) {
        ValidationErrors fieldErrors = context.getValidationErrors();
        Map<String, ValidationMetadata> validationInfos = this.configuration
                .getValidationMetadataProvider().getValidationMetadata(bean.getClass());

        // Take the ParameterMap and turn the keys into ParameterNames
        Map<ParameterName, String[]> parameters = getParameters(bean);

        // Run the required validation first to catch fields that weren't even submitted
        if (validate) {
            validateRequiredFields(parameters, bean, fieldErrors);
        }

        // Converted values for all fields are accumulated in this map to make post-conversion
        // validation go a little easier
        Map<ParameterName, List<Object>> allConvertedFields = new TreeMap<ParameterName, List<Object>>();

        // First we bind all the regular parameters
        for (Map.Entry<ParameterName, String[]> entry : parameters.entrySet()) {
            List<Object> convertedValues = null;
            ParameterName name = entry.getKey();

            try {
                String pname = name.getName(); // exact name of the param in the request
                if (!StripesConstants.SPECIAL_URL_KEYS.contains(pname)
                        && !fieldErrors.containsKey(pname)) {
                    log.trace("Running binding for property with name: ", name);

                    // Determine the target type
                    ValidationMetadata validationInfo = validationInfos.get(name.getStrippedName());
                    PropertyExpressionEvaluation eval;
                    try {
                        eval = new PropertyExpressionEvaluation(PropertyExpression
                                .getExpression(pname), bean);
                    }
                    catch (Exception e) {
                        if (pname.equals(context.getEventName()))
                            continue;
                        else
                            throw e;
                    }
                    Class<?> type = eval.getType();
                    Class<?> scalarType = eval.getScalarType();

                    // Check to see if binding into this expression is permitted
                    if (!isBindingAllowed(eval))
                        continue;

                    if (type == null
                            && (validationInfo == null || validationInfo.converter() == null)) {
                        if (!pname.equals(context.getEventName())) {
                            log.trace("Could not find type for property '", name.getName(),
                                    "' of '", bean.getClass().getSimpleName(),
                                    "' probably because it's not ",
                                    "a property of the bean.  Skipping binding.");
                        }
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

                    // Only do type conversion if there aren't errors already
                    if (errors.isEmpty()) {
                        convertedValues = convert(bean, name, values, type, scalarType, validationInfo, errors);
                        allConvertedFields.put(name, convertedValues);
                    }

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
        StripesRequestWrapper request = StripesRequestWrapper.findStripesWrapper(context
                .getRequest());
        if (request.isMultipart()) {
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
     * <p>
     * Checks to see if binding is permitted for the provided expression evaluation. Note that the
     * expression is available through the {@code getExpression()} and the ActionBean is available
     * through the {@code getBean()} method on the evaluation.
     * </p>
     * 
     * <p>
     * By default checks to ensure that the expression is not attempting to bind into the
     * ActionBeanContext for security reasons.
     * </p>
     * 
     * @param eval the expression evaluation to check for binding permission
     * @return true if binding can/should proceed, false to veto binding
     */
    protected boolean isBindingAllowed(PropertyExpressionEvaluation eval) {
        boolean allowed = BindingPolicyManager.getInstance(eval.getBean().getClass())
                .isBindingAllowed(eval);
        if (!allowed) {
            String param = eval.getExpression().getSource();
            log.warn("Binding denied for parameter [", param, "]");
        }
        return allowed;
    }

    /**
     * Invoked whenever an exception is thrown when attempting to bind a property to an ActionBean.
     * By default logs some information about the occurrence, but could be overridden to do more
     * intelligent things based on the application.
     * 
     * @param bean the ActionBean that was the subject of binding
     * @param name the ParameterName object for the parameter being bound
     * @param values the list of values being bound, potentially null if the error occurred when
     *            binding a null value
     * @param e the exception raised during binding
     * @param errors the validation errors object associated to the ActionBean
     */
    protected void handlePropertyBindingError(ActionBean bean, ParameterName name,
            List<Object> values, Exception e, ValidationErrors errors) {
        if (e instanceof NoSuchPropertyException) {
            NoSuchPropertyException nspe = (NoSuchPropertyException) e;
            // No stack trace if it's a no such property exception
            log.debug("Could not bind property with name [", name, "] to bean of type: ", bean
                    .getClass().getSimpleName(), " : ", nspe.getMessage());
        }
        else {
            log.debug(e, "Could not bind property with name [", name, "] to bean of type: ", bean
                    .getClass().getSimpleName());
        }
    }

    /**
     * Uses a hidden field to determine what (if any) fields were present in the form but did not get
     * submitted to the server. For each such field the value is "softly" set to null on the
     * ActionBean. This is not uncommon for checkboxes, and also for multi-selects.
     * 
     * @param bean the ActionBean being bound to
     * @param context the current ActionBeanContext
     */
    @SuppressWarnings("unchecked")
    protected void bindMissingValuesAsNull(ActionBean bean, ActionBeanContext context) {
        Set<String> parametersSubmitted = context.getRequest().getParameterMap().keySet();

        for (String name : getFieldsPresentInfo(bean)) {
            if (!parametersSubmitted.contains(name)) {
                try {
                    BeanUtil.setPropertyToNull(name, bean);
                }
                catch (Exception e) {
                    handlePropertyBindingError(bean, new ParameterName(name), null, e, context
                            .getValidationErrors());
                }
            }
        }
    }

    /**
     * In a lot of cases (and specifically during wizards) the Stripes form field writes out a
     * hidden field containing a set of field names. This is encrypted to stop the user from
     * monkeying with it. This method retrieves the list of field names, decrypts it and splits it
     * out into a Collection of field names.
     * 
     * @param bean the current ActionBean
     * @return a non-null (though possibly empty) list of field names
     */
    protected Collection<String> getFieldsPresentInfo(ActionBean bean) {
        ActionBeanContext ctx = bean.getContext();
        String fieldsPresent = ctx.getRequest().getParameter(StripesConstants.URL_KEY_FIELDS_PRESENT);
        Wizard wizard = bean.getClass().getAnnotation(Wizard.class);
        boolean isWizard = wizard != null;

        if (fieldsPresent == null || "".equals(fieldsPresent)) {
            if (isWizard && !CollectionUtil.contains(wizard.startEvents(), ctx.getEventName())) {
                throw new StripesRuntimeException(
                        "Submission of a wizard form in Stripes absolutely requires that "
                                + "the hidden field Stripes writes containing the names of the fields "
                                + "present on the form is present and encrypted (as Stripes write it). "
                                + "This is necessary to prevent a user from spoofing the system and "
                                + "getting around any security/data checks.");
            }
            else {
                return Collections.emptySet();
            }
        }
        else {
            fieldsPresent = CryptoUtil.decrypt(fieldsPresent);
            return HtmlUtil.splitValues(fieldsPresent);
        }
    }

    /**
     * Internal helper method to bind one or more values to a single property on an ActionBean. If
     * the target type is an array of Collection, then all values are bound. If the target type is a
     * scalar type then the first value in the List of values is bound.
     * 
     * @param bean the ActionBean instance to which the property is being bound
     * @param propertyEvaluation the property evaluation to be used to set the property
     * @param valueOrValues a List containing one or more values
     * @param targetType the declared type of the property on the ActionBean
     * @throws Exception if the property cannot be bound for any reason
     */
    @SuppressWarnings("unchecked")
    protected void bindNonNullValue(ActionBean bean,
            PropertyExpressionEvaluation propertyEvaluation, List<Object> valueOrValues,
            Class targetType, Class scalarType) throws Exception {
        Class valueType = valueOrValues.iterator().next().getClass();

        // If the target type is an array, set it as one, otherwise set as scalar
        if (targetType.isArray() && !valueType.isArray()) {
            Object typedArray = Array.newInstance(scalarType, valueOrValues.size());
            for (int i = 0; i < valueOrValues.size(); ++i) {
                Array.set(typedArray, i, valueOrValues.get(i));
            }

            propertyEvaluation.setValue(typedArray);
        }
        else if (Collection.class.isAssignableFrom(targetType)
                && !Collection.class.isAssignableFrom(valueType)) {
            Collection collection = null;
            if (targetType.isInterface()) {
                collection = (Collection) ReflectUtil.getInterfaceInstance(targetType);
            }
            else {
                collection = getConfiguration().getObjectFactory().newInstance(
                        (Class<? extends Collection>) targetType);
            }

            collection.addAll(valueOrValues);
            propertyEvaluation.setValue(collection);
        }
        else {
            propertyEvaluation.setValue(valueOrValues.get(0));
        }
    }

    /**
     * Internal helper method that determines what to do when no value was supplied for a given form
     * field (but the field was present on the page). In all cases if the property is already null,
     * or intervening objects in a nested property are null, nothing is done. If the property is
     * non-null, it will be set to null. Unless the property is a collection, in which case it will
     * be clear()'d.
     * 
     * @param bean the ActionBean to which properties are being bound
     * @param property the name of the property being bound
     * @param type the declared type of the property on the ActionBean
     * @throws ExpressionException if the value cannot be manipulated for any reason
     */
    protected void bindNullValue(ActionBean bean, String property, Class<?> type)
            throws ExpressionException {
        BeanUtil.setPropertyToNull(property, bean);
    }

    /**
     * Converts the map of parameters in the request into a Map of ParameterName to String[].
     * Returns a SortedMap so that when iterated over parameter names are accessed in order of
     * length of parameter name.
     */
    @SuppressWarnings("unchecked")
    protected SortedMap<ParameterName, String[]> getParameters(ActionBean bean) {
        Map<String, String[]> requestParameters = bean.getContext().getRequest().getParameterMap();
        Map<String, ValidationMetadata> validations = StripesFilter.getConfiguration()
                .getValidationMetadataProvider().getValidationMetadata(bean.getClass());
        SortedMap<ParameterName, String[]> parameters = new TreeMap<ParameterName, String[]>();

        for (Map.Entry<String, String[]> entry : requestParameters.entrySet()) {
            ParameterName paramName = new ParameterName(entry.getKey().trim());
            ValidationMetadata validation = validations.get(paramName.getStrippedName());
            parameters.put(paramName, trim(entry.getValue(), validation));
        }

        return parameters;
    }

    /**
     * Attempt to set the named property on the target bean. If the binding fails for any reason
     * (property does not exist, type conversion not possible etc.) an exception will be thrown.
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
     * Validates that all required fields have been submitted. This is done by looping through the
     * set of validation annotations and checking that each field marked as required was submitted
     * in the request and submitted with a non-empty value.
     */
    protected void validateRequiredFields(Map<ParameterName, String[]> parameters, ActionBean bean,
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

        Map<String, ValidationMetadata> validationInfos = this.configuration
                .getValidationMetadataProvider().getValidationMetadata(bean.getClass());
        ActionBeanContext context = bean.getContext();
        HttpServletRequest request = context.getRequest();
        StripesRequestWrapper stripesReq = StripesRequestWrapper.findStripesWrapper(request);

        if (validationInfos != null) {
            boolean wizard = bean.getClass().getAnnotation(Wizard.class) != null;
            Collection<String> fieldsOnPage = getFieldsPresentInfo(bean);

            for (Map.Entry<String, ValidationMetadata> entry : validationInfos.entrySet()) {
                String propertyName = entry.getKey();
                ValidationMetadata validationInfo = entry.getValue();

                // If the field is required, and we don't have index params that collapse
                // to that property name, check that it was supplied
                if (validationInfo.requiredOn(context.getEventName())
                        && !indexedParams.contains(propertyName)) {

                    // Make the added check that if the form is a wizard, the required field is
                    // in the set of fields that were on the page
                    if (!wizard || fieldsOnPage.contains(propertyName)) {
                        String[] values = trim(request.getParameterValues(propertyName), validationInfo);
                        log.debug("Checking required field: ", propertyName, ", with values: ", values);
                        checkSingleRequiredField(propertyName, propertyName, values, stripesReq, errors);
                    }
                }
            }
        }

        // Now the easy work is done, figure out which rows of indexed props had values submitted
        // and what to flag up as failing required field validation
        if (indexedParams.size() > 0) {
            Map<String, Row> rows = new HashMap<String, Row>();

            for (Map.Entry<ParameterName, String[]> entry : parameters.entrySet()) {
                ParameterName name = entry.getKey();
                String[] values = entry.getValue();

                if (name.isIndexed()) {
                    String rowKey = name.getName().substring(0, name.getName().indexOf(']') + 1);
                    if (!rows.containsKey(rowKey)) {
                        rows.put(rowKey, new Row());
                    }

                    rows.get(rowKey).put(name, values);
                }
            }

            for (Row row : rows.values()) {
                if (row.hasNonEmptyValues()) {
                    for (Map.Entry<ParameterName, String[]> entry : row.entrySet()) {
                        ParameterName name = entry.getKey();
                        String[] values = entry.getValue();
                        ValidationMetadata validationInfo = validationInfos.get(name.getStrippedName());

                        if (validationInfo != null
                                && validationInfo.requiredOn(context.getEventName())) {
                            checkSingleRequiredField(name.getName(), name.getStrippedName(),
                                    values, stripesReq, errors);
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
     * <p>
     * Checks to see if a single field's set of values are 'present', where that is defined as
     * having one or more values, and where each value is a non-empty String after it has had white
     * space trimmed from each end.
     * <p>
     * 
     * <p>
     * For any fields that fail validation, creates a ScopedLocaliableError that uses the stripped
     * name of the field to find localized info (e.g. foo.bar instead of foo[1].bar). The error is
     * bound to the actual field on the form though, e.g. foo[1].bar.
     * </p>
     * 
     * @param name the name of the parameter verbatim from the request
     * @param strippedName the name of the parameter with any indexing removed from it
     * @param values the String[] of values that was submitted in the request
     * @param errors a ValidationErrors object into which errors can be placed
     */
    protected void checkSingleRequiredField(String name, String strippedName, String[] values,
            StripesRequestWrapper req, ValidationErrors errors) {

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
            errors.add(name, error);
        }
        else {
            for (String value : values) {
                if (value.length() == 0) {
                    ValidationError error = new ScopedLocalizableError("validation.required",
                            "valueNotPresent");
                    error.setFieldValue(value);
                    errors.add(name, error);
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
     * @param validationInfo the ValidationMetadata for the property being validated
     * @param errors a collection of errors to be populated with any validation errors discovered
     */
    protected void doPreConversionValidations(ParameterName propertyName, String[] values,
            ValidationMetadata validationInfo, List<ValidationError> errors) {

        for (String value : values) {
            // Only run validations when there are non-empty values
            if (value != null && value.length() > 0) {
                if (validationInfo.minlength() != null
                        && value.length() < validationInfo.minlength()) {
                    ValidationError error = new ScopedLocalizableError("validation.minlength",
                            "valueTooShort", validationInfo.minlength());

                    error.setFieldValue(value);
                    errors.add(error);
                }

                if (validationInfo.maxlength() != null
                        && value.length() > validationInfo.maxlength()) {
                    ValidationError error = new ScopedLocalizableError("validation.maxlength",
                            "valueTooLong", validationInfo.maxlength());
                    error.setFieldValue(value);
                    errors.add(error);
                }

                if (validationInfo.mask() != null
                        && !validationInfo.mask().matcher(value).matches()) {

                    ValidationError error = new ScopedLocalizableError("validation.mask",
                            "valueDoesNotMatch");

                    error.setFieldValue(value);
                    errors.add(error);
                }
            }
        }
    }

    /**
     * Performs basic post-conversion validations on the properties of the ActionBean after they
     * have been converted to their rich type by the type conversion system. Validates single
     * properties in isolation from other properties.
     * 
     * @param bean the ActionBean that is undergoing validation and binding
     * @param convertedValues a map of ParameterName to all converted values for each field
     * @param errors the validation errors object to put errors in to
     */
    protected void doPostConversionValidations(ActionBean bean,
            Map<ParameterName, List<Object>> convertedValues, ValidationErrors errors) {

        Map<String, ValidationMetadata> validationInfos = this.configuration
                .getValidationMetadataProvider().getValidationMetadata(bean.getClass());
        for (Map.Entry<ParameterName, List<Object>> entry : convertedValues.entrySet()) {
            // Sort out what we need to validate this field
            ParameterName name = entry.getKey();
            List<Object> values = entry.getValue();
            ValidationMetadata validationInfo = validationInfos.get(name.getStrippedName());

            if (values.size() == 0 || validationInfo == null) {
                continue;
            }

            for (Object value : values) {
                // If the value is a number then we should check to see if there are range
                // boundaries
                // established, and check them.
                if (value instanceof Number) {
                    Number number = (Number) value;

                    if (validationInfo.minvalue() != null
                            && number.doubleValue() < validationInfo.minvalue()) {
                        ValidationError error = new ScopedLocalizableError("validation.minvalue",
                                "valueBelowMinimum", validationInfo.minvalue());
                        error.setFieldValue(String.valueOf(value));
                        errors.add(name.getName(), error);
                    }

                    if (validationInfo.maxvalue() != null
                            && number.doubleValue() > validationInfo.maxvalue()) {
                        ValidationError error = new ScopedLocalizableError("validation.maxvalue",
                                "valueAboveMaximum", validationInfo.maxvalue());
                        error.setFieldValue(String.valueOf(value));
                        errors.add(name.getName(), error);
                    }
                }
            }

            // And then do any expression validation
            doExpressionValidation(bean, name, values, validationInfo, errors);
        }
    }

    /**
     * Performs validation of attribute values using a JSP EL expression if one is defined in the
     * {@literal @}Validate annotation. The expression is evaluated once for each value converted.
     * See {@link net.sourceforge.stripes.validation.expression.ExpressionValidator} for details
     * on how this is implemented.
     * 
     * @param bean the ActionBean who's property is being validated
     * @param name the name of the property being validated
     * @param values the non-null post-conversion values for the property
     * @param validationInfo the validation metadata for the property
     * @param errors the validation errors object to add errors to
     */
    protected void doExpressionValidation(ActionBean bean, ParameterName name, List<Object> values,
            ValidationMetadata validationInfo, ValidationErrors errors) {

        if (validationInfo.expression() != null)
            ExpressionValidator.evaluate(bean, name, values, validationInfo, errors);
    }

    /**
     * <p>
     * Converts the String[] of values for a given parameter in the HttpServletRequest into the
     * desired type of Object. If a converter is declared using an annotation for the property (or
     * getter/setter) then that converter will be used - if it does not convert to the right type an
     * exception will be logged and values will not be converted. If no Converter was specified then
     * a default converter will be looked up based on the target type of the property. If there is
     * no default converter, then a Constructor will be looked for on the target type which takes a
     * single String parameter. If such a Constructor exists it will be invoked.
     * </p>
     * 
     * <p>
     * Only parameter values that are non-null and do not equal the empty String will be converted
     * and returned. So an input array with one entry equaling the empty string, [""], will result
     * in an <b>empty</b> List being returned. Similarly, if a length three array is passed in with
     * one item equaling the empty String, a List of length two will be returned.
     * </p>
     * 
     * @param bean the ActionBean on which the property to convert exists
     * @param propertyName the name of the property being converted
     * @param values a String array of values to attempt conversion of
     * @param declaredType the declared type of the ActionBean property
     * @param scalarType if the declaredType is a collection, map or array then this will
     *           be the type contained within the collection/map value/array, otherwise
     *           the same as declaredType
     * @param validationInfo the validation metadata for the property if defined
     * @param errors a List into which ValidationError objects will be populated for any errors
     *            discovered during conversion.
     * @return List<Object> a List of objects containing only objects of the desired type. It is
     *         not guaranteed to be the same length as the values array passed in.
     */
    @SuppressWarnings("unchecked")
    protected List<Object> convert(ActionBean bean, ParameterName propertyName, String[] values,
                                   Class<?> declaredType, Class<?> scalarType,
                                   ValidationMetadata validationInfo, List<ValidationError> errors)
            throws Exception {

        List<Object> returns = new ArrayList<Object>();
        Class returnType = null;

        // Dig up the type converter.  This gets a bit tricky because we need to handle
        // the following cases:
        // 1. We need to simply find a converter for the declared type of a simple property
        // 2. We need to find a converter for the element type in a list/array/map
        // 3. We have a domain model object that implements List/Map and has a converter itself!
        TypeConverterFactory factory = this.configuration.getTypeConverterFactory();
        TypeConverter<?> converter = null;
        Locale locale = bean.getContext().getRequest().getLocale();

        converter = factory.getTypeConverter(declaredType, locale);
        if (validationInfo != null && validationInfo.converter() != null) {
            // If a specific converter was requested and it's the same type as one we'd use
            // for the declared type, set the return type appropriately
            if (converter != null && validationInfo.converter().isAssignableFrom(converter.getClass())) {
                returnType = declaredType;
            }
            // Otherwise assume that it's a converter for the scalar type inside a collection
            else {
                returnType = scalarType;
            }
            converter = factory.getInstance(validationInfo.converter(), locale);
        }
        // Else, if we got a converter for the declared type (e.g. Foo implementes List<Bar>)
        // then convert for the declared type
        else if (converter != null) {
            returnType = declaredType;
        }
        // Else look for a converter for the scalar type (Bar in List<Bar>)
        else {
            converter  = factory.getTypeConverter(scalarType, locale);
            returnType = scalarType;
        }

        log.debug("Converting ", values.length, " value(s) using ", (converter != null ?
            "converter " + converter.getClass().getName()
          : "Constructor(String) if available"));

        for (String value : values) {
            if (!"".equals(value)) {
                try {
                    if (validationInfo != null && validationInfo.encrypted()) {
                        value = CryptoUtil.decrypt(value);
                    }

                    Object retval = null;
                    if (converter != null) {
                        retval = converter.convert(value, returnType, errors);
                    }
                    else {
                        Constructor<?> constructor = returnType.getConstructor(String.class);
                        if (constructor != null) {
                            retval = constructor.newInstance(value);
                        }
                        else {
                            log.debug("Could not find a way to convert the parameter ", propertyName.getName(),
                                      " to a ", returnType.getSimpleName(), ". No TypeConverter could be ",
                                      "found and the class does not ", "have a constructor that takes a ",
                                      "single String parameter.");
                        }
                    }

                    // If we managed to get a non-null converted value, add it to the return set
                    if (retval != null) {
                        returns.add(retval);
                    }

                    // Set the field name and value on the error
                    for (ValidationError error : errors) {
                        error.setFieldName(propertyName.getStrippedName());
                        error.setFieldValue(value);
                    }
                }
                catch (Exception e) {
                    log.warn(e, "Looks like type converter ", converter, " threw an exception.");
                }
            }
        }

        return returns;
    }

    /**
     * Inspects the given {@link ValidationMetadata} object to determine if the given {@code values}
     * should be trimmed. If so, then the trimmed values are returned. Otherwise, the values are
     * returned unchanged. If {@code meta} is null, then the default action is taken, and the values
     * are trimmed. Either {@code values} or {@code meta} (or both) may be null.
     */
    protected String[] trim(String[] values, ValidationMetadata meta) {
        if (values != null && values.length > 0 && (meta == null || meta.trim())) {
            String[] copy = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null)
                    copy[i] = values[i].trim();
            }
            return copy;
        }
        else {
            return values;
        }
    }

    /**
     * An inner class that represents a "row" of form properties that all have the same index
     * so that we can validate all those properties together. 
     */
    protected static class Row extends HashMap<ParameterName, String[]> {
        private static final long serialVersionUID = 1L;

        private boolean hasNonEmptyValues = false;

        /**
         * Adds the value to the map, along the way checking to see if there are any non-null values for
         * the row so far.
         */
        @Override
        public String[] put(ParameterName key, String[] values) {
            if (!hasNonEmptyValues) {
                hasNonEmptyValues = (values != null) && (values.length > 0) && (values[0] != null)
                        && (values[0].trim().length() > 0);

            }
            return super.put(key, values);
        }

        /** Returns true if the row had any non-empty values in it, otherwise false. */
        public boolean hasNonEmptyValues() {
            return this.hasNonEmptyValues;
        }
    }
}
