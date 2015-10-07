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

/**
 * Holds original and calculated values of a checksum.
 *
 * @author Yossi Shaul
 */
@XStreamAlias("checksum")
public class ChecksumInfo implements Serializable {

    // marks a checksum type with no original checksum to be safe.
    // this marker is used when a file is deployed and we don't have the remote
    // checksum but we have the actual file
    public static final String TRUSTED_FILE_MARKER = "NO_ORIG";

    private final ChecksumType type;
    private final String original;
    private final String actual;

    public ChecksumInfo(ChecksumType type, String original, String actual) {
        this.type = type;
        if (actual != null && !type.isValid(actual)) {
            throw new IllegalStateException(
                    "Actual checksum invalid " + actual + " : " + original + " for type " + type);
        }
        // The original checksum comes from remote servers which we can't control their formats, therefore we have to
        // normalize the original checksum value.
        this.original = normalize(original);
        this.actual = actual;
    }

    public ChecksumType getType() {
        return type;
    }

    /**
     * @return The client (original) checksum or the actual if the checksum is marked as trusted
     */
    public String getOriginal() {
        if (isMarkedAsTrusted()) {
            return getActual();
        } else {
            return original;
        }
    }

    /**
     * @return Always returns the client (original) recorded checksum, even if it is org.artifactory.checksum.ChecksumInfo#TRUSTED_FILE_MARKER
     */
    public String getOriginalOrNoOrig() {
        return original;
    }

    public String getActual() {
        return actual;
    }

    public boolean checksumsMatch() {
        return original != null && actual != null && (isMarkedAsTrusted() || actual.equals(original));
    }

    /**
     * Normalize checksum value.
     * Checksum comparisons shouldn't be case sensitive therefore we have to normalize the original checksum to lower case.
     */
    private String normalize(String candidate) {
        return type.isValid(candidate) ? candidate.toLowerCase() : candidate;
    }

    /**
     * Checks if the checksum is marked as trusted by Artifactory.
     * <p/>
     * For internal use only.
     */
    //TODO: [by yl] Remove from papi
    public boolean isMarkedAsTrusted() {
        return TRUSTED_FILE_MARKER.equals(original);
    }

    /**
     * Compares checksum by type and value.
     */
    public boolean isIdentical(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChecksumInfo info = (ChecksumInfo) o;
        if (type != info.type) {
            return false;
        }
        if (actual != null ? !actual.equals(info.actual) : info.actual != null) {
            return false;
        }
        if (original != null ? !original.equals(info.original) : info.original != null) {
            return false;
        }
        return true;
    }

    /**
     * Compares checksum by type only
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChecksumInfo info = (ChecksumInfo) o;
        return type == info.type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return "ChecksumInfo{" +
                "type=" + type +
                ", original='" + original + '\'' +
                ", actual='" + actual + '\'' +
                '}';
    }
}