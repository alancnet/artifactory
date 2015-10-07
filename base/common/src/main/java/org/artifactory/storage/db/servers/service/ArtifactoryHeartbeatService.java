package org.artifactory.storage.db.servers.service;

import org.artifactory.spring.ReloadableBean;

/**
 * author: gidis
 */
public interface ArtifactoryHeartbeatService extends ReloadableBean {
    void updateHeartbeat();
}
