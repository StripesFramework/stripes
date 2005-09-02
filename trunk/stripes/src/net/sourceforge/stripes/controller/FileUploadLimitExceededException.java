package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.exception.StripesServletException;

/**
 * Exception that is thrown when the post size of a multipart/form post used for file
 * upload exceeds the configured maximum size.
 *
 * @author Tim Fennell
 */
public class FileUploadLimitExceededException extends StripesServletException {
    private int maximum;
    private int posted;

    /**
     * Constructs a new exception that contains the limt that was violated, and the size
     * of the post that violated it, both in bytes.
     *
     * @param max the current post size limit
     * @param posted the size of the post
     */
    public FileUploadLimitExceededException(int max, int posted) {
        super("File post limit exceeded. Limit: " + max + " bytes. Posted: " + posted + " bytes.");
        this.maximum = max;
        this.posted = posted;
    }

    /** Gets the limit in bytes for HTTP POSTs. */
    public int getMaximum() { return maximum; }

    /** The size in bytes of the HTTP POST. */
    public int getPosted() { return posted; }
}