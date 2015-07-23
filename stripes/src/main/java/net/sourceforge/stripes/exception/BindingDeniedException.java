package net.sourceforge.stripes.exception;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.validation.Validate;

/**
 * Exception thrown when a client attempts to bind to an {@link ActionBean} property that is not allowed. This will
 * occur when using the {@link StrictBinding} annotation. If you intend to bind to the property, you should apply a
 * naked {@link Validate} annotation to the property.
 *
 * <p>
 * Currently, this will only be thrown if the Stripes configuration is in debug mode (see  {@link Configuration}). When
 * not in debug, a warning is logged.
 * </p>
 *
 * @since Stripes 1.6
 */
public class BindingDeniedException extends RuntimeException {
    public BindingDeniedException(String parameterName) {
        super("Binding denied for parameter [" + parameterName + "]. If you want to allow binding to this parameter, " +
                "use the @Validate annotation.");
    }
}
