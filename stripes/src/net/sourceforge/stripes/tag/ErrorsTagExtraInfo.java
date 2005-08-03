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
