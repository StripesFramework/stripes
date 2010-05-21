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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.Tag;

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
    private LayoutDefinitionTag parentDefinitionTag;
    private LayoutRenderTag parentRenderTag;

    /** Get the context-relative path of the page that invoked this tag. */
    public String getCurrentPagePath() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String path = (String) request.getAttribute(StripesConstants.REQ_ATTR_INCLUDE_PATH);
        if (path == null)
            path = HttpUtil.getRequestedPath(request);
        return path;
    }

    /** True if this tag is executing as a child of the {@link LayoutDefinitionTag}. */
    public boolean isChildOfDefinition() {
        // Check for cached results
        if (this.parentDefinitionTag != null)
            return true;
        else if (this.parentRenderTag != null)
            return false;

        // Search the tag stack for the nearest layout tag ancestor
        for (Tag tag = getParent(); tag != null; tag = tag.getParent()) {
            if (tag instanceof LayoutDefinitionTag) {
                this.parentDefinitionTag = (LayoutDefinitionTag) tag;
                return true;
            }
            else if (tag instanceof LayoutRenderTag) {
                this.parentRenderTag = (LayoutRenderTag) tag;
                return false;
            }
        }

        return false;
    }

    /** True if this tag is executing as a child of the {@link LayoutRenderTag}. */
    public boolean isChildOfRender() {
        // Check for cached results
        if (this.parentRenderTag != null)
            return true;
        else if (this.parentDefinitionTag != null)
            return false;

        // Search the tag stack for the nearest layout tag ancestor
        for (Tag tag = getParent(); tag != null; tag = tag.getParent()) {
            if (tag instanceof LayoutDefinitionTag) {
                this.parentDefinitionTag = (LayoutDefinitionTag) tag;
                return false;
            }
            else if (tag instanceof LayoutRenderTag) {
                this.parentRenderTag = (LayoutRenderTag) tag;
                return true;
            }
        }

        return false;
    }

    /**
     * Get the nearest ancestor of this tag that is an instance of either
     * {@link LayoutDefinitionTag} or {@link LayoutRenderTag}. If no ancestor of either type is
     * found then null.
     */
    @SuppressWarnings("unchecked")
    public <T extends LayoutTag> T getLayoutAncestor() {
        if (isChildOfRender())
            return (T) parentRenderTag;
        else if (isChildOfDefinition())
            return (T) parentDefinitionTag;
        else
            return null;
    }
}
