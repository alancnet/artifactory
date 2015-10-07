package org.artifactory.storage.db.servers.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.or;

/**
 * author: gidis
 */
@Service
public class ArtifactoryServersCommonServiceImpl implements ArtifactoryServersCommonService {

    @Autowired
    private ArtifactoryServersService serversService;

    @Nullable
    @Override
    public ArtifactoryServer getRunningHaPrimary() {
        List<ArtifactoryServer> servers = Lists.newArrayList(
                Iterables.filter(serversService.getAllArtifactoryServers(), and(isPrimary, isRunning)));
        if (servers.size() > 1) {
            throw new IllegalStateException(
                    "Found " + servers.size() + " running primary nodes where only 1 is allowed.");
        }
        return !servers.isEmpty() ? servers.get(0) : null;
    }

    @Override
    public List<ArtifactoryServer> getOtherRunningHaMembers() {
        return Lists.newArrayList(
                Iterables.filter(serversService.getAllArtifactoryServers(), and(isOther, isRunning, hasHeartbeat)));
    }

    @Override
    public ArtifactoryServer getArtifactoryServer(String serverId) {
        return serversService.getArtifactoryServer(serverId);
    }

    @Override
    public void updateArtifactoryServerRole(String serverId, ArtifactoryServerRole newRole) {
        serversService.updateArtifactoryServerRole(serverId, newRole);
    }

    @Override
    public void updateArtifactoryJoinPort(String serverId, int joinPort) {
        serversService.updateArtifactoryJoinPort(serverId, joinPort);
    }

    @Override
    public void updateArtifactoryServerState(ArtifactoryServer server, ArtifactoryServerState newState) {
        serversService.updateArtifactoryServerState(server.getServerId(), newState);
    }

    @Override
    public List<ArtifactoryServer> getAllArtifactoryServers() {
        return serversService.getAllArtifactoryServers();
    }

    @Override
    public void createArtifactoryServer(ArtifactoryServer artifactoryServer) {
        serversService.createArtifactoryServer(artifactoryServer);
    }

    @Override
    public void updateArtifactoryServer(ArtifactoryServer artifactoryServer) {
        serversService.updateArtifactoryServer(artifactoryServer);
    }

    @Override
    public boolean removeServer(String serverId) {
        return serversService.removeServer(serverId);
    }

    @Override
    public void updateArtifactoryServerHeartbeat(String serverId, long heartBeat) {
        serversService.updateArtifactoryServerHeartbeat(serverId, heartBeat);
    }

    @Override
    public List<ArtifactoryServer> getOtherActiveMembers() {
        return Lists.newArrayList(Iterables.filter(getActiveMembers(), and(isOther)));
    }

    @Override
    public List<ArtifactoryServer> getActiveMembers() {
        return Lists.newArrayList(Iterables.filter(serversService.getAllArtifactoryServers(),
                and(or(isRunning, isStarting, isStopping, isConverting), hasHeartbeat)));
    }

    @Override
    public ArtifactoryServer getCurrentMember() {
        String serverId = ContextHelper.get().getServerId();
        return serversService.getArtifactoryServer(serverId);
    }
}
