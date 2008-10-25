/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.controller.multipart.MultipartWrapper;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.exception.UrlBindingConflictException;

/**
 * HttpServletRequestWrapper that is used to make the file upload functionality transparent.
 * Every request handled by Stripes is wrapped.  Those containing multipart form file uploads
 * are parsed and treated differently, while normal requests are silently wrapped and all calls
 * are delegated to the real request.
 *
 * @author Tim Fennell
 */
public class StripesRequestWrapper extends HttpServletRequestWrapper {
    /** The Multipart Request that parses out all the pieces. */
    private MultipartWrapper multipart;

    /** The Locale that is going to be used to process the request. */
    private Locale locale;

    /** Local copy of the parameter map, into which URI-embedded parameters will be merged. */
    private MergedParameterMap parameterMap;

    /**
     * Looks for the StripesRequesetWrapper for the specific request and returns it. This is done
     * by checking to see if the request is a StripesRequestWrapper, and if not, successively
     * unwrapping the request until the StripesRequestWrapper is found.
     *
     * @param request the ServletRequest that is wrapped by a StripesRequestWrapper
     * @return the StripesRequestWrapper that is wrapping the supplied request
     * @throws IllegalStateException if the request is not wrapped by Stripes
     */
    public static StripesRequestWrapper findStripesWrapper(ServletRequest request) {
        // Loop through any request wrappers looking for the stripes one
        while ( !(request instanceof StripesRequestWrapper)
                && request != null
                && request instanceof HttpServletRequestWrapper) {
            request = ((HttpServletRequestWrapper) request).getRequest();
        }

        // If we have our wrapper after the loop exits, we're good; otherwise...
        if (request instanceof StripesRequestWrapper) {
            return (StripesRequestWrapper) request;
        }

        else {
            throw new IllegalStateException("A request made it through to some part of Stripes " +
                "without being wrapped in a StripesRequestWrapper. The StripesFilter is " +
                "responsible for wrapping the request, so it is likely that either the " +
                "StripesFilter is not deployed, or that its mappings do not include the " +
                "DispatcherServlet _and_ *.jsp. Stripes does not require that the Stripes " +
                "wrapper is the only request wrapper, or the outermost; only that it is present.");
        }
    }

    /**
     * Constructor that will, if the POST is multi-part, parse the POST data and make it
     * available through the normal channels.  If the request is not a multi-part post then it is
     * just wrapped and the behaviour is unchanged.
     *
     * @param request the HttpServletRequest to wrap
     *        this is not a file size limit, but a post size limit.
     * @throws FileUploadLimitExceededException if the total post size is larger than the limit
     * @throws StripesServletException if any other error occurs constructing the wrapper
     */
    public StripesRequestWrapper(HttpServletRequest request) throws StripesServletException {
        super(request);

        String contentType = request.getContentType();
        boolean isPost = "POST".equalsIgnoreCase(request.getMethod());
        if (isPost && contentType != null && contentType.startsWith("multipart/form-data")) {
            constructMultipartWrapper(request);
        }

        // Create a parameter map that merges the URI parameters with the others
        if (isMultipart())
            this.parameterMap = new MergedParameterMap(this, this.multipart);
        else
            this.parameterMap = new MergedParameterMap(this);
    }

    /**
     * Responsible for constructing the MultipartWrapper object and setting it on to
     * the instance variable 'multipart'.
     *
     * @param request the HttpServletRequest to wrap
     *        this is not a file size limit, but a post size limit.
     * @throws StripesServletException if any other error occurs constructing the wrapper
     */
    protected void constructMultipartWrapper(HttpServletRequest request) throws StripesServletException {
        try {
            this.multipart =
                    StripesFilter.getConfiguration().getMultipartWrapperFactory().wrap(request);
        }
        catch (IOException e) {
            throw new StripesServletException("Could not construct request wrapper.", e);
        }
    }

    /** Returns true if this request is wrapping a multipart request, false otherwise. */
    public boolean isMultipart() {
        return this.multipart != null;
    }

    /**
     * Fetches just the names of regular parameters and does not include file upload parameters. If
     * the request is multipart then the information is sourced from the parsed multipart object
     * otherwise it is just pulled out of the request in the usual manner.
     */
    @Override
	public Enumeration<String> getParameterNames() {
        return Collections.enumeration(getParameterMap().keySet());
    }

    /**
     * Returns all values sent in the request for a given parameter name. If the request is
     * multipart then the information is sourced from the parsed multipart object otherwise it is
     * just pulled out of the request in the usual manner.  Values are consistent with
     * HttpServletRequest.getParameterValues(String).  Values for file uploads cannot be retrieved
     * in this way (though parameters sent along with file uploads can).
     */
    @Override
    public String[] getParameterValues(String name) {
        /*
         * When determining whether to provide a URI parameter's default value, the merged parameter
         * map needs to know if the parameter is otherwise defined in the request. It calls this
         * method to do that, so if the parameter map is not defined (which it won't be during its
         * construction), we delegate to the multipart wrapper or superclass.
         */

        MergedParameterMap map = getParameterMap();
        if (map == null) {
            if (isMultipart())
                return this.multipart.getParameterValues(name);
            else
                return super.getParameterValues(name);
        }
        else {
            return map.get(name);
        }
    }

