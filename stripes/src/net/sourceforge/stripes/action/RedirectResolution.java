package net.sourceforge.stripes.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Resolution that uses the Servlet API to <em>redirect</em> the user to another path by issuing
 * a client side redirect. Unlike the ForwardResolution the RedirectResolution can send the user to
 * any URL anywhere on the web - though it is more commonly used to send the user to a location
 * within the same application.
 *
 * @see ForwardResolution
 * @author Tim Fennell
 */
public class RedirectResolution extends OnwardResolution implements Resolution {
    /**
     * Simple constructor that takes the URL to which to forward the user.
     * @param url the URL to which the user's browser should be re-directed.
     */
    public RedirectResolution(String url) {
        setPath(url);
    }

    /**
     * Attempts to redirect the user to the specified URL.
     *
     * @throws ServletException thrown when the Servlet container encounters an error
     * @throws IOException thrown when the Servlet container encounters an error
     */
    public void execute(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + getPath());
    }
}
