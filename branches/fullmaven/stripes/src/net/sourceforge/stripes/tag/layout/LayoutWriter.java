/* Copyright 2010 Ben Gunter
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
package net.sourceforge.stripes.tag.layout;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;

/**
 * A writer that wraps around the normal JSP writer with the ability to silence the output
 * temporarily. This is required to prevent the non-layout contents of a {@link LayoutDefinitionTag}
 * from rendering more than once when {@link LayoutRenderTag}s and {@link LayoutComponentTag}s are
 * nested within it. The definition tag silences output during a component render phase, and the
 * component that wishes to render turns output back on during its body evaluation.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5.4
 */
public class LayoutWriter extends Writer {
    private static final Log log = Log.getInstance(LayoutWriter.class);

    /** The control character that, when encountered in the output stream, toggles the silent state. */
    private static final char TOGGLE = 0;

    private LinkedList<Writer> writers = new LinkedList<Writer>();
    private boolean silent, silentState;

    /**
     * Create a new layout writer that wraps the given JSP writer.
     * 
     * @param out The JSP writer to which output will be written.
     */
    public LayoutWriter(JspWriter out) {
        log.debug("Create layout writer wrapped around ", out);
        this.writers.addFirst(out);
    }

    /** Get the writer to which output is currently being written. */
    protected Writer getOut() {
        return writers.peek();
    }

    /** If true, then discard all output. If false, then resume sending output to the JSP writer. */
    public boolean isSilent() {
        return silent;
    }

    /**
     * Enable or disable silent mode. The output buffer for the given page context will be flushed
     * before silent mode is enabled to ensure all buffered data are written.
     * 
     * @param silent True to silence output, false to enable output.
     * @param pageContext The page context in use at the time output is to be silenced.
     * @throws IOException If an error occurs writing to output.
     */
    public void setSilent(boolean silent, PageContext pageContext) throws IOException {
        if (silent != this.silent) {
            pageContext.getOut().write(TOGGLE);
            this.silent = silent;
            log.trace("Output is ", (silent ? "DISABLED" : "ENABLED"));
        }
    }

    /**
     * Flush the page context's output buffer and redirect output into a buffer. The buffer can be
     * closed and its contents retrieved by calling {@link #closeBuffer(PageContext)}.
     */
    public void openBuffer(PageContext pageContext) {
        log.trace("Open buffer");
        tryFlush(pageContext);
        writers.addFirst(new StringWriter(1024));
    }

    /**
     * Flush the page context's output buffer and resume sending output to the writer that was
     * receiving output prior to calling {@link #openBuffer(PageContext)}.
     * 
     * @return The buffer's contents.
     */
    public String closeBuffer(PageContext pageContext) {
        if (getOut() instanceof StringWriter) {
            tryFlush(pageContext);
            String contents = ((StringWriter) writers.poll()).toString();
            log.trace("Closed buffer: \"", contents, "\"");
            return contents;
        }
        else {
            throw new StripesRuntimeException(
                    "Attempt to close a buffer without having first called openBuffer(..)!");
        }
    }

    /** Try to flush the page context's output buffer. If an exception is thrown, just log it. */
    protected void tryFlush(PageContext pageContext) {
        try {
            if (pageContext != null)
                pageContext.getOut().flush();
        }
        catch (IOException e) {
            // This seems to happen once at the beginning and once at the end. Don't know why.
            log.debug("Failed to flush buffer: ", e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        getOut().close();
    }

    @Override
    public void flush() throws IOException {
        getOut().flush();
    }

    /**
     * Calls {@link JspWriter#clear()} on the wrapped JSP writer.
     * 
     * @throws IOException
     */
    public void clear() throws IOException {
        Writer out = getOut();
        if (out instanceof JspWriter) {
            ((JspWriter) out).clear();
        }
        else if (out instanceof StringWriter) {
            ((StringWriter) out).getBuffer().setLength(0);
        }
        else {
            throw new StripesRuntimeException("How did I get a writer of type "
                    + out.getClass().getName() + "??");
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int i = off, mark = i, n = i + len; i < n; ++i) {
            switch (cbuf[i]) {
            case TOGGLE:
                if (this.silentState)
                    mark = i + 1;
                else if (i > mark)
                    getOut().write(cbuf, mark, i - mark);
                this.silentState = !this.silentState;
                break;
            default:
                if (this.silentState)
                    ++mark;
                else if (i > mark && i == n - 1)
                    getOut().write(cbuf, mark, i - mark + 1);
            }
        }
    }
}