    /**
     * Retrieves the first value of the specified parameter from the request. If the parameter was
     * not sent, null will be returned.
     */
    @Override
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
     * If the request is a clean URL, then extract the parameters from the URI and merge with the
     * parameters from the query string and/or request body.
     */
    @Override
    public MergedParameterMap getParameterMap() {
        return this.parameterMap;
    }

    /**
     * Extract new URI parameters from the URI of the given {@code request} and merge them with the
     * previous URI parameters.
     */
    public void pushUriParameters(HttpServletRequestWrapper request) {
        getParameterMap().pushUriParameters(request);
    }

    /**
     * Restore the URI parameters to the state they were in before the previous call to
     * {@link #pushUriParameters(HttpServletRequestWrapper)}.
     */
    public void popUriParameters() {
        getParameterMap().popUriParameters();
    }

    /**
     * Provides access to the Locale being used to process the request.
     * @return a Locale object representing the chosen locale for the request.
     * @see net.sourceforge.stripes.localization.LocalePicker
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     *  Returns a single element enumeration containing the selected Locale for this request.
     *  @see net.sourceforge.stripes.localization.LocalePicker
     */
    @Override
    public Enumeration<Locale> getLocales() {
        List<Locale> list = new ArrayList<Locale>();
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
        return this.multipart.getFileParameterNames();
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
        if (this.multipart != null) {
            return this.multipart.getFileParameterValue(name);
        }
        else {
            return null;
        }
    }
}

/**
 * A {@link Map} implementation that is used by {@link StripesRequestWrapper} to merge URI parameter
 * values with request parameter values on the fly.
 * 
 * @author Ben Gunter
 */
class MergedParameterMap implements Map<String, String[]> {
    class Entry implements Map.Entry<String, String[]> {
        private String key;

        Entry(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public String[] getValue() {
            return get(key);
        }

        public String[] setValue(String[] value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object obj) {
            Entry that = (Entry) obj;
            return this.key == that.key;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public String toString() {
            return "" + key + "=" + Arrays.deepToString(getValue());
        }
    }

    private HttpServletRequestWrapper request;
    private Map<String, String[]> uriParams;
    private Stack<Map<String, String[]>> uriParamStack;

    MergedParameterMap(HttpServletRequestWrapper request) {
        this.request = request;
        this.uriParams = getUriParameters(request);
        if (this.uriParams == null) {
            this.uriParams = Collections.emptyMap();
        }
    }

    MergedParameterMap(HttpServletRequestWrapper request, MultipartWrapper multipart) {
        this.request = request;

        // extract URI parameters
        Map<String, String[]> uriParams = getUriParameters(request);

        /*
         * Special handling of parameters if this is a multipart request. The parameters will be
         * pulled from the MultipartWrapper and merged in with the URI parameters.
         */
        Map<String, String[]> multipartParams = null;
        Enumeration<?> names = multipart.getParameterNames();
        if (names != null && names.hasMoreElements()) {
            multipartParams = new LinkedHashMap<String, String[]>();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                multipartParams.put(name, multipart.getParameterValues(name));
            }
        }

        // if no multipart params and no URI params then use empty map
        if (uriParams == null && multipartParams == null) {
            this.uriParams = Collections.emptyMap();
        }
        else {
            this.uriParams = mergeParameters(uriParams, multipartParams);
        }
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey(Object key) {
        return getParameterMap().containsKey(key) || uriParams.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return getParameterMap().containsValue(value) || uriParams.containsValue(value);
    }

    public Set<Map.Entry<String, String[]>> entrySet() {
        Set<Map.Entry<String, String[]>> entries = new LinkedHashSet<Map.Entry<String, String[]>>();
        for (String key : keySet()) {
            entries.add(new Entry(key));
        }
        return entries;
    }

    public String[] get(Object key) {
        if (key == null)
            return null;
        else
            return mergeParameters(getParameterMap().get(key), uriParams.get(key));
    }

    public boolean isEmpty() {
        return getParameterMap().isEmpty() && uriParams.isEmpty();
    }

    public Set<String> keySet() {
        Set<String> merged = new LinkedHashSet<String>();
        merged.addAll(uriParams.keySet());
        merged.addAll(getParameterMap().keySet());
        return merged;
    }

    public String[] put(String key, String[] value) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends String, ? extends String[]> m) {
        throw new UnsupportedOperationException();
    }

    public String[] remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return keySet().size();
    }

