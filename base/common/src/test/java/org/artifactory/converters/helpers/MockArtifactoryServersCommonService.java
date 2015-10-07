/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.converters.helpers;

import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.artifactory.version.ArtifactoryVersion;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gidi Shabat
 */
public class MockArtifactoryServersCommonService implements ArtifactoryServersCommonService {
    public MockArtifactoryServersCommonService(ArtifactoryVersion version) {

    }

    @Nullable
    @Override
    public ArtifactoryServer getRunningHaPrimary() {
        return null;
    }

    @Override
    public ArtifactoryServer getCurrentMember() {
        return null;
    }

    @Override
    public List<ArtifactoryServer> getOtherActiveMembers() {
        return new ArrayList<>();
    }

    @Override
    public List<ArtifactoryServer> getActiveMembers() {
        return null;
    }

    @Override
    public List<ArtifactoryServer> getOtherRunningHaMembers() {
        return null;
    }

    @Override
    public ArtifactoryServer getArtifactoryServer(String serverId) {
        return null;
    }

    @Override
    public List<ArtifactoryServer> getAllArtifactoryServers() {
        return null;
    }

    @Override
    public void updateArtifactoryServerRole(String serverId, ArtifactoryServerRole newRole) {

    }

    @Override
    public void updateArtifactoryJoinPort(String serverId, int port) {

    }

    @Override
    public void updateArtifactoryServerState(ArtifactoryServer server, ArtifactoryServerState newState) {

    }

    @Override
    public void createArtifactoryServer(ArtifactoryServer artifactoryServer) {

    }

    @Override
    public void updateArtifactoryServer(ArtifactoryServer artifactoryServer) {

    }

    @Override
    public boolean removeServer(String serverId) {
        return false;
    }

    @Override
    public void updateArtifactoryServerHeartbeat(String serverId, long heartBeat) {

    }
}
