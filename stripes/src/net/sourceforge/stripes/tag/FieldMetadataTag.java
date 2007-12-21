package net.sourceforge.stripes.tag;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.bean.PropertyExpression;
import net.sourceforge.stripes.util.bean.PropertyExpressionEvaluation;
import net.sourceforge.stripes.validation.ValidationMetadata;
import net.sourceforge.stripes.validation.ValidationMetadataProvider;

/**
 * <p>Field metadata tag for use with the Stripes framework. Exposes field properties via JavaScript to
 * allow client side validation. If this tag has a body it will be wrapped with JavaScript tags for
 * convenience.</p>
 * 
 * @author Aaron Porter
 * 
 */
public class FieldMetadataTag extends HtmlTagSupport implements BodyTag {
    /** Log used to log error and debugging information for this class. */
    private static final Log log = Log.getInstance(FormTag.class);

    /** Name of variable to hold metadata. */
    private String var;
    /** Optional comma separated list of additional fields to expose. */
    private String fields;
    /** Set to true to include type information for all fields. */
    private boolean includeType = false;
    /** Set to true to include the fully qualified class name for all fields. */
    private boolean fqn = false;
    /** Stores the value of the action attribute before the context gets appended. */
    private String actionWithoutContext;
    
    public FormTag getForm() {
        return getParentTag(FormTag.class);
    }

