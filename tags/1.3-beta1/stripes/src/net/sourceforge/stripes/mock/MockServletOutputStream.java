/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
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
