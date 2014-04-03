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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Unit tests for the HtmlUtil class..
 */
public class HtmlUtilTest {

    @Test(groups="fast")
    public void testJoinAndSplit() throws Exception {
        String[] input = {"foo", "bar", "foobar"};
        List<String> listInput = Arrays.asList(input);

        String combined = HtmlUtil.combineValues(listInput);
        Collection<String> output = HtmlUtil.splitValues(combined);

        Assert.assertEquals(output.size(), listInput.size(), "Different number of params.");
        Assert.assertTrue(output.contains("foo"));
        Assert.assertTrue(output.contains("bar"));
        Assert.assertTrue(output.contains("foobar"));
    }

    @Test(groups="fast")
    public void testJoinWithNoStrings() throws Exception {
        String combined = HtmlUtil.combineValues(null);
        Assert.assertEquals(combined, "");

        combined = HtmlUtil.combineValues( new HashSet<String>() );
        Assert.assertEquals(combined, "");
    }

    @Test(groups="fast")
    public void testSplitWithNoValues() throws Exception {
        Collection<String> values = HtmlUtil.splitValues(null);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 0);

        values = HtmlUtil.splitValues("");
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 0);
    }
}
