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

package org.artifactory.resource;

import java.io.Closeable;
import java.io.InputStream;

/**
 * A handle object that will be used for sending back a resource as stream.
 * <p/>
 * The stream close() method is typically called back automatically by the caller so the handle creator only needs to
 * provide the close() implementation.
 *
 * @author Yoav Landman
 */
public interface ResourceStreamHandle extends Closeable {
    /**
     * @return The input stream of this handle
     */
    InputStream getInputStream();

    /**
     * @return The size of the stream or -1 if unknown
     */
    long getSize();

    /**
     * Closes the underlying input stream
     */
    @Override
    void close();
}
