/* Copyright 2009 Ben Gunter
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
package net.sourceforge.stripes.examples.bugzooky.ext;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.stripes.examples.bugzooky.biz.Person;
import net.sourceforge.stripes.format.Formatter;

/**
 * A {@link Formatter} that formats a {@link Person} object to text in one of several ways. The
 * default format simply returns the {@link Person}'s integer ID as a string. This serves as a
 * complement to {@link PersonTypeConverter}, which does the opposite. The "short" format type
 * returns the {@link Person}'s username. The "full" format type returns the person's name in a form
 * specified by the format pattern, where %F means first name, %L means last name, %U means username
 * and %E means email address.
 * 
 * @author Ben Gunter
 */
public class PersonFormatter implements Formatter<Person> {
    /** The default format pattern to use if no format pattern is specified. */
    private static final String DEFAULT_FORMAT_PATTTERN = "%L, %F (%U)";

    private String formatType, formatPattern;

    /** Format the {@link Person} object according to the format type and pattern. */
    public String format(Person person) {
        if (person == null) {
            return "";
        }
        else if ("short".equals(formatType)) {
            return checkNull(person.getUsername());
        }
        else if ("full".equals(formatType)) {
            Pattern pattern = Pattern.compile("%[EFLU]");
            String fp = formatPattern == null ? DEFAULT_FORMAT_PATTTERN : formatPattern;

            StringBuffer buf = new StringBuffer();
            Matcher matcher = pattern.matcher(fp);
            while (matcher.find()) {
                char spec = matcher.group().charAt(1);
                switch (spec) {
                case 'E':
                    matcher.appendReplacement(buf, checkNull(person.getEmail()));
                    break;
                case 'F':
                    matcher.appendReplacement(buf, checkNull(person.getFirstName()));
                    break;
                case 'L':
                    matcher.appendReplacement(buf, checkNull(person.getLastName()));
                    break;
                case 'U':
                    matcher.appendReplacement(buf, checkNull(person.getUsername()));
                    break;
                default:
                    buf.append(matcher.group());
                }
            }
            matcher.appendTail(buf);

            return buf.toString();
        }
        else {
            return String.valueOf(person.getId());
        }
    }

    protected String checkNull(String s) {
        return s == null ? "" : s;
    }

    /** Set the format type, which specifies the general format type: default (null), short or full. */
    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    /**
     * Set the format pattern to be used by the "full" format type. In this pattern, %F will be
     * replaced with the first name, %L by the last name, %U by the username and %E by the email
     * address.
     */
    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    /** This method is specified by the {@link Formatter} interface, but it is not used here. */
    public void init() {
    }

    /** This method is specified by the {@link Formatter} interface, but it is not used here. */
    public void setLocale(Locale locale) {
    }
}