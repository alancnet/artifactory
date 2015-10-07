/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.request;

import com.google.common.base.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * An Artifactory response which holds the OutputStream for later use.
 *
* @author Shay Yaakov
*/
public class InternalCapturingResponse extends InternalArtifactoryResponse {
    /**
     * Either the output stream or the writer might be used. Never both of them.
     */
    ByteArrayOutputStream out;
    private StringWriter stringWriter;
    private PrintWriter writer;

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (out == null) {
            out = new ByteArrayOutputStream();
        }
        return out;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (stringWriter == null) {
            stringWriter = new StringWriter();
            writer = new PrintWriter(stringWriter);
        }
        return writer;
    }

    /**
     * Returns the string representing the response. Only call this method if the expected response is text base.
     *
     * @return The response result as a stream.
     */
    public String getResultAsString() {
        if (out != null) {
            return new String(out.toByteArray(), Charsets.UTF_8);
        } else if (stringWriter != null) {
            return stringWriter.toString();
        } else {
            return null;
        }
    }
}