    /**
     * Builds a string that contains field metadata in a JavaScript object.
     * 
     * @return JavaScript object containing field metadata
     */
    private String getMetadata() {
        ActionBean bean = null;

        String action = getAction();

        FormTag form = getForm();

        if (form != null) {
            if (action != null)
                log.warn("Parameters action and/or beanclass specified but field-metadata tag is inside of a Stripes form tag. The bean will be pulled from the form tag.");
            
            action = form.getAction();
        }

        if (form != null)
            bean = form.getActionBean();

        Class<? extends ActionBean> beanClass = null;

        if (bean != null)
            beanClass = bean.getClass();
        else if (action != null) {
            beanClass = StripesFilter.getConfiguration().getActionResolver().getActionBeanType(action);

            try {
                bean = beanClass.newInstance();
            }
            catch (Exception e) {
                log.error(e);
                return null;
            }
        }

        if (beanClass == null) {
            log.error("Couldn't determine ActionBean class from FormTag! One of the following conditions must be met:\r\n\t",
                        "1. Include this tag inside of a stripes:form tag\r\n\t",
                        "2. Use the action parameter\r\n\t",
                        "3. Use the beanclass parameter");
            return null;
        }

        ValidationMetadataProvider metadataProvider = StripesFilter.getConfiguration()
                .getValidationMetadataProvider();

        if (metadataProvider == null) {
            log.error("Couldn't get ValidationMetadataProvider!");
            return null;
        }

        Map<String, ValidationMetadata> metadata = metadataProvider
                .getValidationMetadata(beanClass);

        StringBuilder sb = new StringBuilder("{\r\n\t\t");

        Set<String> fields = new HashSet<String>();
        
        if (form != null)
            fields.addAll(form.getRegisteredFields());

        if ((this.fields != null) && (this.fields.trim().length() > 0))
            fields.addAll(Arrays.asList(this.fields.split(",")));
        else if (form == null) {
            log.error("Fields attribute is required when field-metadata tag isn't inside of a Stripes form tag.");
            return null;
        }

        boolean first = true;
        
        for (String field : fields) {

            PropertyExpressionEvaluation eval = null;
            
            try {
                eval = new PropertyExpressionEvaluation(PropertyExpression.getExpression(field), bean);
            }
            catch (Exception e) {
                continue;
            }

            Class<?> fieldType = eval.getType();

            ValidationMetadata data = metadata.get(field);

            StringBuilder fieldInfo = new StringBuilder();

            if (fieldType.isPrimitive() || Number.class.isAssignableFrom(fieldType)
                    || Date.class.isAssignableFrom(fieldType) || includeType)
                fieldInfo.append("type:")
                        .append("'")
                        .append(fqn ? fieldType.getName() : fieldType.getSimpleName())
                        .append("'");
            
            Class<?> typeConverterClass = null;
            
            if (data != null) {
                if (data.encrypted())
                    fieldInfo.append(fieldInfo.length() > 0 ? "," : "").append("encrypted:")
                            .append(data.encrypted());
                if (data.required())
                    fieldInfo.append(fieldInfo.length() > 0 ? "," : "").append("required:").append(
                            data.required());
                if (data.mask() != null)
                    fieldInfo.append(fieldInfo.length() > 0 ? "," : "").append("mask:")
                            .append("/^").append(data.mask()).append("$/");
                if (data.minlength() != null)
                    fieldInfo.append(fieldInfo.length() > 0 ? "," : "").append("minlength:")
                            .append(data.minlength());
                if (data.maxlength() != null)
                    fieldInfo.append(fieldInfo.length() > 0 ? "," : "").append("maxlength:")
                            .append(data.maxlength());
                if (data.minvalue() != null)
                    fieldInfo.append(fieldInfo.length() > 0 ? "," : "").append("minvalue:").append(
                            data.minvalue());
                if (data.maxvalue() != null)
                    fieldInfo.append(fieldInfo.length() > 0 ? "," : "").append("maxvalue:").append(
                            data.maxvalue());
                
                typeConverterClass = data.converter();
            }

            // If we couldn't get the converter from the validation annotation
            // try to get it from the TypeConverterFactory
            if (typeConverterClass == null) {
                try {
                    typeConverterClass = StripesFilter.getConfiguration().getTypeConverterFactory()
                            .getTypeConverter(fieldType, pageContext.getRequest().getLocale())
                            .getClass();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (typeConverterClass != null) {
                fieldInfo.append(fieldInfo.length() > 0 ? "," : "")
                        .append("typeConverter:")
                        .append("'")
                        .append(fqn ? typeConverterClass.getName() : typeConverterClass.getSimpleName())
                        .append("'");
            }


            if (fieldInfo.length() > 0) {
                if (first)
                    first = false;
                else
                    sb.append(",\r\n\t\t");

                sb.append("'").append(field).append("':{");

                sb.append(fieldInfo);

                sb.append("}");
            }
        }

        sb.append("\r\n\t}");

        return sb.toString();
    }

    public FieldMetadataTag() {
        getAttributes().put("type", "text/javascript");
    }

    public void doInitBody() throws JspException {
    }

    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    @Override
    public int doStartTag() throws JspException {
        getPageContext().setAttribute(getVar(), new Var(getMetadata()));
        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag() throws JspException {
        JspWriter writer = getPageContext().getOut();

        String body = getBodyContentAsString();

        if (body != null) {
            try {
                String contentType = getPageContext().getResponse().getContentType();
                
                // Catches application/x-javascript, text/javascript, and text/ecmascript
                boolean pageIsScript = contentType != null && contentType.toLowerCase().contains("ascript");
                
                // Don't write the script tags if this page is a script
                if (!pageIsScript) {
                    writeOpenTag(writer, "script");
                    writer.write("//<![CDATA[\r\n");
                }

                writer.write(body);

                if (!pageIsScript) {
                    writer.write("\r\n//]]>");
                    writeCloseTag(writer, "script");
                }
            }
            catch (IOException ioe) {
                throw new StripesJspException("IOException while writing output in LinkTag.", ioe);
            }
        }

        return SKIP_BODY;
    }

    public String getVar() {
        return var;
    }

    /**
     * Sets the name of the variable to hold metadata.
     * 
     * @param var the name of the attribute that will contain field metadata
     */
    public void setVar(String var) {
        this.var = var;
    }

    public String getFields() {
        return fields;
    }

    /**
     * Optional comma separated list of additional fields to expose. Any fields that have
     * already been added to the Stripes form tag will automatically be included.
     * 
     * @param fields comma separated list of field names
     */
    public void setFields(String fields) {
        this.fields = fields;
    }

    public boolean isIncludeType() {
        return includeType;
    }

    /**
     * Set to true to include type information for all fields. By default, type information is only
     * included for primitives, numbers, and dates.
     * 
     * @param includeType include type info for all fields
     */
    public void setIncludeType(boolean includeType) {
        this.includeType = includeType;
    }

    public boolean isFqn() {
        return fqn;
    }

    /**
     * Set to true to include the fully qualified class name for all fields.
     * 
     * @param fqn include fully qualified class name for all fields
     */
    public void setFqn(boolean fqn) {
        this.fqn = fqn;
    }

    /**
     * Sets the action for the form. If the form action begins with a slash, and does not already
     * contain the context path, then the context path of the web application will get prepended to
     * the action before it is set. In general actions should be specified as &quot;absolute&quot;
     * paths within the web application, therefore allowing them to function correctly regardless of
     * the address currently shown in the browser&apos;s address bar.
     * 
     * @param action the action path, relative to the root of the web application
     */
    public void setAction(String action) {
        // Use the action resolver to figure out what the appropriate URL binding if for
        // this path and use that if there is one, otherwise just use the action passed in
        String binding = StripesFilter.getConfiguration().getActionResolver()
                .getUrlBindingFromPath(action);
        if (binding != null) {
            this.actionWithoutContext = binding;
        }
        else {
            this.actionWithoutContext = action;
        }
    }

    public String getAction() {
        return this.actionWithoutContext;
    }

    /**
     * Sets the 'action' attribute by inspecting the bean class provided and asking the current
     * ActionResolver what the appropriate URL is.
     * 
     * @param beanclass the String FQN of the class, or a Class representing the class
     * @throws StripesJspException if the URL cannot be determined for any reason, most likely
     *             because of a mis-spelled class name, or a class that's not an ActionBean
     */
    public void setBeanclass(Object beanclass) throws StripesJspException {
        String url = getActionBeanUrl(beanclass);
        if (url == null) {
            throw new StripesJspException(
                    "Could not determine action from 'beanclass' supplied. "
                            + "The value supplied was '"
                            + beanclass
                            + "'. Please ensure that this bean type "
                            + "exists and is in the classpath. If you are developing a page and the ActionBean "
                            + "does not yet exist, consider using the 'action' attribute instead for now.");
        }
        else {
            setAction(url);
        }
    }

    /** Corresponding getter for 'beanclass', will always return null. */
    public Object getBeanclass() {
        return null;
    }
    /**
     * This is what is placed into the request attribute. It allows us to
     * get the field metadata as well as the form id.
     */
    public class Var {
        private String fieldMetadata, formId;

        private Var(String fieldMetadata) {
            this.fieldMetadata = fieldMetadata;
            FormTag form = getForm();
            if (form.getId() == null)
                form.setId("stripes-" + new Random().nextInt());
            this.formId = form.getId();
        }

        public String toString() {
            return fieldMetadata;
        }

        public String getFormId() {
            return formId;
        }
    }
}
