package org.artifactory.storage.db.servers.service;

import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A service to interact with the artifactory servers table.
 *
 * @author Yossi Shaul
 */
public interface ArtifactoryServersService {
    List<ArtifactoryServer> getAllArtifactoryServers();

    /**
     * Get the {@link ArtifactoryServer} instance from storage with the given {@code serverId}
     *
     * @param nodeId The unique server id
     * @return {@link ArtifactoryServer} if exists, otherwise null
     */
    @Nullable
    ArtifactoryServer getArtifactoryServer(String nodeId);

    @Transactional
    int createArtifactoryServer(ArtifactoryServer artifactoryServer);

    @Transactional
    int updateArtifactoryServer(ArtifactoryServer artifactoryServer);

    @Transactional
    int updateArtifactoryServerState(String serverId, ArtifactoryServerState newState);

    @Transactional
    int updateArtifactoryServerRole(String serverId, ArtifactoryServerRole newRole);

    @Transactional
    int updateArtifactoryJoinPort(String serverId, int port);

    @Transactional
    boolean removeServer(String serverId);

    @Transactional
    int updateArtifactoryServerHeartbeat(String serverId, long heartbeat);
}
