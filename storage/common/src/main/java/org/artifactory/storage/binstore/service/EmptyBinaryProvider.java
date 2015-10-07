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
import org.artifactory.storage.binstore.service.annotation.BinaryProviderClassInfo;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * Date: 12/11/12
 * Time: 6:31 PM
 *
 * @author freds
 */
@BinaryProviderClassInfo(nativeName = "empty")
public class EmptyBinaryProvider extends BinaryProviderBase {

    @Override
    public BinaryProviderBase next() {
        return null;
    }

    @Override
    public boolean exists(String sha1, long length) {
        return false;
    }

    @Nonnull
    @Override
    public InputStream getStream(String sha1) throws BinaryNotFoundException {
        throw new BinaryNotFoundException("Binary provider has no content for '" + sha1 + "'");
    }

    @Override
    public BinaryInfo addStream(InputStream is) throws IOException {
        throw new IOException("Empty Binary Provider cannot accept stream");
    }

    @Override
    public boolean delete(String sha1) {
        return true;
    }
}
