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
package net.sourceforge.stripes.validation;

import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * <p>Encapsulates the validation metadata for a single property of a single class. Structure
 * is purposely very similar to the @Validate annotation. This class is used internally
 * to store and manipulate validation metadata, the source of which is often validation
 * annotations.</p>
 *
 * <p>However, since this class is not an annotation it has the added benefits of being
 * able to contain behaviour, being subclassable, and of being able to be instantiated at
 * runtime - i.e. it can contain non-static validation information.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.5
 */
public class ValidationMetadata {
    String property;
    boolean required;
    private Set<String> on;
    private boolean onIsPositive;
    private boolean ignore;
    private Integer minlength, maxlength;
    private Double minvalue, maxvalue;
    private Pattern mask;
    private String expression;
    Class<? extends TypeConverter> converter;

    /**
     * Constructs a ValidationMetadata object for the specified property. Further constraints
     * can be specified by calling individual methods, e.g.
     * {@code  new ValidationMetadata("username").minlength(5).maxlength(10);}
     *
     * @param property the name of the property to be validated. If the property is a nested
     *        property then the fully path should be included, e.g. {@code user.address.city}
     */
    public ValidationMetadata(String property) {
        this.property = property;
    }

    /**
     * Essentially a copy constructor that constructs a ValidationMetadata object from
     * an @Validate annotation declared on a property.
     *
     * @param validate
     */
    public ValidationMetadata(String property, Validate validate) {
        // Copy over all the simple values
        this.property = property;
        required(validate.required());
        ignore(validate.ignore());
        if (validate.minlength() != -1) minlength(validate.minlength());
        if (validate.maxlength() != -1) maxlength(validate.maxlength());
        if (validate.minvalue() != Double.MIN_VALUE) minvalue(validate.minvalue());
        if (validate.maxvalue() != Double.MAX_VALUE) maxvalue(validate.maxvalue());
        if (!"".equals(validate.mask())) mask(validate.mask());
        if (validate.converter() != TypeConverter.class) converter(validate.converter());
        if (!"".equals(validate.expression())) expression(validate.expression());
        if (validate.on().length > 0) on(validate.on());
    }

    /** Returns the name of the property this validation metadata represents. */
    public String getProperty() {
        return this.property;
    }

    /** Sets the required-ness of this field. True = required, false = not required. */
    public ValidationMetadata required(boolean required) {
        this.required = required;
        return this;
    }

    /** Returns true if the field in question is required. */
    public boolean required() { return this.required; }

    /** Returns true if the field is required when processing the specified event. */
    public boolean requiredOn(String event) {
        return this.required && (
                (this.on == null) ||
                (this.onIsPositive && this.on.contains(event)) ||
                (!this.onIsPositive && !this.on.contains(event))
            );
    }

    /** Sets whether or not this field should be ignored during binding and validation. */
    public ValidationMetadata ignore(boolean ignore) {
        this.ignore = ignore;
        return this;
    }

    /** Returns true if this field should be ignored in binding and validation. */
    public boolean ignore() { return this.ignore; }

    /** Sets the minimum acceptable length for property values. */
    public ValidationMetadata minlength(Integer minlength) {
        this.minlength = minlength;
        return this;
    }

    /** Returns the minimum acceptable length for values, or null if there is none. */
    public Integer minlength() { return this.minlength; }

    /** Sets the maximum acceptable length for property values. */
    public ValidationMetadata maxlength(Integer maxlength) {
        this.maxlength = maxlength;
        return this;
    }

    /** Returns the maximum acceptable length for values, or null if there is none. */
    public Integer maxlength() { return this.maxlength; }

    /** Sets the minimum acceptable <b>value</b> for numeric property values. */
    public ValidationMetadata minvalue(Double minvalue) {
        this.minvalue = minvalue;
        return this;
    }

    /** Returns the minimum acceptable value for numeric properties, or null if there is none. */
    public Double minvalue() { return this.minvalue; }

    /** Sets the maximum acceptable <b>value</b> for numeric property values. */
    public ValidationMetadata maxvalue(Double maxvalue) {
        this.maxvalue = maxvalue;
        return this;
    }

    /** Returns the maximum acceptable value for numeric properties, or null if there is none. */
    public Double maxvalue() { return this.maxvalue; }

    /** Sets the mask which the String form of the property must match. */
    public ValidationMetadata mask(String mask) {
        this.mask = Pattern.compile(mask);
        return this;
    }

    /** Returns the mask Pattern property values must match, or null if there is none. */
    public Pattern mask() { return this.mask; }

    /** Sets the overridden TypeConveter to use to convert values. */
    public ValidationMetadata converter(Class<? extends TypeConverter> converter) {
        this.converter = converter;
        return this;
    }

    /** Returns the overridden TypeConverter if there is one, or null. */
    public Class<? extends TypeConverter> converter() { return this.converter; }

    /** Sets the expression that should be used to validate values. */
    public ValidationMetadata expression(String expression) {
        this.expression = expression;
        if (!this.expression.startsWith("${")) this.expression = "${" + this.expression + "}";
        return this;
    }

    /** Returns the overridden TypeConverter if there is one, or null. */
    public String expression() { return this.expression; }

    /** Sets the set of events for which the field in question is required, if it is at all. */
    public ValidationMetadata on(String... on) {
        if (on.length == 0) {
            this.on = null;
        }
        else {
            this.on = new HashSet<String>();
            this.onIsPositive = !(on[0].charAt(0) == '!');
            for (String s : on) {
                if (this.onIsPositive) {
                    this.on.add(s);
                }
                else {
                    this.on.add(s.substring(1));
                }
            }
        }

        return this;
    }

    /**
     * Overidden toString() that only outputs the constraints that are specified by
     * the instance of validation metadata (i.e. omits nulls, defaults etc.)
     *
     * @return a human readable string form of the metadata
     */
    public String toString() {
        return "ValidationMetadata{" +
                (required ? "required=" + required : "") +
                (ignore   ? ", ignore=" + ignore : "" ) +
                (minlength != null ? ", minlength=" + minlength : "") +
                (maxlength != null ? ", maxlength=" + maxlength : "") +
                (minvalue != null ? ", minvalue=" + minvalue : "") +
                (maxvalue != null ? ", maxvalue=" + maxvalue : "") +
                (mask != null ? ", mask=" + mask : "" ) +
                (expression != null ? ", expression='" + expression + '\'' : "") +
                (converter != null ? ", converter=" + converter.getSimpleName() : "") +
                '}';
    }
}

