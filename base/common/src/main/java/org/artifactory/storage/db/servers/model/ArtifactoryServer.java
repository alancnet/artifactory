/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.storage.db.servers.model;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.state.ArtifactoryServerState;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Represents an Artifactory server in cluster configuration.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryServer implements Serializable {
    private final String serverId;
    private final long startTime;
    private final String contextUrl;
    private final int membershipPort;
    private final ArtifactoryServerState serverState;
    private final ArtifactoryServerRole serverRole;
    private final long lastHeartbeat;
    private final String artifactoryVersion;
    private final int artifactoryRevision;
    private final long artifactoryRelease;
    private final String licenseKeyHash;
    private final ArtifactoryRunningMode artifactoryRunningMode;

    public ArtifactoryServer(String serverId, long startTime, String contextUrl, int membershipPort,
            ArtifactoryServerState serverState, ArtifactoryServerRole serverRole, long lastHeartbeat,
            String artifactoryVersion, int artifactoryRevision, long artifactoryRelease,
            ArtifactoryRunningMode artifactoryRunningMode, String licenseKeyHash) {

        if (StringUtils.isBlank(serverId)) {
            throw new IllegalArgumentException("Artifactory serverId cannot be empty or null!");
        }
        if (startTime <= 0L) {
            throw new IllegalArgumentException("Artifactory server start time cannot be zero or negative!");
        }
        if (serverState == null || serverRole == null) {
            throw new IllegalArgumentException("Artifactory server state and role cannot be null!");
        }
        if (StringUtils.isBlank(licenseKeyHash)) {
            throw new IllegalArgumentException("Artifactory licenseKeyHash cannot be empty or null!");
        }
        if (artifactoryRunningMode == null) {
            throw new IllegalArgumentException("Artifactory artifactoryRunningMode cannot be null!");
        }
        this.licenseKeyHash = licenseKeyHash;
        this.serverId = serverId.trim();
        this.startTime = startTime;
        this.contextUrl = contextUrl;
        this.membershipPort = membershipPort;
        this.serverState = serverState;
        this.serverRole = serverRole;
        this.lastHeartbeat = lastHeartbeat;
        this.artifactoryVersion = artifactoryVersion;
        this.artifactoryRevision = artifactoryRevision;
        this.artifactoryRelease = artifactoryRelease;
        this.artifactoryRunningMode = artifactoryRunningMode;
    }

    @Nonnull
    public String getServerId() {
        return serverId;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getContextUrl() {
        return contextUrl;
    }

    public int getMembershipPort() {
        return membershipPort;
    }

    public ArtifactoryServerState getServerState() {
        return serverState;
    }

    public ArtifactoryServerRole getServerRole() {
        return serverRole;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public String getArtifactoryVersion() {
        return artifactoryVersion;
    }

    public int getArtifactoryRevision() {
        return artifactoryRevision;
    }

    public long getArtifactoryRelease() {
        return artifactoryRelease;
    }

    public String getLicenseKeyHash() {
        return licenseKeyHash;
    }

    public ArtifactoryRunningMode getArtifactoryRunningMode() {
        return artifactoryRunningMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ArtifactoryServer that = (ArtifactoryServer) o;

        if (!serverId.equals(that.serverId)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return serverId.hashCode();
    }

    @Override
    public String toString() {
        return serverId;
    }
}
