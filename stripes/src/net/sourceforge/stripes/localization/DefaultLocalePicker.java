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
package net.sourceforge.stripes.localization;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.nio.charset.Charset;

/**
 * <p>Default locale picker that uses a comma separated list of locales in the servlet init
 * parameters to determine the set of locales that are supported by the application.  Then at
 * request time matches the user's preference order list as specified by the headers included
 * in the request until it finds one of those locales in the system list.  If a match cannot be
 * found, the first locale in the system list will be picked.  If there is no list of configured
 * locales then the picker will default the list to a one entry list containing the system locale.</p>
 *
 * <p>Locales are hierarchical, with up to three levels designating language, country and
 * variant.  Only the first level (language) is required.  To provide the best match possible the
 * DefaultLocalePicker tracks the one-level matches, two-level matches and three-level matches. If
 * a three level match is found, it will be returned.  If not the first two-level match will be
 * returned if one was found.  If not, the first one-level match will be returned.  If not even a
 * one-level match is found, the first locale supported by the system is returned.</p>
 *
 * @author Tim Fennell
 */
public class DefaultLocalePicker implements LocalePicker {
    /**
     * The configuration parameter that is used to lookup a comma separated list of locales that
     * the system supports.
     */
    public static final String LOCALE_LIST = "LocalePicker.Locales";

    /** Log instance for use within the class. */
    private static final Log log = Log.getInstance(DefaultLocalePicker.class);

    /** Stores a reference to the configuration passed in at initialization. */
    protected Configuration configuration;

    /** Stores the configured set of Locales that the system supports, looked up at init time. */
    protected List<Locale> locales = new ArrayList<Locale>();

    /** Contains a map of Locale to preferred character encoding. */
    protected Map<Locale,String> encodings = new HashMap<Locale,String>();

    /**
     * Attempts to read the
     * @param configuration
     * @throws Exception
     */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;

        String configuredLocales =
                configuration.getBootstrapPropertyResolver().getProperty(LOCALE_LIST);

        if (configuredLocales == null || configuredLocales.equals("")) {
            log.info("No locale list specified, defaulting to single locale: ", Locale.getDefault());
            this.locales.add(Locale.getDefault());
        }
        else {
            // Split apart the Locales on commas, and then parse the local strings into their bits
            String[] localeStrings = StringUtil.standardSplit(configuredLocales);
            for (String localeString : localeStrings) {
                // Each locale string can be made up of two parts, locale:encoding
                // and the locale can be made up of up to three segment, e.g. en_US_PC

                String[] halves = localeString.split(":");
                String[] parts = halves[0].split("[-_]");
                Locale locale = null;

                if (parts.length == 1) {
                    locale = new Locale(parts[0].trim().toLowerCase());
                }
                else if (parts.length == 2) {
                    locale = new Locale(parts[0].trim().toLowerCase(),
                                        parts[1].trim().toUpperCase());
                }
                else if (parts.length == 3) {
                    locale =  new Locale(parts[0].trim().toLowerCase(),
                                         parts[1].trim().toUpperCase(),
                                         parts[2].trim());
                }
                else {
                    log.error("Configuration property ", LOCALE_LIST, " contained a locale value ",
                              "that split into more than three parts! The parts were: ", parts);
                }

                this.locales.add(locale);

                // Now check to see if a character encoding was specified, and if so is it valid
                if (halves.length == 2) {
                    String encoding = halves[1];

                    if (Charset.isSupported(encoding)) {
                        this.encodings.put(locale, halves[1]);
                    }
                    else {
                        log.error("Configuration property ", LOCALE_LIST, " contained a locale value ",
                                  "with an unsupported character encoding. The offending entry is: ",
                                  localeString);
                    }
                }
            }

            log.debug("Configured DefaultLocalePicker with locales: ", this.locales);
            log.debug("Configured DefaultLocalePicker with encodings: ", this.encodings);
        }
    }

    /**
     * Uses a prefence matching algorithm to pick a Locale for the user's request.  Iterates
     * through the user's acceptable list of Locales, mathing them against the system list. On the
     * way through the list records the first Locale to match on Language, and the first locale to
     * match on both Language and Country.  If a match is found for all three, Language, Country
     * and Variant, it will be returned.  If no three-way match is found the first two-way match
     * found will be returned.  If no two-way match way found the first one-way match found will
     * be returned.  If no one way match was found, the default system locale will be returned.
     *
     * @param request the request being processed
     * @return a Locale to use in processing the request
     */
    @SuppressWarnings("unchecked")
	public Locale pickLocale(HttpServletRequest request) {
        Locale oneWayMatch = null;
        Locale twoWayMatch= null;

        List<Locale> preferredLocales = Collections.list(request.getLocales());
        for (Locale preferredLocale : preferredLocales) {
            for (Locale systemLocale : this.locales) {

                if ( systemLocale.getLanguage().equals(preferredLocale.getLanguage()) ) {

                    // We have a language match, let's go for two!
                    oneWayMatch = (oneWayMatch == null ? systemLocale : oneWayMatch);
                    String systemCountry = systemLocale.getCountry();
                    String preferredCountry = preferredLocale.getCountry();

                    if ( (systemCountry == null && preferredCountry == null) ||
                         (systemCountry != null && systemCountry.equals(preferredCountry)) ) {

                        // Ooh, we have a two way match, can we make three?
                        twoWayMatch = (twoWayMatch == null ? systemLocale : twoWayMatch);
                        String systemVariant = systemLocale.getVariant();
                        String preferredVariant = preferredLocale.getVariant();

                        if ( (systemVariant == null && preferredVariant == null) ||
                                (systemVariant != null && systemVariant.equals(preferredVariant)) ) {
                            // Bingo!  You sunk my battleship!
                            return systemLocale;
                        }
                    }
                }
            }
        }

        // We didn't get a match complete match, maybe partial will do
        if (twoWayMatch != null) {
            return twoWayMatch;
        }
        else if (oneWayMatch != null) {
            return oneWayMatch;
        }
        else {
            return this.locales.get(0);
        }
    }

    /**
     * Returns the character encoding to use for the request and locale if one has been
     * specified in the configuration.  If no value has been specified, returns null.
     *
     * @param request the current request
     * @param locale the locale picked for the request
     * @return a valid character encoding or null
     */
    public String pickCharacterEncoding(HttpServletRequest request, Locale locale) {
        return this.encodings.get(locale);
    }
}
