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
package net.sourceforge.stripes.format;

import net.sourceforge.stripes.exception.StripesRuntimeException;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * <p>Implements a basic formatter for Date objects.  Accepts several known types and patterns, as
 * well as arbitrary patterns.  Under the covers uses DateFormat and SimpleDateFormat objects
 * from the java.text package - it is advised that you become familiar with these classes before
 * attempting to use custom patterns.</p>
 *
 * <p>Format types affect the kind of information that is output.  The supported format types
 * are (values are not case sensitive):</p>
 * <ul>
 *   <li>date</li>
 *   <li>time</li>
 *   <li>datetime</li>
 * </ul>
 *
 * <p>Format strings affect the format of the selected output. One of the following known values
 * may be supplied as the format string (named values are not case sensitive). If the value is not
 * one of the following, it is passed to SimpleDateFormat as a pattern string.
 * <ul>
 *   <li>short</li>
 *   <li>medium</li>
 *   <li>long</li>
 *   <li>full</li>
 * </ul>
 *
 * @author Tim Fennell
 */
public class DateFormatter implements Formatter<Date> {
    /** Maintains a map of named formats that can be used instead of patterns. */
    protected static final Map<String,Integer> namedPatterns = new HashMap<String,Integer>();

    static {
        namedPatterns.put("short",  DateFormat.SHORT );
        namedPatterns.put("medium", DateFormat.MEDIUM);
        namedPatterns.put("long",   DateFormat.LONG  );
        namedPatterns.put("full",   DateFormat.FULL  );
    }

    private String formatType;
    private String formatPattern;
    private Locale locale;
    private DateFormat format;

    /** Sets the format type to be used to render dates as Strings. */
    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    /** Sets the named format string or date pattern to use to format the date. */
    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    /** Sets the locale that output String should be in. */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Constructs the DateFormat used for formatting, based on the values passed to the
     * various setter methods on the class.  If the formatString is one of the named formats
     * then a DateFormat instance is created of the specified type and format, otherwise
     * a SimpleDateFormat is constructed using the pattern provided and the formatType is ignored.
     *
     * @throws StripesRuntimeException if the formatType is not one of 'date', 'time' or 'datetime'.
     */
    public void init() {
        // Default these values if they were not supplied
        if (formatPattern == null) {
            formatPattern = "short";
        }

        if (formatType == null) {
            formatType = "date";
        }

        String lcFormatString = formatPattern.toLowerCase();
        String lcFormatType  = formatType.toLowerCase();

        // Now figure out how to construct our date format for our locale
        if ( namedPatterns.containsKey(lcFormatString) ) {

            if (lcFormatType.equals("date")) {
                format = DateFormat.getDateInstance(namedPatterns.get(lcFormatString), locale);
            }
            else if (lcFormatType.equals("datetime")) {
                format = DateFormat.getDateTimeInstance(namedPatterns.get(lcFormatString),
                                                        namedPatterns.get(lcFormatString),
                                                        locale);
            }
            else if (lcFormatType.equals("time")) {
                format = DateFormat.getTimeInstance(namedPatterns.get(lcFormatString));
            }
            else {
                throw new StripesRuntimeException("Invalid formatType for Date: " + formatType +
                ". Allowed types are 'date', 'time' and 'datetime'.");
            }
        }
        else {
            format = new SimpleDateFormat(formatPattern, locale);
        }
    }

    /** Formats a Date as a String using the rules supplied when the formatter was built. */
    public String format(Date input) {
        return this.format.format(input);
    }
}
