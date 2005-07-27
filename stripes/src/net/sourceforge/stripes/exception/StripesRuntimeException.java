package net.sourceforge.stripes.exception;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 15, 2005 Time: 9:42:50 PM To change this
 * template use File | Settings | File Templates.
 */
public class StripesRuntimeException extends RuntimeException {
    public StripesRuntimeException(String message) {
        super(message);
    }

    public StripesRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public StripesRuntimeException(Throwable cause) {
        super(cause);
    }
}
