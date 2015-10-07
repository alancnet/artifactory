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

package org.artifactory.storage.db.binstore.entity;

import org.apache.commons.lang.StringUtils;
import org.artifactory.checksum.ChecksumType;

/**
 * Represents a binary data entry in the database.
 *
 * @author Yossi Shaul
 */
public class BinaryData {
    private final String sha1;
    private final String md5;
    private final long length;

    public BinaryData(String sha1, String md5, long length) {
        this.sha1 = sha1;
        this.md5 = md5;
        this.length = length;
        simpleValidation();
    }

    private void simpleValidation() {
        if (StringUtils.isBlank(sha1) || sha1.length() != ChecksumType.sha1.length()) {
            throw new IllegalArgumentException("SHA1 value '" + sha1 + "' is not a valid checksum");
        }
        if (StringUtils.isBlank(md5) || md5.length() != ChecksumType.md5.length()) {
            throw new IllegalArgumentException("MD5 value '" + md5 + "' is not a valid checksum");
        }
        if (length < 0L) {
            throw new IllegalArgumentException("Length " + length + " is not a valid length");
        }
    }

    public boolean isValid() {
        simpleValidation();
        return ChecksumType.sha1.isValid(sha1) && ChecksumType.md5.isValid(md5);
    }

    public String getSha1() {
        return sha1;
    }

    public String getMd5() {
        return md5;
    }

    public long getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "{" + sha1 + ',' + md5 + ',' + length + '}';
    }
}
