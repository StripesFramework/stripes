package net.sourceforge.stripes.controller;

import com.oreilly.servlet.MultipartRequest;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.exception.StripesServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * HttpServletRequestWrapper that is used to make the file upload functionality transparent.
 * Every request handled by Stripes is wrapped.  Those containing multipart form file uploads
 * are parsed and treated differently, while normal requests are silently wrapped and all calls
 * are delgated to the real request.
 *
 * @author Tim Fennell
 */
@SuppressWarnings("CLASS") // Request has some deprecated methods we don't touch, so don't warn us!
public class StripesRequestWrapper extends HttpServletRequestWrapper {
    /** The Multipart Request that parses out all the pieces. */
    private MultipartRequest multipart;

    /** The Locale that is going to be used to process the request. */
    private Locale locale;

    /**
     * Constructor that will, if the POST is multi-part, parse the POST data and make it
     * available through the normal channels.  If the request is not a multi-part post then it is
     * just wrapped and the behaviour is unchanged.
     *
     * @param request the HttpServletRequest to wrap
     * @param pathToTempDir the path to a temporary directory in which to store files during upload
     * @param maxTotalPostSize a limit on how much can be uploaded in a single request. Note that
     *        this is not a file size limit, but a post size limit.
     */
    public StripesRequestWrapper(HttpServletRequest request,
                                 String pathToTempDir,
                                 int maxTotalPostSize) throws StripesServletException {
        super(request);

        try {
            String contentType = request.getContentType();

            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                this.multipart = new MultipartRequest(request, pathToTempDir, maxTotalPostSize);
            }
        }
        catch (Exception e) {
            throw new StripesServletException("Could not construct request wrapper.", e);
        }
    }

    public boolean isMultipart() {
        return this.multipart != null;
    }

    /**
     * Fetches just the names of regular parameters and does not include file upload parameters. If
     * the request is multipart then the information is sourced from the parsed multipart object
     * otherwise it is just pulled out of the request in the usual manner.
     */
    public Enumeration<String> getParameterNames() {
        if ( isMultipart() ) {
            return this.multipart.getParameterNames();
        }
        else {
            return super.getParameterNames();
        }
    }

    /**
     * Returns all values sent in the request for a given parameter name. If the request is
     * multipart then the information is sourced from the parsed multipart object otherwise it is
     * just pulled out of the request in the usual manner.  Values are consistent with
     * HttpServletRequest.getParameterValues(String).  Values for file uploads cannot be retrieved
     * in this way (though parameters sent along with file uploads can).
     */
    public String[] getParameterValues(String name) {
        if ( isMultipart() ) {
            String[] values = this.multipart.getParameterValues(name);
            if (values != null) {
                for (int i=0; i<values.length; ++i) {
                    if (values[i] == null) {
                        values[i] = "";
                    }
                }
            }

            return values;
        }
        else {
            return super.getParameterValues(name);
        }
    }

    /**
     * Retrieves the first value of the specified parameter from the request. If the parameter was
     * not sent, null will be returned.
     */
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        else {
            return null;
        }
    }


    /**
     * Returns a map of parameter name and values.
     */
    public Map<String,String[]> getParameterMap() {
        if (isMultipart()) {
            Map parameterMap = new HashMap();
            Enumeration names = this.multipart.getParameterNames();

            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                parameterMap.put(name, getParameterValues(name));
            }

            return parameterMap;
        }
        else {
            return super.getParameterMap();
        }
    }

    /**
     * Provides access to the Locale being used to process the request.
     * @return a Locale object representing the chosen locale for the request.
     * @see net.sourceforge.stripes.localization.LocalePicker
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     *  Returns a single element enumeration containing the selected Locale for this request.
     *  @see net.sourceforge.stripes.localization.LocalePicker
     */
    public Enumeration getLocales() {
        List list = new ArrayList();
        list.add(this.locale);
        return Collections.enumeration(list);
    }

    ///////////////////////////////////////////////////////////////////////////
    // The following methods are specific to the StripesRequestWrapper and are
    // not present in the HttpServletRequest interface.
    ///////////////////////////////////////////////////////////////////////////

    /** Used by the dispatcher to set the Locale chosen by the configured LocalePicker. */
    protected void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Returns the names of request parameters that represent files being uploaded by the user. If
     * no file upload parameters are submitted returns an empty enumeration.
     */
    public Enumeration<String> getFileParameterNames() {
        return this.multipart.getFileNames();
    }

    /**
     * Returns a FileBean representing an uploaded file with the form field name = &quot;name&quot;.
     * If the form field was present in the request, but no file was uploaded, this method will
     * return null.
     *
     * @param name the form field name of type file
     * @return a FileBean if a file was actually submitted by the user, otherwise null
     */
    public FileBean getFileParameterValue(String name) {

        if (this.multipart.getFile(name) != null) {
            return new FileBean(this.multipart.getFile(name),
                                this.multipart.getContentType(name),
                                this.multipart.getOriginalFileName(name));
        }
        else {
            return null;
        }
    }
}
