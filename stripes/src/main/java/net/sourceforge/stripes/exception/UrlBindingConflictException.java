/* Copyright 2008 Ben Gunter
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
package net.sourceforge.stripes.exception;

import java.util.Collection;

import net.sourceforge.stripes.action.ActionBean;

/**
 * <p>
 * This exception indicates that a URL does not contain enough information to map it to a single
 * {@link ActionBean} class. In some cases, a URL may match more than one URL binding.
 * </p>
 * <p>
 * For example, suppose you have two ActionBeans with the URL bindings <code>/foo/{param}/bar</code>
 * and <code>/foo/{param}/blah</code>. The paths {@code /foo} and {@code /foo/X} -- while legal,
 * since any number of parameters or literals may be omitted from the end of a clean URL -- match
 * both of the URL bindings. Since Stripes cannot determine from the URL the ActionBean to which to
 * dispatch the request, it throws this exception to indicate the conflict.
 * </p>
 * 
 * @author Ben Gunter
 * @since Stripes 1.5.1
 */
public class UrlBindingConflictException extends StripesRuntimeException {
    private static final long serialVersionUID = 1L;

    /** Generate the message to pass to the superclass constructor */
    protected static String getMessage(Class<? extends ActionBean> targetClass, String path,
            Collection<String> matches) {
        return (targetClass == null ? "" : "Failure generating URL for " + targetClass + ". ")
                + "The path " + path + " cannot be mapped to a single ActionBean because multiple "
                + "URL bindings match it. The matching URL bindings are " + matches + ". If you "
                + "generated the URL using the Stripes tag library (stripes:link, stripes:url, "
                + "stripes:form, etc.) then you must embed enough stripes:param tags within the "
                + "parent tag to produce a URL that maps to exactly one of the indicated matches. "
                + "If you generated the URL by some other means, then you must embed enough "
                + "information in the URL to achieve the same end.";
    }

    private String path;
    private Collection<String> matches;
    private Class<? extends ActionBean> targetClass;

    /**
     * New exception indicating that the {@code path} does not map to a single ActionBean because it
     * potentially matches all the URL bindings in the {@code matches} collection.
     * 
     * @param message An informative message about what went wrong
     * @param targetClass The class for which a URL could not be generated.
     * @param path The offending path
     * @param matches A collection of all the potentially matching URL bindings
     */
    public UrlBindingConflictException(String message, Class<? extends ActionBean> targetClass,
            String path, Collection<String> matches) {
        super(message);
        this.targetClass = targetClass;
        this.path = path;
        this.matches = matches;
    }

    /**
     * New exception indicating that the {@code path} does not map to a single ActionBean because it
     * potentially matches all the URL bindings in the {@code matches} collection.
     * 
     * @param targetClass The class for which a URL could not be generated.
     * @param path The offending path
     * @param matches A collection of all the potentially matching URL bindings
     */
    public UrlBindingConflictException(Class<? extends ActionBean> targetClass, String path,
            Collection<String> matches) {
        this(getMessage(targetClass, path, matches), targetClass, path, matches);
    }

    /**
     * New exception indicating that the {@code path} does not map to a single ActionBean because it
     * potentially matches all the URL bindings in the {@code matches} collection.
     * 
     * @param message An informative message about what went wrong
     * @param path The offending path
     * @param matches A collection of all the potentially matching URL bindings
     */
    public UrlBindingConflictException(String message, String path, Collection<String> matches) {
        this(message, null, path, matches);
    }

    /**
     * New exception indicating that the {@code path} does not map to a single ActionBean because it
     * potentially matches all the URL bindings in the {@code matches} collection.
     * 
     * @param path The offending path
     * @param matches A collection of all the potentially matching URL bindings
     */
    public UrlBindingConflictException(String path, Collection<String> matches) {
        this(getMessage(null, path, matches), path, matches);
    }

    /** Get the path that failed to map to a single ActionBean */
    public String getPath() {
        return path;
    }

    /** Get all the URL bindings on existing ActionBeans that match the path */
    public Collection<String> getMatches() {
        return matches;
    }

    /**
     * Get the {@link ActionBean} class for which a URL was being generated when this exception was
     * thrown. If the exception occurred while dispatching a request, then this property will be
     * null since the path cannot be associated with an ActionBean class. However, if it is thrown
     * while generating a URL that is intended to point to an ActionBean, then this property will
     * indicate the class that was being targeted.
     */
    public Class<? extends ActionBean> getTargetClass() {
        return targetClass;
    }
}
