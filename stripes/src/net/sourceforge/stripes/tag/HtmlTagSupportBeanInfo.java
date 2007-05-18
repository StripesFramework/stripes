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
package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.util.Log;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Descirbes the properties supported by the HtmlTagSupport class which is the parent of all the
 * HTML Form/Input tags in Stripes.  Exists to provide some flexibility in the naming of methods
 * and primarily to provide support for the &quot;class&quot; tag attribute in JSP containers that
 * demand a javabean compliant getter and setter method.  Since getClass() is rather special in Java
 * and cannot (and should not) be overriden, containers may not like calling setClass(String)
 * without there being a corresponding getClass():String method.  So the PropertyDescriptor for
 * the &quot;class&quot; property specifies the methods getCssClass() and setCssClass.</p>
 *
 * @author Tim Fennell
 */
public class HtmlTagSupportBeanInfo extends SimpleBeanInfo {
    private static final Log log = Log.getInstance(HtmlTagSupportBeanInfo.class);

    /**
     * Generates a simple set of PropertyDescriptors for the HtmlTagSupport class.
     */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();

            // Add the tricky one first
            Method getter = HtmlTagSupport.class.getMethod("getCssClass");
            Method setter = HtmlTagSupport.class.getMethod("setCssClass", String.class);
            descriptors.add( new PropertyDescriptor("class", getter, setter) );

            // Now do all the vanilla properties
            descriptors.add( new PropertyDescriptor("id",          HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("title",       HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("style",       HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("dir",         HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("lang",        HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("tabindex",    HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("accesskey",   HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onfocus",     HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onblur",      HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onselect",    HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onchange",    HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onclick",     HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("ondblclick",  HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onmousedown", HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onmouseup",   HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onmouseover", HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onmousemove", HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onmouseout",  HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onkeypress",  HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onkeydown",   HtmlTagSupport.class) );
            descriptors.add( new PropertyDescriptor("onkeyup",     HtmlTagSupport.class) );

            PropertyDescriptor[] array = new PropertyDescriptor[descriptors.size()];
            return descriptors.toArray(array);
        }
        catch (Exception e) {
            // This is crazy talk, we're only doing things that should always succeed
            log.fatal(e, "Could not contruct bean info for HtmlTagSupport. This is very bad.");
            return null;
        }
    }
}
