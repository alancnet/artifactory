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

package org.artifactory.storage.binstore.service;

import org.artifactory.binstore.BinaryInfo;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * Date: 12/11/12
 * Time: 10:24 AM
 *
 * @author freds
 */
public interface BinaryProvider {

    /**
     * Check if the binary associated with the SHA1 checksum key exists and match the length.
     *
     * @param sha1   The SHA1 checksum key for the binary
     * @param length The expected length of this binary
     * @return true if this binary provider has the given key with length
     */
    boolean exists(String sha1, long length);

    /**
     * Retrieve the readable stream with the bytes associated with the provided SHA1 checksum.
     *
     * @param sha1 the checksum key
     * @return the stream that should be closed by the user
     * @throws BinaryNotFoundException If the checksum does not exists in this store
     */
    @Nonnull
    InputStream getStream(String sha1) throws BinaryNotFoundException;

    /**
     * Add the stream to the binary provider and returns all the information about it.
     *
     * @param is The input stream to add to the binary provider
     * @return All information (checksums and length) about the newly added binary entry
     * @throws IOException If the input stream cannot be saved
     */
    BinaryInfo addStream(InputStream is) throws IOException;

    /**
     * Delete the binary entry for this SHA1 key checksum.
     * Should return true if entry is non-existent after this call.
     * Even if nothing was deleted, if entry does not exists, this should return true.
     *
     * @param sha1 The checksum key of the entry to delete
     * @return true if key does not exists anymore, false otherwise
     */
    boolean delete(String sha1);
}
