/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
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

package org.artifactory.storage.db.util.blob;

import org.artifactory.util.StringInputStream;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * A wrapper around an input stream with the expected stream length.
 *
 * @author Yossi Shaul
 */
public class BlobWrapper {
    public static final int LENGTH_UNKNOWN = -1;

    private final InputStream in;
    private final long length;

    /**
     * Builds a wrapper around an input stream with unknown length.
     * This constructor is protected to prevent accidental creation when using PostgreSQL.
     *
     * @param in The input stream to wrap
     * @see
     */
    BlobWrapper(InputStream in) {
        this(in, LENGTH_UNKNOWN);
    }

    /**
     * Builds a wrapper around an input stream with known length
     *
     * @param in     The input stream to wrap
     * @param length The length of the wrapped input stream (in bytes)
     */
    public BlobWrapper(InputStream in, long length) {
        if (in == null) {
            throw new NullPointerException("Input stream cannot be null");
        }
        this.in = in;
        this.length = length;
    }

    /**
     * Build a wrapper around a string. The string is expected to be UTF-8 encoded.
     *
     * @param data The data to use as the input stream
     */
    public BlobWrapper(String data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        StringInputStream sis;
        try {
            sis = new StringInputStream(data);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        this.in = sis;
        this.length = sis.getLength();
    }

    /**
     * @return The wrapped input stream
     */
    public InputStream getInputStream() {
        return in;
    }

    /**
     * @return The length (in bytes) of the wrapped input stream or {@link BlobWrapper#LENGTH_UNKNOWN} if the size is
     *         not known.
     */
    public long getLength() {
        return length;
    }
}
