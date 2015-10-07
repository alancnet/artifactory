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

import org.artifactory.checksum.ChecksumType;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.fs.StatsInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;

/**
 * A mutable interface of a virtual file.
 *
 * @author Yossi Shaul
 */
public interface MutableVfsFile extends MutableVfsItem<MutableFileInfo>, VfsFile<MutableFileInfo> {
    /**
     * Sets the client provided checksum. This value is provided by the user and might not be a valid checksum.
     *
     * @param type     The checksum type. Either sha1 or md5
     * @param checksum The user provided checksum value
     */
    void setClientChecksum(@Nonnull ChecksumType type, @Nullable String checksum);

    /**
     * Sets the client provided sha1 checksum. This value is provided by the user and might not be a valid checksum.
     *
     * @param sha1 The user provided sha1 checksum
     */
    void setClientSha1(@Nullable String sha1);

    /**
     * Sets the client provided md5 checksum. This value is provided by the user and might not be a valid checksum.
     *
     * @param md5 The user provided sha1 checksum
     */
    void setClientMd5(String md5);

    void fillBinaryData(InputStream in);

    /**
     * Sets the statistics data on this mutable file. Used only during import.
     *
     * @param statsInfo The stats info to set on this file
     */
    void setStats(StatsInfo statsInfo);

    /**
     * Fills the non-content dependent fields (real checksums) from the source info.
     *
     * @param sourceInfo The source file info to read data from
     */
    void fillInfo(FileInfo sourceInfo);

    /**
     * Automatically adds a binary record to the database if binary matching the given sha1 exists in the binary
     * provider. This method is required to support skeleton import.
     *
     * @param sha1   The binary sha1 checksum
     * @param md5    The binary md5 checksum
     * @param length The length of the binary
     * @return True if the record exists or was added successfully
     */
    boolean tryUsingExistingBinary(String sha1, String md5, long length);
}
