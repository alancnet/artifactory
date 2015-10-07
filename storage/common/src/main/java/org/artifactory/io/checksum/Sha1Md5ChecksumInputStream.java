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

package org.artifactory.io.checksum;

import org.artifactory.checksum.ChecksumType;

import javax.annotation.Nonnull;
import java.io.InputStream;

/**
 * A {@link org.artifactory.io.checksum.ChecksumInputStream} that calculates SHA-1 and MD5 checksums.
 *
 * @author Yossi Shaul
 */
public class Sha1Md5ChecksumInputStream extends ChecksumInputStream {
    public Sha1Md5ChecksumInputStream(InputStream is) {
        // the order of the checksums does matter
        super(is, new Checksum(ChecksumType.sha1), new Checksum(ChecksumType.md5));
    }

    @Nonnull
    public String getSha1() {
        return getChecksums()[0].getChecksum();
    }

    @Nonnull
    public String getMd5() {
        return getChecksums()[1].getChecksum();
    }
}
