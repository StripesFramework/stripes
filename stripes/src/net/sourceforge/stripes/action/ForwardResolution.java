package net.sourceforge.stripes.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Resolution that uses the Servlet API to <em>forward</em> the user to another path within the
 * same web application using a server side forward.
 *
 * @see RedirectResolution
 * @author Tim Fennell
 */
public class ForwardResolution extends OnwardResolution implements Resolution {
    /**
     * Simple constructor that takes in the path to forward the user to.
     * @param path the path within the web application that the user should be forwarded to
     */
    public ForwardResolution(String path) {
        setPath(path);
    }

    /**
     * Attempts to forward the user to the specified path.
     * @throws ServletException thrown when the Servlet container encounters an error
     * @throws IOException thrown when the Servlet container encounters an error
     */
    public void execute(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        request.getRequestDispatcher(getPath()).forward(request, response);
    }
}
