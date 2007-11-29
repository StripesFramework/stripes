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

import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.exception.StripesRuntimeException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

/**
 * <p>A specialized type converter for converting a <b>single</b> input field/parameter value
 * into one or more Java objects contained in a <b>List</b>. Designed to handle the case where a user
 * is allowed to enter more than one value into a single field, separated by certain characters,
 * that should result in a number of Java objects being created.</p>
 *
 * <p>For example imagine a search field where a user is allowed to enter one or more numbers.
 * They might enter "2 4 8 16".  In this case the {@code OneToManyTypeConverter} will convert
 * this to a Collection of numbers with one entry for each of the four numbers shown above. The type
 * of number created ({@link java.lang.Integer}, {@link java.lang.Long} etc.) is inferred from the
 * declaration of the property on the {@code ActionBean}.  For example:</p>
 *
 *<pre>@Validate(converter=OneToManyTypeConverter.class) private List<Long> numbers;</pre>
 *
 * <p>would result in the numbers being converted to Longs as opposed to any other numeric type.</p>
 *
 * <p>The splitting of the input String is done using the {@link java.lang.String#split(String)}
 * method.  The regular expression passed to {@code split()} is obtained by calling
 * {@link #getSplitRegex()}.  By default the regular expression used will match an optional comma
 * followed by one or spaces (e.g. " " or ", " or " &nbsp; " etc.). This behaviour can easily be
 * modified by subclassing and overriding {@link #getSplitRegex()} to return a different
 * regular expression string.</p>
 *
 * <p>The individual components of the String are then converted using an appropriate
 * {@link TypeConverter} which is looked up using the {@link TypeConverterFactory}.  As a result
 * the {@code OneToManyTypeConverter} can be used to convert to a list of any type fo which
 * a {@code TypeConverter} has been registered.  If a usable {@code TypeConverter} cannot be
 * discovered then an Exception will be thrown!  However, if you wish to use the
 * {@code OneToManyTypeConverter} with a {@code TypeConverter} which is not registered as the
 * default converter for it's type you can override this behaviour by subclassing
 * this class and overriding {@link #getSingleItemTypeConverter(Class)}.</p>
 *
 * <p>Strictly speaking the {@code OneToManyTypeConverter} returns a {@link java.util.Collection}
 * of converted items. It does not have any way of inferring the collection type that should be
 * used, and so by default it will always return an instance of {@link java.util.List}. This
 * behaviour can easily be override by extending this class and overriding
 * {@link #getCollectionInstance()}.</p>
 *
 * <p>Note that the converter itself does not create any {@link ValidationError}s, but that
 * by using other {@link TypeConverter}s internally it is possible to produce one or more
 * errors per item split out of the input String!</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.2.2
 */
public class OneToManyTypeConverter implements TypeConverter<Object> {
    private Locale locale;

    public void setLocale(Locale locale) { this.locale = locale; }

    /**
     * Converts the supplied String into one or more objects is the manner described in
     * the class level JavaDoc. If any validation errors occur then {@code null} is returned
     * regardless of whether any items were successfully converted or not.
     *
     * @param input an input String containing one or more items to be converted in a single
     *        String
     * @param targetType the type that each individual item will be converted to
     * @param errors a collection of ValidationErrors that can be added to
     * @return a Collection containing one or more items of targetType, or null if any
     *         ValidationErrors occur.
     */
    @SuppressWarnings("unchecked")
	public Collection<? extends Object> convert(String input,
                                          Class<? extends Object> targetType,
                                          Collection<ValidationError> errors) {

        TypeConverter converter = getSingleItemTypeConverter(targetType);
        String[] splits = input.split( getSplitRegex() );
        Collection<Object> items = getCollectionInstance();

        for (String split : splits) {
            Object item = converter.convert(split, targetType, errors);
            if (item != null) {
                items.add(item);
            }
        }

        return items.size() > 0 ? items : null;
    }

    /**
     * Instantiates and returns a Collection of a type that can be set on ActionBeans using
     * this converter.  By default returns an instance of {@link java.util.List}.
     *
     * @return an instance of {@link java.util.List}
     */
    @SuppressWarnings("unchecked")
	public Collection getCollectionInstance() {
        return new LinkedList<Object>();
    }

    /**
     * Returns the String form of a regular expression that identifies the separator Strings
     * in the input String.  The default expression matches an optional comma followed by one
     * or more spaces.
     *
     * @return a regular expression matching an optional comma followed by one or more spaces.
     */
    protected String getSplitRegex() {
        return ",?[ ]+";
    }

    /**
     * Fetches an instance of {@link TypeConverter} that can be used to convert the individual
     * items split out of the input String. By default uses the {@link TypeConverterFactory} to
     * find an appropriate {@link TypeConverter}.
     *
     * @param targetType the type that each item should be converted to.
     * @return a TypeConverter for use in converting each individual item.
     */
    @SuppressWarnings("unchecked")
	protected TypeConverter getSingleItemTypeConverter(Class targetType) {
        try {
            TypeConverterFactory factory = StripesFilter.getConfiguration().getTypeConverterFactory();
            return factory.getTypeConverter(targetType, this.locale);
        }
        catch (Exception e) {
            throw new StripesRuntimeException(
                    "You are using the OneToManyTypeConverter to convert a String to a List of " +
                            "items for which there is no registered converter! Please check that the " +
                            "TypeConverterFactory knows how to make a converter for: " +
                            targetType, e
            );
        }
    }
}
