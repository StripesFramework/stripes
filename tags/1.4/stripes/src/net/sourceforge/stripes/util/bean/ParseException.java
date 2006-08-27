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
package net.sourceforge.stripes.util.bean;

/**
 * Exception used to designate that an expression was invalid and could not be parsed. The
 * exception will contain the offending expression (accessible via getExpression()) as well
 * as a String message explaining why it was not parsable.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class ParseException extends ExpressionException {
    private String expression;

    /** Constructs an exception with the supplied expression and message. */
    public ParseException(String expression, String message) {
        super(message + " Expression: " + expression);
        this.expression = expression;
    }

    /** Fetches the expression which caused the parsing failure. */
    public String getExpression() {
        return expression;
    }
}
