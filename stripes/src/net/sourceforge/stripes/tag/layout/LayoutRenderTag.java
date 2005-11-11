package net.sourceforge.stripes.tag.layout;

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.tag.StripesTagSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.DynamicAttributes;
import java.util.Stack;
import java.net.URL;

/**
 * Renders a named layout, optionally overriding one or more components in the layout. Any
 * attribute provided to the class other than 'name' will be placed into page context during
 * the evaluation of the layout, making them available to other tags, and in EL.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class LayoutRenderTag extends StripesTagSupport implements BodyTag, DynamicAttributes {
    private String name;
    private LayoutContext context = new LayoutContext();

    /** Gets the name of the layout to be used. */
    public String getName() { return name; }

    /** Sets the name of the layout to be used. */
    public void setName(String name) { this.name = name; }

    /** Used by the JSP container to provide the tag with dynamic attributes. */
    public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
        this.context.getParameters().put(localName, value);
    }

    /**
     * Allows nested tags to register their contents for rendering in the layout.
     *
     * @param name the name of the component to be overriden in the layout
     * @param contents the output that will be used
     */
    public void addComponent(String name, String contents) {
        this.context.getComponents().put(name, contents);
    }

    /**
     * Does nothing.
     * @return EVAL_BODY_BUFFERED in all cases
     */
    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    /**
     * Discards the body content since it is not used. Input from nested LayoutComponent tags is
     * captured through a different mechanism.
     */
    public void setBodyContent(BodyContent bodyContent) { /* Don't use it */ }

    /** Does nothing. */
    public void doInitBody() throws JspException { /* Do nothing. */ }

    /**
     * Does nothing.
     * @return SKIP_BODY in all cases.
     */
    public int doAfterBody() throws JspException { return SKIP_BODY; }

    /**
     * Invokes the named layout, providing it with the overriden components and provided
     * parameters.
     * @return EVAL_PAGE in all cases.
     * @throws JspException if any exceptions are encountered processing the request
     */
    public int doEndTag() throws JspException {
        try {
            HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();

            // Put the components into the request, for the definition tag to use.. using a stack
            // to allow for the same layout to be nested inside itself :o
            String attributeName = LayoutDefinitionTag.PREFIX + this.name;
            Stack<LayoutContext> stack =
                    (Stack<LayoutContext>) request.getAttribute(attributeName);
            if (stack == null) {
                stack = new Stack<LayoutContext>();
                request.setAttribute(attributeName, stack);
            }

            // Check that the page named is actually there, because some containers will
            // just quietly ignore includes of non-existent pages!
            URL target = request.getSession().getServletContext().getResource(this.name);
            if (target == null) {
                throw new StripesJspException(
                    "Attempt made to render a layout that does not exist. The layout name " +
                    "provided was '" + this.name + "'. Please check that a JSP exists at " +
                    "that location within your web application."
                );
            }

            stack.push(this.context);

            // Now wrap the JSPWriter, and include the target JSP
            BodyContent content = getPageContext().pushBody();
            getPageContext().include(this.name, false);
            getPageContext().popBody();
            getPageContext().getOut().write(content.getString());

            stack.pop();

            // Clean up in case the tag gets pooled
            this.context = new LayoutContext();
        }
        catch (StripesJspException sje) { throw sje; }
        catch (Exception e) {
            throw new StripesJspException(
                "An exception was raised while invoking a layout. The layout used was " +
                "'" + this.name + "'. The following information was supplied to the render " +
                "tag: " + this.context.toString(), e);
        }

        return EVAL_PAGE;
    }
}
