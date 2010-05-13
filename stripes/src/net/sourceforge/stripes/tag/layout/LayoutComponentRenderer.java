/**
 * Created by Ben Gunter on May 12, 2010 at 2:01:33 PM.
 *
 * Copyright 2010 Comsquared Systems. All rights reserved.
 */
package net.sourceforge.stripes.tag.layout;

import java.io.IOException;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;

import net.sourceforge.stripes.exception.StripesRuntimeException;

/**
 * An object that can be stuffed into a scope (page, request, application, etc.) and render a layout
 * component to a string. This allows for use of EL expressions to output a component (as described
 * in the book <em>Stripes ... and web development is fun again</em>) without requiring that all
 * components be evaluated and buffered just in case a string representation is needed. The
 * evaluation happens only when necessary, saving cycles and memory.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5.4
 */
public class LayoutComponentRenderer {
    private LinkedList<PageContext> pageContext;
    private String componentName, layoutName;

    /**
     * Create a new instance to render the specified component tag to a string. The tag itself is
     * only used to get other information that is necessary to turn a component into a string, such
     * as layout name and component name.
     * 
     * @param tag The layout component to render.
     */
    public LayoutComponentRenderer(LayoutComponentTag tag) {
        this.layoutName = tag.getLayoutName();
        this.componentName = tag.getName();
    }

    /**
     * Push a new page context onto the page context stack. The last page context pushed onto the
     * stack is the one that will be used to evaluate the component tag's body.
     */
    public void pushPageContext(PageContext pageContext) {
        if (this.pageContext == null) {
            this.pageContext = new LinkedList<PageContext>();
        }

        this.pageContext.add(pageContext);
    }

    /** Pop the last page context off the stack and return it. */
    public PageContext popPageContext() {
        if (pageContext == null || pageContext.isEmpty())
            return null;
        else
            return pageContext.removeLast();
    }

    /** Get the last page context that was pushed onto the stack. */
    public PageContext getPageContext() {
        if (pageContext == null || pageContext.isEmpty())
            return null;
        else
            return pageContext.getLast();
    }

    @Override
    public String toString() {
        PageContext pageContext = getPageContext();
        LayoutContext context = LayoutContext.find(pageContext, layoutName);

        // Save the current component name so it can be restored when we're done
        String restore = context.getCurrentComponentName();
        context.setCurrentComponentName(componentName);

        try {
            BodyContent body = pageContext.pushBody();
            pageContext.include(context.getRenderPage());
            pageContext.popBody();
            return body.getString();
        }
        catch (ServletException e) {
            throw new StripesRuntimeException(e);
        }
        catch (IOException e) {
            throw new StripesRuntimeException(e);
        }
        finally {
            context.setCurrentComponentName(restore);
        }
    }
}
