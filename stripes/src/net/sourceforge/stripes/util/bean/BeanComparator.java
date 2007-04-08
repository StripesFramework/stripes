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

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * <p>A comparator which compares objects based on one or more bean properties. Nested properties
 * are fully supported.  If a property is non-String and implements {@link Comparable} then the
 * {@code compareTo()} method is delegated to.  Otherwise the property is converted to a String
 * and a {@link Locale} aware {@link Collator} is used to to compare property values.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.5
 */
public class BeanComparator implements Comparator<Object> {
    private Locale locale;
    private PropertyExpression[] expressions;

    /**
     * Constructs a BeanComparator for comparing beans based on the supplied set of properties,
     * using the default system Locale to collate Strings.
     *
     * @param properties one or more property names to be used, in order, to sort beans
     */
    public BeanComparator(String... properties) {
        this(Locale.getDefault(), properties);
    }

    /**
     * Constructs a BeanComparator for comparing beans based on the supplied set of properties,
     * using the supplied Locale to collate Strings.
     *
     * @param locale the Locale to be used for collating Strings
     * @param properties one or more property names to be used, in order, to sort beans
     */
    public BeanComparator(Locale locale, String... properties) {
        this.locale = locale;
        this.expressions = new PropertyExpression[properties.length];

        for (int i=0; i<properties.length; ++i) {
            this.expressions[i] = PropertyExpression.getExpression(properties[i]);
        }
    }

    /**
     * <p>Compares two JavaBeans for order. Returns a negative integer, zero, or a positive
     * integer as the first argument sorts earlier, equal to, or later than the second.</p>
     *
     * <p>Iterates through the properties supplied in the constructor comparing the values of
     * each property for the two beans.  As soon as a property is found that supplied a non-equal
     * ordering, the ordering is returned. If all properties are equal, will return 0.</p>
     *
     * @param o1 the first object to be compared, must not be null.
     * @param o2 the second object to be compared, must not be null.
     * @return a negative integer, zero, or a positive integer as the first argument is less than,
     *         equal to, or greater than the second.
     * @throws ClassCastException if the arguments' types, or the types of the properties,
     *         prevent them from being compared by this Comparator.
     */
    @SuppressWarnings("unchecked")
	public int compare(Object o1, Object o2) {
        int retval = 0;
        Collator collator = Collator.getInstance(this.locale);

        for (PropertyExpression expression : this.expressions) {
            PropertyExpressionEvaluation e1 = new PropertyExpressionEvaluation(expression, o1);
            PropertyExpressionEvaluation e2 = new PropertyExpressionEvaluation(expression, o2);

            Object prop1 = e1.getValue();
            Object prop2 = e2.getValue();

            if (prop1 == null && prop2 == null) {
                retval = 0;
            }
            else if (prop1 == null) {
                retval = 1;
            }
            else if (prop2 == null) {
                retval = -1;
            }
            else if ( !(prop1 instanceof String) && prop1 instanceof Comparable) {
                retval = ((Comparable) prop1).compareTo(prop2);
            }
            else {
                String string1 = prop1.toString();
                String string2 = prop2.toString();
                retval = collator.compare(string1, string2);
            }

            if (retval != 0) break;
        }

        return retval;
    }
}
