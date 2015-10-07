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

package org.artifactory.sapi.fs;

import org.artifactory.fs.FileInfo;

import java.io.InputStream;

/**
 * Immutable interface of a virtual file.
 *
 * @author Yossi Shaul
 */
public interface VfsFile<T extends FileInfo> extends VfsItem<T> {

    @Override
    FileInfo getInfo();

    /**
     * Returns the actual sha1 checksum of the binary for this file.
     * <b>Note:</b> This might be null during new file creation
     *
     * @return The actual sha1 checksum of the binary for this file, null if not constructed yet
     */
    String getSha1();

    /**
     * Returns the actual md5 checksum of the binary for this file.
     * <b>Note:</b> This might be null during new file creation
     *
     * @return The actual sha1 checksum of the binary for this file, null if not constructed yet
     */
    String getMd5();

    /**
     * Returns the input stream of this file. This method will throw an exception if the content doesn't exist
     * (new item) or the content couldn't be loaded.
     *
     * @return An input stream of this file data.
     */
    InputStream getStream();

    /**
     * @return The size, in bytes, of the current file
     */
    long length();
}
