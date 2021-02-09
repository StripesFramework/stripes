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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;


public class HtmlUtilTest {

   @Test
   public void testJoinAndSplit() {
      String[] input = { "foo", "bar", "foobar" };
      List<String> listInput = Arrays.asList(input);

      String combined = HtmlUtil.combineValues(listInput);
      Collection<String> output = HtmlUtil.splitValues(combined);

      assertThat(output).containsExactly("foo", "bar", "foobar");
   }

   @Test
   public void testJoinWithNoStrings() {
      String combined = HtmlUtil.combineValues(null);
      assertThat(combined).isEmpty();

      combined = HtmlUtil.combineValues(new HashSet<>());
      assertThat(combined).isEmpty();
   }

   @Test
   public void testSplitWithNoValues() {
      Collection<String> values = HtmlUtil.splitValues(null);
      assertThat(values).isNotNull();
      assertThat(values).isEmpty();

      values = HtmlUtil.splitValues("");
      assertThat(values).isNotNull();
      assertThat(values).isEmpty();
   }
}
