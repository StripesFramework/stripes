package net.sourceforge.stripes.exception;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 15, 2005 Time: 9:41:59 PM To change this
 * template use File | Settings | File Templates.
 */
public class StripesException extends Exception {
    public StripesException(String message) {
        super(message);
    }

    public StripesException(String message, Throwable cause) {
        super(message, cause);
    }

    public StripesException(Throwable cause) {
        super(cause);
    }
}
