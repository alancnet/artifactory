package org.artifactory.addon.ha;

import org.artifactory.addon.Addon;
import org.artifactory.addon.ha.message.HaMessage;
import org.artifactory.addon.ha.message.HaMessageTopic;
import org.artifactory.addon.ha.semaphore.SemaphoreWrapper;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;

import java.util.List;

/**
 * @author mamo, gidi, fred
 */
public interface HaCommonAddon extends Addon {

    /**
     * Response header for an HA Artifactory Node
     */
    String ARTIFACTORY_NODE_ID = "X-Artifactory-Node-Id";

    /**
     * Server ID in case of a non HA Artifactory node
     */
    String ARTIFACTORY_PRO = "Artifactory";

    String STATS_SEMAPHORE_NAME = "flushStatsSemaphore";
    String STATS_REMOTE_SEMAPHORE_NAME = "flushRemoteStatsSemaphore";
    String INDEXING_SEMAPHORE_NAME = "indexingSemaphore";
    String INDEX_MARKED_ARCHIVES_SEMAPHORE_NAME = "indexMarkedArchivesSemaphore";
    int DEFAULT_SEMAPHORE_PERMITS = 1;

    /**
     * determines if HA is enabled.
     * <p>that is {@link org.artifactory.common.ArtifactoryHome#ARTIFACTORY_HA_NODE_PROPERTIES_FILE} exists and license type
     * is HA or Trial
     *
     * @return {@code true} if the current Artifactory instance is HA enabled
     */
    boolean isHaEnabled();

    /**
     * @return {@code true} if HA is enabled and activated, and current Artifactory instance is the primary.
     *         <p>is HA is <b>not enabled</b>, return true
     */
    boolean isPrimary();

    /**
     * @return {@code true} if HA is configured.
     */
    boolean isHaConfigured();

    //todo move into HaAddon, should not be a common interface
    void notify(HaMessageTopic haMessageTopic, HaMessage haMessage);

    /**
     * @return A unique hostId for a Pro/OSS installation, a hashed token for an HA node
     */
    String getHostId();

    SemaphoreWrapper getSemaphore(String semaphoreName);

    void shutdown();

    List<ArtifactoryServer> getAllArtifactoryServers();

    boolean deleteArtifactoryServer(String id);

    boolean artifactoryServerHasHeartbeat(ArtifactoryServer artifactoryServer);

}
