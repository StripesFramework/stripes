package net.sourceforge.stripes.controller;

/**
 * Container for constant values that are used across more than one class in Stripes.
 *
 * @author Tim Fennell
 */
public interface StripesConstants {
    /**
     * The name of a URL parameter that is used to hold the path (relative to the web application
     * root) from which the current form submission was made.
     */
    String URL_KEY_SOURCE_PAGE = "_sourcePage";

    /**
     * The name under which the ActionBean for a request is stored as a request attribute before
     * forwarding to the JSP.
     */
    String REQ_ATTR_ACTION_BEAN = "actionBean";
}
