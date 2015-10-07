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

package org.artifactory.checksum;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import static org.artifactory.checksum.ChecksumInfo.TRUSTED_FILE_MARKER;

/**
 * A container class to manage a collection of checksums, with isIdentical and other equals, hashCode goodies.
 *
 * @author Yoav Landman
 * @author Fred Simon
 */
@XStreamAlias("checksumsInfo")
public class ChecksumsInfo implements Serializable {

    private final EnumMap<ChecksumType, ChecksumInfo> checksums =
            new EnumMap<>(ChecksumType.class);

    public ChecksumsInfo() {
        // default empty constructor
    }

    public ChecksumsInfo(ChecksumsInfo other) {
        // create a defensive copy. ChecksumInfo is immutable no need to copy.
        if (!other.isEmpty()) {
            for (ChecksumInfo checksum : other.getChecksums()) {
                addChecksumInfo(checksum);
            }
        }
    }

    public boolean isEmpty() {
        return checksums.isEmpty();
    }

    public int size() {
        return checksums.size();
    }

    /**
     * @return The actual SHA1 checksum or null if not found
     */
    public String getSha1() {
        ChecksumInfo sha1 = getChecksumInfo(ChecksumType.sha1);
        return sha1 == null ? null : sha1.getActual();
    }

    public String getMd5() {
        ChecksumInfo md5 = getChecksumInfo(ChecksumType.md5);
        return md5 == null ? null : md5.getActual();
    }

    public void setChecksums(Set<ChecksumInfo> checksums) {
        if (checksums == null) {
            throw new IllegalArgumentException("Checksums cannot be null.");
        }
        this.checksums.clear();
        for (ChecksumInfo checksum : checksums) {
            addChecksumInfo(checksum);
        }
    }

    public Set<ChecksumInfo> getChecksums() {
        return new HashSet<>(checksums.values());
    }

    public ChecksumInfo getChecksumInfo(ChecksumType type) {
        return checksums.get(type);
    }

    /**
     * Adds new checksum info. If checksum of the same type already exists it will be overridden.
     *
     * @param checksumInfo The checksum info to add
     */
    public void addChecksumInfo(ChecksumInfo checksumInfo) {
        if (checksumInfo == null) {
            throw new IllegalArgumentException("Nulls are not allowed");
        }
        checksums.put(checksumInfo.getType(), checksumInfo);
    }

    /**
     * Replaces all checksums with null checksum values that are marked as trusted by Artifactory.
     * <p/>
     * For internal use only.
     */
    //TODO: [by yl] Remove from papi
    public void createTrustedChecksums() {
        for (ChecksumType type : ChecksumType.BASE_CHECKSUM_TYPES) {
            addChecksumInfo(new ChecksumInfo(type, TRUSTED_FILE_MARKER, null));
        }
    }

    /**
     * Compares checksums by type and values.
     *
     * @param info
     * @return
     */
    public boolean isIdentical(ChecksumsInfo info) {
        if (this.checksums == info.checksums) {
            return true;
        }
        if (this.checksums == null || info.checksums == null) {
            return false;
        }
        // TODO: Should be this
        //return this.checksums.equals(info.checksums);
        if (checksums.size() != info.checksums.size()) {
            return false;
        }

        for (ChecksumInfo other : checksums.values()) {
            ChecksumInfo mine = info.getChecksumInfo(other.getType());
            if (!other.isIdentical(mine)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares checksums by type only.
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChecksumsInfo that = (ChecksumsInfo) o;

        if (checksums != null ? !checksums.equals(that.checksums) : that.checksums != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return checksums != null ? checksums.hashCode() : 0;
    }

    @Override
    public String toString() {
        return new StringBuilder("ChecksumsInfo").append("{checksums=").append(checksums).append('}').toString();
    }
}