package org.artifactory.storage.db.servers.service;

import com.google.common.base.Predicate;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * author: gidis
 */
public interface ArtifactoryServersCommonService {

    /**
     * Get the current running primary HA member
     *
     * @return {@link org.artifactory.storage.db.servers.model.ArtifactoryServer} if exists, otherwise null
     */
    @Nullable
    ArtifactoryServer getRunningHaPrimary();

    /**
     * Get the current server
     */
    ArtifactoryServer getCurrentMember();

    /**
     * Get al the other active (state=STARTING,RUNNING,STOPPING) servers
     */
    List<ArtifactoryServer> getOtherActiveMembers();

    /**
     * Get al the other active (state=STARTING,RUNNING,STOPPING) servers
     */
    List<ArtifactoryServer> getActiveMembers();

    /**
     * Get al the other running HA(state=RUNNING and license=HA) servers
     */
    List<ArtifactoryServer> getOtherRunningHaMembers();

    /**
     * Gets Artifactory server from database by serverId
     */
    ArtifactoryServer getArtifactoryServer(String serverId);

    /**
     * Returns all the ArtifactoryServers from the database.
     */
    List<ArtifactoryServer> getAllArtifactoryServers();

    void updateArtifactoryServerRole(String serverId, ArtifactoryServerRole newRole);

    void updateArtifactoryJoinPort(String serverId, int port);

    void updateArtifactoryServerState(ArtifactoryServer server, ArtifactoryServerState newState);

    void createArtifactoryServer(ArtifactoryServer artifactoryServer);

    void updateArtifactoryServer(ArtifactoryServer artifactoryServer);

    boolean removeServer(String serverId);

    void updateArtifactoryServerHeartbeat(String serverId, long heartBeat);

    //predicates
    //todo remove from here
    public static final Predicate<ArtifactoryServer> isOther = new Predicate<ArtifactoryServer>() {
        @Override
        public boolean apply(ArtifactoryServer input) {
            final String serverId = ContextHelper.get().getServerId();
            return !input.getServerId().trim().equals(serverId);
        }
    };

    public static final Predicate<ArtifactoryServer> isRunning = new Predicate<ArtifactoryServer>() {
        @Override
        public boolean apply(ArtifactoryServer server) {
            return server.getServerState() == ArtifactoryServerState.RUNNING;
        }
    };

    public static final Predicate<ArtifactoryServer> isStarting = new Predicate<ArtifactoryServer>() {
        @Override
        public boolean apply(ArtifactoryServer server) {
            return server.getServerState() == ArtifactoryServerState.STARTING;
        }
    };

    public static final Predicate<ArtifactoryServer> isStopping = new Predicate<ArtifactoryServer>() {
        @Override
        public boolean apply(ArtifactoryServer server) {
            return server.getServerState() == ArtifactoryServerState.STOPPING;
        }
    };

    public static final Predicate<ArtifactoryServer> isConverting = new Predicate<ArtifactoryServer>() {
        @Override
        public boolean apply(ArtifactoryServer server) {
            return server.getServerState() == ArtifactoryServerState.CONVERTING;
        }
    };

    public static final Predicate<ArtifactoryServer> hasHeartbeat = new Predicate<ArtifactoryServer>() {
        @Override
        public boolean apply(ArtifactoryServer server) {
            long lastHeartbeatBeforeSecs = TimeUnit.MILLISECONDS.toSeconds(
                    System.currentTimeMillis() - server.getLastHeartbeat());
            return lastHeartbeatBeforeSecs <= ConstantValues.haHeartbeatStaleIntervalSecs.getInt();
        }
    };

    public static final Predicate<ArtifactoryServer> validForRemoval = new Predicate<ArtifactoryServer>() {
        @Override
        public boolean apply(ArtifactoryServer server) {
            long lastHeartbeatBeforeSecs = TimeUnit.MILLISECONDS.toSeconds(
                    System.currentTimeMillis() - server.getLastHeartbeat());
            return lastHeartbeatBeforeSecs >= (ConstantValues.haHeartbeatStaleIntervalSecs.getInt() * 5);
        }
    };

    public static final Predicate<ArtifactoryServer> isPrimary = new Predicate<ArtifactoryServer>() {
        @Override
        public boolean apply(ArtifactoryServer server) {
            return server.getServerRole() == ArtifactoryServerRole.PRIMARY;
        }
    };
}

