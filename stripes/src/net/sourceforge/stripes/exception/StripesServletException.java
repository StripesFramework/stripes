package net.sourceforge.stripes.exception;

import javax.servlet.ServletException;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 15, 2005 Time: 9:43:39 PM To change this
 * template use File | Settings | File Templates.
 */
public class StripesServletException extends ServletException {
    public StripesServletException(String string) {
        super(string);
    }

    public StripesServletException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public StripesServletException(Throwable throwable) {
        super(throwable);
    }
}
