package org.artifactory.state;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.license.VerificationResult;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.spring.ContextReadinessListener;
import org.artifactory.spring.Reloadable;
import org.artifactory.spring.ReloadableBean;
import org.artifactory.state.model.ArtifactoryStateManager;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.artifactory.util.ArtifactoryServerHelper;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.artifactory.state.ArtifactoryServerState.*;
import static org.artifactory.storage.db.servers.model.ArtifactoryServerRole.STANDALONE;

/**
 * @author Gidi Shabat
 */
@Reloadable(beanClass = ArtifactoryStateManager.class,
        initAfter = {DbService.class, InternalCentralConfigService.class})
public class ArtifactoryStateManagerImpl implements ArtifactoryStateManager, ReloadableBean, ContextReadinessListener {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryStateManagerImpl.class);

    @Autowired
    private ArtifactoryServersCommonService serversService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void init() {
        ArtifactoryRunningMode runningMode = addonsManager.getArtifactoryRunningMode();
        log.debug("Starting Artifactory in running mode: {}", runningMode.name());
        VerificationResult result = addonsManager.verifyAllArtifactoryServers();
        switch (result) {
            case valid: {
                initHa();
                break;
            }
            case converting: {
                updateArtifactoryServerState(STOPPED);
                throw new RuntimeException(result.showMassage());
            }
            case notSameVersion: {
                updateArtifactoryServerState(STOPPED);
                throw new RuntimeException(result.showMassage());
            }
            case duplicateServerIds: {
                long lastHeartbeat = serversService.getCurrentMember().getLastHeartbeat();
                throw new RuntimeException("Current Artifactory node last heartbeat is: " + lastHeartbeat + ". " +
                        result.showMassage());
            }
            default: {
                updateArtifactoryServerState(OFFLINE);
                log.error(result.showMassage());
            }
        }
    }

    private void initHa() {
        boolean converting = ContextHelper.get().getConverterManager().isConverting();
        updateArtifactoryServerState(converting ? CONVERTING : STARTING);
        addonsManager.addonByType(HaAddon.class).init();
    }

    @Override
    public void onContextReady() {
        ArtifactoryServer artifactoryServer = serversService.getArtifactoryServer(getServerId());
        if (artifactoryServer == null) {
            throw new IllegalStateException("Artifactory server could not be found [" + getServerId() + "]");
        }
        // Allow to change the state only if not offline and the previous state was STARTING
        boolean allowedToPromote = STARTING == artifactoryServer.getServerState() || CONVERTING == artifactoryServer.getServerState();
        if (!ContextHelper.get().isOffline() && allowedToPromote) {
            addonsManager.addonByType(HaAddon.class).updateArtifactoryServerRole();
            updateArtifactoryServerState(RUNNING);
        } else {
            log.debug("Changing from {} to {} is allowed only if the previous step was {}",
                    artifactoryServer.getServerState(), RUNNING.getPrettyName(), STARTING.getPrettyName());
        }
    }

    @Override
    public void beforeDestroy() {
        updateArtifactoryServerState(STOPPING);
    }

    @Override
    public void destroy() {
        updateArtifactoryServerState(STOPPED);
    }

    @Override
    public boolean forceState(ArtifactoryServerState state) {
        // Currently we do not support to change state during offline
        if (ContextHelper.get().isOffline() && ArtifactoryServerState.OFFLINE != state) {
            log.warn("The change in Artifactory state to {} will take effect only after restart",
                    OFFLINE.getPrettyName());
            return false;
        }
        if (ArtifactoryServerState.OFFLINE == state) {
            ContextHelper.get().setOffline();
        } else if (ArtifactoryServerState.RUNNING == state) {
            addonsManager.addonByType(HaAddon.class).updateArtifactoryServerRole();
        }
        updateArtifactoryServerState(state);

        ArtifactoryServer artifactoryServer = serversService.getCurrentMember();
        log.debug("Artifactory server '{}' state is {}", artifactoryServer.getServerId(),
                artifactoryServer.getServerState().getPrettyName());
        return true;
    }

    private ArtifactoryServer createOrUpdateCurrentArtifactoryServer(String serverId, String serverContextUrl,
            ArtifactoryServerState serverState) {
        ArtifactoryRunningMode runningMode = addonsManager.getArtifactoryRunningMode();
        CompoundVersionDetails runningVersion = ContextHelper.get().getVersionProvider().getRunning();

        ArtifactoryServer prevArtifactoryServer = serversService.getArtifactoryServer(serverId);
        ArtifactoryServer artifactoryServer;
        if (prevArtifactoryServer == null) {
            int joinPort = 0; //initial cluster port is set to 0
            artifactoryServer = ArtifactoryServerHelper.createArtifactoryServer(serverId, serverContextUrl, joinPort,
                    runningVersion, serverState, STANDALONE, runningMode);
            serversService.createArtifactoryServer(artifactoryServer);
            log.debug("Created Artifactory server: {} state: {}",
                    serverId, artifactoryServer.getServerState().getPrettyName());
        } else {
            int joinPort = 0; //initial cluster port is set to 0 //todo avoid it, not used anyway
            artifactoryServer = ArtifactoryServerHelper.createArtifactoryServer(serverId, serverContextUrl, joinPort,
                    runningVersion, serverState, prevArtifactoryServer.getServerRole(), runningMode);
            verifyArtifactoryServerUpdate(serverId, prevArtifactoryServer, artifactoryServer);
            serversService.updateArtifactoryServer(artifactoryServer);

            log.debug("Updated Artifactory server: {} state: {}", serverId,
                    artifactoryServer.getServerState().getPrettyName());
        }
        return artifactoryServer;
    }

    private String getServerId() {
        final String serverId = ContextHelper.get().getServerId();
        if (StringUtils.isBlank(serverId)) {
            throw new IllegalArgumentException("Artifactory serverId cannot be empty or null!");
        }
        return serverId;
    }

    private void updateArtifactoryServerState(ArtifactoryServerState state) {
        final String serverId = getServerId();
        String contextUrl = "";
        try {
            HaNodeProperties haNodeProperties = ArtifactoryHome.get().getHaNodeProperties();
            // TODO: [by sy] Pro -> HA should reload config or do something else here, otherwise will be empty string
            contextUrl = haNodeProperties != null ? haNodeProperties.getContextUrl() : "";
        } catch (Exception e) {
            log.error("failed to find Artifactory contextUrl in ha-node properties");
        }
        //Create or update server row
        createOrUpdateCurrentArtifactoryServer(serverId, contextUrl, state);
    }

    private void verifyArtifactoryServerUpdate(String serverId, ArtifactoryServer prevArtifactoryServer,
            ArtifactoryServer artifactoryServer) {
        //todo only master?
        if (artifactoryServer.getServerState() != prevArtifactoryServer.getServerState()) {
            log.debug("Changing Artifactory server: '{}' state: {} to state: {}", serverId,
                    prevArtifactoryServer.getServerState(), artifactoryServer.getServerState());
        }
        if (!artifactoryServer.getContextUrl().equals(prevArtifactoryServer.getContextUrl())) {
            log.debug("Previous Artifactory server: '{}' has different context URL {}. Updating {}", serverId,
                    prevArtifactoryServer.getContextUrl(), artifactoryServer.getContextUrl());
        }
    }

    @Override
    public void onContextCreated() {
    }

    @Override
    public void onContextUnready() {
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
    }
}