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
    @Override
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
