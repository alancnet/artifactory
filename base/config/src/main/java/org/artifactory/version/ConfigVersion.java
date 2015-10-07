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

/**
 * Holds the various Artifactory configuration versions. Each configuration version represents a range of versions that
 * use the same underlying sub-config versions (of metadata, security etc.). Config versions can be used to determine if
 * conversion from one version to another is needed (different sub config elements), if such conversion is possible, and
 * whether it can be done automatically.
 *
 * @author Yossi Shaul
 */
public enum ConfigVersion implements SubConfigElementVersion {
    v1(ArtifactoryVersion.v122rc0, ArtifactoryVersion.v130beta2, false),
    v2(ArtifactoryVersion.v130beta3, ArtifactoryVersion.v130beta5, false),
    v3(ArtifactoryVersion.v130beta6, ArtifactoryVersion.v130beta61, false),
    v4(ArtifactoryVersion.v130rc1, ArtifactoryVersion.v208),
    v5(ArtifactoryVersion.v210, ArtifactoryVersion.getCurrent());

    private final VersionComparator comparator;
    private final boolean autoUpdateCapable;

    ConfigVersion(ArtifactoryVersion from, ArtifactoryVersion until) {
        this(from, until, true);
    }

    ConfigVersion(ArtifactoryVersion from, ArtifactoryVersion until, boolean autoUpdateCapable) {
        this.comparator = new VersionComparator( from, until);
        this.autoUpdateCapable = autoUpdateCapable;
    }

    public boolean isAutoUpdateCapable() {
        return autoUpdateCapable;
    }

    public boolean isCurrent() {
        return comparator.isCurrent();
    }

    public boolean isCompatibleWith(ArtifactoryVersion version) {
        return comparator.supports(version);
    }

    public boolean isBefore(ArtifactoryVersion version) {
        return comparator.isBefore(version);
    }

    public boolean isAfter(ArtifactoryVersion version) {
        return comparator.isAfter(version);
    }

    @Override
    public VersionComparator getComparator() {
        return comparator;
    }

    public static ConfigVersion findCompatibleVersion(ArtifactoryVersion version) {
        for (ConfigVersion configVersion : values()) {
            if (configVersion.isCompatibleWith(version)) {
                return configVersion;
            }
        }
        throw new IllegalArgumentException(
                "No compatible storage version found for exiting Artifactory storage version: " + version + ".");
    }
}
