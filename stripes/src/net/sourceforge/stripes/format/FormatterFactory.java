package net.sourceforge.stripes.format;

import net.sourceforge.stripes.config.ConfigurableComponent;

import java.util.Locale;

/**
 * Interface for creating instances of formatter classes that are capable of formatting
 * the types specified into Strings.
 *
 * @see Formatter
 * @author Tim Fennell
 */
public interface FormatterFactory extends ConfigurableComponent {

    /**
     * Returns a configured formatter that meets the criteria specified.  The formatter is ready
     * for use as soon as it is returned from this method.
     *
     * @param clazz the type of object being formatted
     * @param locale the Locale into which the object should be formatted
     * @param formatType the manner in which the object should be formatted (allows nulls)
     * @param formatPattern the named format, or format pattern to be applied (allows nulls)
     * @return Formatter an instance of a Formatter, or null if no Formatter is available for
     *         the type specified
     */
    Formatter getFormatter(Class clazz, Locale locale, String formatType, String formatPattern);
}
