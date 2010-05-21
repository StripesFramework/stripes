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
import java.io.Writer;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

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

    private JspWriter out;
    private boolean silent;

    /**
     * Create a new layout writer that wraps the given JSP writer.
     * 
     * @param out The JSP writer to which output will be written.
     */
    public LayoutWriter(JspWriter out) {
        log.debug("Create layout writer wrapped around ", out);
        this.out = out;
    }

    /** If true, then discard all output. If false, then resume sending output to the JSP writer. */
    public boolean isSilent() {
        return silent;
    }

    /**
     * Enable or disable silent mode. The output buffer for the given page context will be flushed
     * before silent mode is enabled to ensure all buffered data are written.
     */
    public void setSilent(boolean silent, PageContext context) {
        if (silent != this.silent) {
            try {
                if (context != null)
                    context.getOut().flush();
            }
            catch (IOException e) {
                // This seems to happen once at the beginning and once at the end. Don't know why.
                log.debug("Failed to flush buffer: ", e.getMessage());
            }
            finally {
                this.silent = silent;
                log.trace("Output is " + (silent ? "DISABLED" : "ENABLED"));
            }
        }
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Calls {@link JspWriter#clear()} on the wrapped JSP writer.
     * 
     * @throws IOException
     */
    public void clear() throws IOException {
        out.clear();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (!isSilent())
            out.write(cbuf, off, len);
    }
}
