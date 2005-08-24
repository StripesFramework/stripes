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

import net.sourceforge.stripes.validation.ValidationError;

import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagData;

/**
 * This tag extra info exposes index and error context variables for the body
 * of the errors tag.
 *
 * @author Greg Hinkle
 */
public class ErrorsTagExtraInfo extends TagExtraInfo {

    /** Returns an array of length two, for the variables exposed. */
    public VariableInfo[] getVariableInfo(TagData data) {
        VariableInfo[]  scriptVars = new VariableInfo[2];

        scriptVars[0] = new VariableInfo("index",
                                         "java.lang.Number",
                                         true,
                                         VariableInfo.NESTED);

        // TODO: ValidationError should expose properties like field name
        scriptVars[1] = new VariableInfo("error",
                                         ValidationError.class.getName(),
                                         true,
                                         VariableInfo.NESTED);

        return scriptVars;
    }
}
