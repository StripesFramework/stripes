package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.util.OgnlUtil;
import net.sourceforge.stripes.exception.StripesJspException;
import ognl.OgnlException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import java.util.Collection;

/**
 *
 */
public class InputOptionsCollectionTag extends HtmlTagSupport implements Tag {
    private Collection collection;

    public int doStartTag() throws JspException {
        // Locate the collection and
        String collectionName = getAttributes().remove("collection");
        Collection collection = evaluateExpression(collectionName, Collection.class);

        // Evaluate the rest of the attributes before we go any further
        evaluateExpressions();
        String labelProperty = getLabel();
        String valueProperty = getValue();

        InputOptionTag tag = new InputOptionTag();
        tag.setParent(this);
        tag.setPageContext(getPageContext());
        tag.getAttributes().putAll(getAttributes());

        try {
            for (Object item : collection) {
                Object label = OgnlUtil.getValue(labelProperty, item);
                Object value = OgnlUtil.getValue(valueProperty, item);

                tag.setLabel(label.toString());
                tag.setValue(value.toString());
                tag.doStartTag();
                tag.doInitBody();
                tag.doAfterBody();
                tag.doEndTag();
            }
        }
        catch (OgnlException oe) {
            throw new StripesJspException("A problem occurred generating an options-collection. " +
                "Most likely either [" + labelProperty + "] or ["+ valueProperty + "] is not a " +
                "valid property of the beans in the collection: " + collection);
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    /** Sets the EL Expression representing the collection that will be used to generate options. */
    public void setCollection(String collection) {
        set("collection", collection);
    }

    /** Gets the EL Expression representing the collection that will be used to generate options. */
    public String getCollection() {
        return get("collection");
    }

    /** Sets the name of the property that will be used to generate the option value. */
    public void setValue(String value) {
        set("value", value);
    }

    /** Gets the name of the property that will be used to generate the option value. */
    public String getValue() {
        return get("value");
    }

    /** Sets the name of the property that will be used to generate the option's label. */
    public void setLabel(String label) {
        set("label", label);
    }

    /** Gets the name of the property that will be used to generate the option's label. */
    public String getLabel() {
        return get("label");
    }

    /** Sets a single value that should be selected, in the generated set of options. */
    public void setSelected(String selected) { set("selected", selected); }

    /** Gets a single value that should be selected, in the generated set of options. */
    public String getSelected() { return get("selected"); }


}
