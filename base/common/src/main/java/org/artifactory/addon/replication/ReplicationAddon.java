/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
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

package org.artifactory.addon.replication;

import org.artifactory.addon.Addon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.rest.replication.ReplicationStatus;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.repo.RepoPath;

import java.io.IOException;

/**
 * @author Noam Y. Tenne
 */
public interface ReplicationAddon extends Addon {

    //replication
    String PROP_REPLICATION_PREFIX = "artifactory.replication.";
    String PROP_REPLICATION_STARTED_SUFFIX = ".started";
    String PROP_REPLICATION_FINISHED_SUFFIX = ".finished";
    String PROP_REPLICATION_RESULT_SUFFIX = ".result";
    String TASK_MANUAL_DESCRIPTOR = "task_manual_settings";

    BasicStatusHolder performRemoteReplication(RemoteReplicationSettings settings) throws IOException;

    BasicStatusHolder performLocalReplication(LocalReplicationSettings settings) throws IOException;

    void scheduleImmediateLocalReplicationTask(LocalReplicationDescriptor replicationDescriptor,
            BasicStatusHolder statusHolder);

    void scheduleImmediateRemoteReplicationTask(RemoteReplicationDescriptor replicationDescriptor,
            BasicStatusHolder statusHolder);

    ReplicationStatus getReplicationStatus(RepoPath repoPath);

    void offerLocalReplicationDeploymentEvent(RepoPath repoPath);

    void offerLocalReplicationMkDirEvent(RepoPath repoPath);

    void offerLocalReplicationDeleteEvent(RepoPath repoPath);

    void offerLocalReplicationPropertiesChangeEvent(RepoPath repoPath);

    void validateTargetIsDifferentInstance(ReplicationBaseDescriptor descriptor, RealRepoDescriptor repoDescriptor)
            throws IOException;

    boolean isRepoExistInCache(RepoPath repoPath);

    void validateTargetLicense(ReplicationBaseDescriptor descriptor,RealRepoDescriptor repoDescriptor,int numOfReplicationConfigured)
            throws IOException;

    /**
     * When a local replication is removed, or changes it's url call this method to get rid of it's (now) unused properties
     */
    void cleanupLocalReplicationProperties(LocalReplicationDescriptor replication);

    public enum Overwrite {
        never, force
    }
}
