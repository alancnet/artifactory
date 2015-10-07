package org.artifactory.util;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;
import org.artifactory.version.CompoundVersionDetails;

/**
 * author: gidis
 */
public abstract class ArtifactoryServerHelper {

    public static ArtifactoryServer createArtifactoryServer(String serverId, String serverContextUrl,
            int clusterPort, CompoundVersionDetails versionDetails, ArtifactoryServerState serverState,
            ArtifactoryServerRole serverRole,
            ArtifactoryRunningMode artifactoryRunningMode) {

        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        long startTime = System.currentTimeMillis(); //todo pass real start time before bootstrapping started
        String licenseKeyHash = addonsManager.getLicenseKeyHash();
        return new ArtifactoryServer(
                serverId,
                startTime,
                serverContextUrl,
                clusterPort,
                serverState,
                serverRole,
                System.currentTimeMillis(),
                versionDetails.getVersionName(),
                versionDetails.getRevisionInt(),
                versionDetails.getTimestamp(),
                artifactoryRunningMode, licenseKeyHash
        );
    }
}
