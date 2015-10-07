package org.artifactory.state.model;

import org.artifactory.spring.ReloadableBean;
import org.artifactory.state.ArtifactoryServerState;

/**
 * author: gidis
 */
public interface ArtifactoryStateManager extends ReloadableBean {

    /**
     * Set the {@link org.artifactory.storage.ha.entity.ArtifactoryServer} matching the
     * given {@code serverId} state to the {@code state}
     * <p><i>Use this method with caution.</i>
     *
     * @param serverId the serverId of an existing {@code ArtifactoryServer}
     * @throws IllegalStateException when given {@code serverId} does not match any existing server
     */
    boolean forceState(ArtifactoryServerState state);

    void beforeDestroy();
}
