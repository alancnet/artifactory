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

package org.artifactory.version;

import org.apache.commons.lang.StringUtils;

/**
 * Holds all the version data about Artifactory. version name, and revision from the properties file and
 * ArtifactoryVersion that matches those values.
 *
 * @author Yossi Shaul
 */
public class CompoundVersionDetails {
    private final ArtifactoryVersion version;
    private final String versionName;
    private final String revision;
    private final long timestamp;

    public CompoundVersionDetails(ArtifactoryVersion version, String versionName, String revision, long timestamp) {
        this.version = version;
        this.versionName = versionName;
        this.revision = revision;
        this.timestamp = timestamp;
    }

    /**
     * @return The closest matched version for the input stream/file
     */
    public ArtifactoryVersion getVersion() {
        return version;
    }

    /**
     * @return The raw version string as read from the input stream/file
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * @return The raw revision string as read from the input stream/file
     */
    public String getRevision() {
        return revision;
    }

    /**
     * @return Artifactory release timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    public boolean isCurrent() {
        return version.isCurrent();
    }

    public int getRevisionInt() {
        int rev = 0;
        if (StringUtils.isNotBlank(revision) && StringUtils.isNumeric(revision) &&
                !("" + Integer.MAX_VALUE).equals(revision)) {
            rev = Integer.valueOf(revision);
        }
        return rev;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompoundVersionDetails details = (CompoundVersionDetails) o;
        return revision.equals(details.revision) && version == details.version &&
                versionName.equals(details.versionName);

    }

    @Override
    public int hashCode() {
        int result = version.hashCode();
        result = 31 * result + versionName.hashCode();
        result = 31 * result + revision.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "version='" + versionName + '\'' +
                ", revision='" + revision + '\'' +
                ", released version=" + version;
    }
}
