package net.sourceforge.stripes.exception;

import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 15, 2005 Time: 9:43:39 PM To change this
 * template use File | Settings | File Templates.
 */
public class StripesJspException extends JspException {
    public StripesJspException(String string) {
        super(string);
    }

    public StripesJspException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public StripesJspException(Throwable throwable) {
        super(throwable);
    }
}
