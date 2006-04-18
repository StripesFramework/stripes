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
package net.sourceforge.stripes.mock;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

/**
 * Mock implementation of a ServletOutputStream that just uses a byte array output stream to
 * capture any output and make it available after the test is done.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class MockServletOutputStream extends ServletOutputStream {
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    /** Pass through method calls ByteArrayOutputStream.write(int b). */
    public void write(int b) throws IOException { out.write(b); }

    /** Returns the array of bytes that have been written to the output stream. */
    public byte[] getBytes() {
        return out.toByteArray();
    }

    /** Returns, as a character string, the output that was written to the output stream. */
    public String getString() {
        return out.toString();
    }
}
