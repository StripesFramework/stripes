package net.sourceforge.stripes.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract class that provides a consistent API for all Resolutions that send the user onward to
 * another view - either by forwarding, redirecting or some other mechanism.  Provides methods
 * for getting and setting the path that the user should be sent to next.
 *
 * @author Tim Fennell
 */
public abstract class OnwardResolution {
    protected String path;

    /** Accessor for the path that the user should be sent to. */
    public String getPath() {
        return path;
    }

    /** Setter for the path that the user should be sent to. */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Method should be implemented by sublcasses to perform the actual task of sending the user
     * to the path specified.
     *
     * @param request the current HttpServletRequest
     * @param response the current HttpServletResponse
     * @throws Exception subclasses should throw an exception if the execution fails
     */
    public abstract void execute(HttpServletRequest request, HttpServletResponse response)
        throws Exception;


    /**
     * Method that will work for this class and subclasses; returns a String containing the
     * class name, and the path to which it will send the user.
     */
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "path='" + path + "'" +
            "}";
    }
}
