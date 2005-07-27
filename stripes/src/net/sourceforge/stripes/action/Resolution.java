package net.sourceforge.stripes.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Type that is designed to be returned by &quot;handler&quot; methods in ActionBeans. The
 * Resolution is responsible for executing the next step after the ActionBean has handled the
 * user's request.  In most cases this will likely be to forward the user on to the next page.
 *
 * @see ForwardResolution
 * @author Tim Fennell
 */
public interface Resolution {
    /**
     * Called by the Stripes dispatcher to invoke the Resolution.  Should use the request and
     * response provided to direct the user to an approprirate view.
     *
     * @param request the current HttpServletRequest
     * @param response the current HttpServletResponse
     * @throws Exception exceptions of any type may be thrown if the Resolution cannot be
     *         executed as intended
     */
    void execute(HttpServletRequest request, HttpServletResponse response)
        throws Exception;
}
