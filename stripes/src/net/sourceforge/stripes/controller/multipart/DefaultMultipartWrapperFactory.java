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
package net.sourceforge.stripes.controller.multipart;

import net.sourceforge.stripes.controller.FileUploadLimitExceededException;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.exception.StripesRuntimeException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * <p>Default implementation of a factory for MultipartWrappers. Looks up a class name in
 * Configuration under the key specified by {@link #WRAPPER_CLASS_NAME}. If no class
 * name is configured, defaults to the {@link CosMultipartWrapper}. An additional configuration
 * parameter is supported to specify the maximum post size allowable.</p>
 * 
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class DefaultMultipartWrapperFactory implements MultipartWrapperFactory {
    /** The configuration key used to lookup the implementation of MultipartWrapper. */
    public static final String WRAPPER_CLASS_NAME = "MultipartWrapper.Class";

    /** The names of the MultipartWrapper classes that will be tried if no other is specified. */
    public static final String[] BUNDLED_IMPLEMENTATIONS = {
            "net.sourceforge.stripes.controller.multipart.CommonsMultipartWrapper",
            "net.sourceforge.stripes.controller.multipart.CosMultipartWrapper" };

    /** Key used to lookup the name of the maximum post size. */
    public static final String MAX_POST = "FileUpload.MaximumPostSize";

    private static final Log log = Log.getInstance(DefaultMultipartWrapperFactory.class);

    // Instance level fields
    private Configuration configuration;
    private Class<? extends MultipartWrapper> multipartClass;
    private long maxPostSizeInBytes = 1024 * 1024 * 10; // Defaults to 10MB
    private File temporaryDirectory;

    /** Get the configuration object that was passed into {@link #init(Configuration)}. */
    protected Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Invoked directly after instantiation to allow the configured component to perform one time
     * initialization.  Components are expected to fail loudly if they are not going to be in a
     * valid state after initialization.
     *
     * @param config the Configuration object being used by Stripes
     * @throws Exception should be thrown if the component cannot be configured well enough to use.
     */
    @SuppressWarnings("unchecked")
	public void init(Configuration config) throws Exception {
        this.configuration = config;

        // Determine which class we're using
        this.multipartClass = config.getBootstrapPropertyResolver().getClassProperty(WRAPPER_CLASS_NAME, MultipartWrapper.class);
        
        if (this.multipartClass == null) {
            // It wasn't defined in web.xml so we'll try the bundled MultipartWrappers
            for (String className : BUNDLED_IMPLEMENTATIONS) {
                try {
                    this.multipartClass = ((Class<? extends MultipartWrapper>) Class
                            .forName(className));
                    break;
                }
                catch (Throwable t) {
                    log.debug(getClass().getSimpleName(), " not using ", className,
                            " because it failed to load. This likely means the supporting ",
                            "file upload library is not present on the classpath.");
                }
            }
        }

        // Log the name of the class we'll be using or a warning if none could be loaded
        if (this.multipartClass == null) {
            log.warn("No ", MultipartWrapper.class.getSimpleName(),
                    " implementation could be loaded");
        }
        else {
            log.info("Using ", this.multipartClass.getName(), " as ", MultipartWrapper.class
                    .getSimpleName(), " implementation.");
        }

        // Figure out where the temp directory is, and store that info
        File tempDir = (File) config.getServletContext().getAttribute("javax.servlet.context.tempdir");
        if (tempDir != null) {
            this.temporaryDirectory = tempDir;
        }
        else {
            this.temporaryDirectory = new File(System.getProperty("java.io.tmpdir")).getAbsoluteFile();
        }

        // See if a maximum post size was configured
        String limit = config.getBootstrapPropertyResolver().getProperty(MAX_POST);
        if (limit != null) {
            Pattern pattern = Pattern.compile("([\\d,]+)([kKmMgG]?).*");
            Matcher matcher = pattern.matcher(limit);
            if (!matcher.matches()) {
                log.error("Did not understand value of configuration parameter ", MAX_POST,
                         " You supplied: ", limit, ". Valid values are any string of numbers ",
                         "optionally followed by (case insensitive) [k|kb|m|mb|g|gb]. ",
                         "Default value of ", this.maxPostSizeInBytes, " bytes will be used instead.");
            }
            else {
                String digits = matcher.group(1);
                String suffix = matcher.group(2).toLowerCase();
                int number = Integer.parseInt(digits);

                if ("k".equals(suffix)) { number = number * 1024; }
                else if ("m".equals(suffix)) {  number = number * 1024 * 1024; }
                else if ("g".equals(suffix)) { number = number * 1024 * 1024 * 1024; }

                this.maxPostSizeInBytes = number;
                log.info("Configured file upload post size limit: ", number, " bytes.");
            }
        }
    }

    /**
     * Wraps the request in an appropriate implementation of MultipartWrapper that is capable of
     * providing access to request parameters and any file parts contained within the request.
     *
     * @param request an active HttpServletRequest
     * @return an implementation of the appropriate wrapper
     * @throws IOException if encountered when consuming the contents of the request
     * @throws FileUploadLimitExceededException if the post size of the request exceeds any
     *          configured limits
     */
    public MultipartWrapper wrap(HttpServletRequest request) throws IOException, FileUploadLimitExceededException {
        try {
            MultipartWrapper wrapper = getConfiguration().getObjectFactory().newInstance(
                    this.multipartClass);
            wrapper.build(request, this.temporaryDirectory, this.maxPostSizeInBytes);
            return wrapper;
        }
        catch (IOException ioe) { throw ioe; }
        catch (FileUploadLimitExceededException fulee) { throw fulee; }
        catch (Exception e) {
            throw new StripesRuntimeException
                    ("Could not construct a MultipartWrapper for the current request.", e);
        }
    }
}
