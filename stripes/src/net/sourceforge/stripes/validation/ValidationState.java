package net.sourceforge.stripes.validation;

/**
 * <p>Enumeration that describes the choices for when validation methods should be run. Allows
 * developers to choose between having their validation methods run always, only when there
 * are no errors, or to adopt the system default policy.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.3
 */
public enum ValidationState {
    /**
     * Specifies that validations should be applied in all conditions without regard for whether
     * there are pre-existing validation errors.
     */
    ALWAYS,

    /**
     * Specifies that validations should be applied only when there are no validation errors.
     */
    NO_ERRORS,

    /**
     * Specifies that the decision of whether or not the validation should be applied when
     * errors exist should be made by consulting the system level default. Stripes' default
     * for the system level value is equivelant to NO_ERRORS, but can be configured. See the
     * Stripes
     * <a href="http://stripes.mc4j.org/confluence/display/stripes/Configuration+Reference#ConfigurationReference-ValidationProperties">Configuration Reference</a>
     * for details.
     */
    DEFAULT
}
