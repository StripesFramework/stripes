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
package net.sourceforge.stripes.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.HashMap;

/**
 * Simple set of tests for the UrlBuilder class.
 *
 * @author Tim Fennell
 */
public class UrlBuilderTest {

    @Test(groups="fast")
    public void testBasicUrl() throws Exception {
        String path = "/test/page.jsp";
        UrlBuilder builder = new UrlBuilder(path);
        String result = builder.toString();
        Assert.assertEquals(result, path);
    }

    @Test(groups="fast")
    public void testUrlWithParameters() throws Exception {
        String path = "/test/page.jsp";
        UrlBuilder builder = new UrlBuilder(path);
        builder.addParameter("one", 1);
        builder.addParameter("two", 2);
        builder.addParameter("three", 3);
        String result = builder.toString();
        Assert.assertEquals(result, "/test/page.jsp?one=1&two=2&three=3");
    }

    @Test(groups="fast")
    public void testUrlWithParameterVarargs() throws Exception {
        String path = "/test/page.jsp";
        UrlBuilder builder = new UrlBuilder(path);
        builder.addParameter("one", 1, "one", "uno", "i");
        String result = builder.toString();
        Assert.assertEquals(result, "/test/page.jsp?one=1&one=one&one=uno&one=i");
    }

    @Test(groups="fast")
    public void testUrlWithValuelessParameter() throws Exception {
        String path = "/test/page.jsp";
        UrlBuilder builder = new UrlBuilder(path);
        builder.addParameter("one", null);
        builder.addParameter("two", "");
        builder.addParameter("three");
        String result = builder.toString();
        Assert.assertEquals(result, "/test/page.jsp?one=&two=&three=");
    }

    @Test(groups="fast")
    public void testUrlWithParameterArray() throws Exception {
        String path = "/test/page.jsp";
        UrlBuilder builder = new UrlBuilder(path);
        builder.addParameter("one", Literal.array("1", "one", "uno", "i") );
        String result = builder.toString();
        Assert.assertEquals(result, "/test/page.jsp?one=1&one=one&one=uno&one=i");
    }

    @Test(groups="fast")
    public void testUrlWithParameterCollection() throws Exception {
        String path = "/test/page.jsp";
        UrlBuilder builder = new UrlBuilder(path);

        builder.addParameter("one", Literal.list("1", "one", "uno", "i") );
        String result = builder.toString();
        Assert.assertEquals(result, "/test/page.jsp?one=1&one=one&one=uno&one=i");
    }

    @Test(groups="fast")
    public void testUrlWithParameterMap() throws Exception {
        String path = "/test/page.jsp";
        UrlBuilder builder = new UrlBuilder(path);
        Map map = new HashMap<Object,Object>();
        map.put("one", "one");
        map.put("two", Literal.list("2", "two"));
        map.put("three", Literal.array("3", "three"));

        builder.addParameters(map);
        String result = builder.toString();
        Assert.assertEquals(result,
                            "/test/page.jsp?one=one&two=2&two=two&three=3&three=three");
    }
}