    public Collection<String[]> values() {
        Set<String> keys = keySet();
        List<String[]> merged = new ArrayList<String[]>(keys.size());
        for (String key : keys) {
            merged.add(mergeParameters(getParameterMap().get(key), uriParams.get(key)));
        }
        return merged;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("{ ");
        for (Map.Entry<String, String[]> entry : entrySet()) {
            buf.append(entry).append(", ");
        }
        if (buf.toString().endsWith(", "))
            buf.setLength(buf.length() - 2);
        buf.append(" }");
        return buf.toString();
    }

    /** Get the parameter map from the request that is wrapped by the {@link StripesRequestWrapper}. */
    @SuppressWarnings("unchecked")
    Map<String, String[]> getParameterMap() {
        return request == null ? Collections.emptyMap() : request.getRequest().getParameterMap();
    }

    /**
     * Extract new URI parameters from the URI of the given {@code request} and merge them with the
     * previous URI parameters.
     */
    void pushUriParameters(HttpServletRequestWrapper request) {
        if (this.uriParamStack == null) {
            this.uriParamStack = new Stack<Map<String, String[]>>();
        }
        Map<String, String[]> map = getUriParameters(request);
        this.uriParamStack.push(this.uriParams);
        this.uriParams = mergeParameters(new LinkedHashMap<String, String[]>(this.uriParams), map);
    }

    /**
     * Restore the URI parameters to the state they were in before the previous call to
     * {@link #pushUriParameters(HttpServletRequestWrapper)}.
     */
    void popUriParameters() {
        if (this.uriParamStack == null || this.uriParamStack.isEmpty()) {
            this.uriParams = null;
        }
        else {
            this.uriParams = this.uriParamStack.pop();
        }
    }

    /**
     * Extract any parameters embedded in the URI of the given {@code request} and return them in a
     * {@link Map}. If no parameters are present in the URI, then return null.
     */
    Map<String, String[]> getUriParameters(HttpServletRequest request) {
        UrlBinding binding = null;
        try {
            binding = UrlBindingFactory.getInstance().getBinding(request);
        }
        catch (UrlBindingConflictException e) {
            // This can be safely ignored
        }

        Map<String, String[]> params = null;
        if (binding != null && binding.getParameters().size() > 0) {
            for (UrlBindingParameter p : binding.getParameters()) {
                String name = p.getName();
                if (name != null) {
                    String value = p.getValue();
                    if (UrlBindingParameter.PARAMETER_NAME_EVENT.equals(name)) {
                        if (value == null) {
                            // Don't provide the default event name. The dispatcher will handle that
                            // automatically, and there's no way of knowing at this point if another
                            // event name is provided by a different parameter.
                            continue;
                        }
                        else {
                            name = value;
                            value = "";
                        }
                    }
                    if (value == null && request.getParameterValues(name) == null) {
                        value = p.getDefaultValue();
                    }
                    if (name != null && value != null) {
                        if (params == null) {
                            params = new LinkedHashMap<String, String[]>();
                        }
                        String[] values = params.get(name);
                        if (values == null) {
                            values = new String[] { value };
                        }
                        else {
                            String[] tmp = new String[values.length + 1];
                            System.arraycopy(values, 0, tmp, 0, values.length);
                            tmp[tmp.length - 1] = value;
                            values = tmp;
                        }
                        params.put(name, values);
                    }
                }
            }
        }

        return params;
    }

    /** Merge the values from {@code source} into {@code target}. */
    Map<String, String[]> mergeParameters(Map<String, String[]> target, Map<String, String[]> source) {
        // target must not be null and we must not modify source
        if (target == null)
            target = new LinkedHashMap<String, String[]>();

        // nothing to do if source is null or empty
        if (source == null || source.isEmpty())
            return target;

        // merge the values from source that already exist in target
        for (Map.Entry<String, String[]> entry : target.entrySet()) {
            entry.setValue(mergeParameters(entry.getValue(), source.get(entry.getKey())));
        }

        // copy the values from source that do not exist in target
        for (Map.Entry<String, String[]> entry : source.entrySet()) {
            if (!target.containsKey(entry.getKey())) {
                target.put(entry.getKey(), entry.getValue());
            }
        }

        return target;
    }

    /**
     * Merge request parameter values from the original request with the parameters that are
     * embedded in the URI. Either or both arguments may be empty or null.
     * 
     * @param requestParams the parameters from the original request
     * @param uriParams parameters extracted from the URI
     * @return the merged parameter values
     */
    String[] mergeParameters(String[] requestParams, String[] uriParams) {
        if (requestParams == null || requestParams.length == 0) {
            if (uriParams == null || uriParams.length == 0)
                return null;
            else
                return uriParams;
        }
        else if (uriParams == null || uriParams.length == 0) {
            return requestParams;
        }
        else {
            String[] merged = new String[uriParams.length + requestParams.length];
            System.arraycopy(uriParams, 0, merged, 0, uriParams.length);
            System.arraycopy(requestParams, 0, merged, uriParams.length, requestParams.length);
            return merged;
        }
    }
}
