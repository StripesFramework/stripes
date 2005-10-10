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
package net.sourceforge.stripes.tag;

import org.apache.commons.logging.LogFactory;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

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

    /**
     * Generates a simple set of PropertyDescriptors for the HtmlTagSupport class.
     */
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
            LogFactory.getLog(HtmlTagSupportBeanInfo.class).fatal
                ("Could not contruct bean info for HtmlTagSupport. This is very bad.", e);

            return null;
        }
    }
}
