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

package org.artifactory.util;

import com.google.common.base.Charsets;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

/**
 * Creates an input stream from a string.
 *
 * @author Yossi Shaul
 */
public class StringInputStream extends ByteArrayInputStream {

    private int length;

    public StringInputStream(String string) throws UnsupportedEncodingException {
        this(string, Charsets.UTF_8.displayName());
    }

    public StringInputStream(String string, String encoding) throws UnsupportedEncodingException {
        super(string.getBytes(encoding));
        this.length = super.buf.length;
    }

    /**
     * @return The length, in bytes, of the input stream
     */
    public int getLength() {
        return length;
    }
}
