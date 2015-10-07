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
 * @author freds
 * @date Nov 18, 2008
 */
public class VersionComparator {
    private final ArtifactoryVersion from;
    private final ArtifactoryVersion until;

    public VersionComparator(ArtifactoryVersion from, ArtifactoryVersion until) {
        this.from = from;
        this.until = until;
    }

    public boolean isCurrent() {
        return until.isCurrent();
    }

    public boolean isBefore(ArtifactoryVersion version) {
        return until.before(version);
    }

    public boolean isAfter(ArtifactoryVersion version) {
        return from.after(version);
    }

    public boolean supports(ArtifactoryVersion version) {
        return from.beforeOrEqual(version) && version.beforeOrEqual(until);
    }

    public boolean supports(int revision) {
        return from.getRevision() <= revision && revision <= until.getRevision();
    }

    public ArtifactoryVersion getFrom() {
        return from;
    }

    public ArtifactoryVersion getUntil() {
        return until;
    }
}
