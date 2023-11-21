/* Copyright 2010 Ben Gunter
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
package net.sourceforge.stripes.tag.layout;

import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.tag.StripesTagSupport;
import net.sourceforge.stripes.util.HttpUtil;

/**
 * Abstract base class for the tags that handle rendering of layouts.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5.4
 */
public abstract class LayoutTag extends StripesTagSupport {
    /** Get the context-relative path of the page that invoked this tag. */
    public String getCurrentPagePath() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String path = (String) request.getAttribute(StripesConstants.REQ_ATTR_INCLUDE_PATH);
        if (path == null)
            path = HttpUtil.getRequestedPath(request);
        return path;
    }

    /**
     * True if the nearest ancestor of this tag that is an instance of {@link LayoutTag} is also an
     * instance of {@link LayoutRenderTag}.
     */
    public boolean isChildOfRender() {
        LayoutTag parent = getLayoutParent();
        return parent instanceof LayoutRenderTag;
    }

    /**
     * True if the nearest ancestor of this tag that is an instance of {@link LayoutTag} is also an
     * instance of {@link LayoutDefinitionTag}.
     */
    public boolean isChildOfDefinition() {
        LayoutTag parent = getLayoutParent();
        return parent instanceof LayoutDefinitionTag;
    }

    /**
     * True if the nearest ancestor of this tag that is an instance of {@link LayoutTag} is also an
     * instance of {@link LayoutComponentTag}.
     */
    public boolean isChildOfComponent() {
        LayoutTag parent = getLayoutParent();
        return parent instanceof LayoutComponentTag;
    }

    /**
     * Get the nearest ancestor of this tag that is an instance of {@link LayoutTag}. If no ancestor
     * of that type is found then null.
     */
    @SuppressWarnings("unchecked")
    public <T extends LayoutTag> T getLayoutParent() {
        return (T) getParentTag(LayoutTag.class);
    }

    /**
     * Starting from the outer-most context and working up the stack, put a reference to each
     * component renderer by name into the page context and push this tag's page context onto the
     * renderer's page context stack. Working from the bottom of the stack up ensures that newly
     * defined components override any that might have been defined previously by the same name.
     */
    public void exportComponentRenderers() {
        for (LayoutContext c = LayoutContext.lookup(pageContext).getFirst(); c != null; c = c.getNext()) {
            for (Entry<String, LayoutComponentRenderer> entry : c.getComponents().entrySet()) {
                entry.getValue().pushPageContext(pageContext);
                pageContext.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    /** Pop this tag's page context off each of the component renderers' page context stacks. */
    public void cleanUpComponentRenderers() {
        for (LayoutContext c = LayoutContext.lookup(pageContext).getLast(); c != null; c = c.getPrevious()) {
            for (LayoutComponentRenderer renderer : c.getComponents().values()) {
                renderer.popPageContext();
            }
        }
    }
}
