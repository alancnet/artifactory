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

package org.artifactory.webapp.wicket.page.config.repos;

import com.google.common.collect.Lists;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.p2.P2RepositoryModel;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;

import java.io.Serializable;
import java.util.List;

/**
 * Helper class to RepositoryConfigPage, help syncing between mutableDescriptor model and mutableDescriptor cache
 *
 * @author Eli Givoni
 */
public class CachingDescriptorHelper implements Serializable {

    /**
     * Used as the model for the repositories config page.
     */
    private MutableCentralConfigDescriptor modelMutableDescriptor;

    @SpringBean
    private CentralConfigService centralConfigService;

    private List<P2RepositoryModel> p2RepositoryModels = Lists.newArrayList();

    {
        Injector.get().inject(this);
    }

    public CachingDescriptorHelper(MutableCentralConfigDescriptor mutableDescriptor) {
        this.modelMutableDescriptor = mutableDescriptor;
    }

    /**
     * @return New mutable descriptor loaded from the database.
     */
    public MutableCentralConfigDescriptor getSavedMutableDescriptor() {
        return centralConfigService.getMutableDescriptor();
    }

    /**
     * @return RepositoryConfigPage mutable descriptor model
     */
    public MutableCentralConfigDescriptor getModelMutableDescriptor() {
        return modelMutableDescriptor;
    }

    public void syncAndSaveLocalRepositories() {
        MutableCentralConfigDescriptor configDescriptor = getSavedMutableDescriptor();
        configDescriptor.setLocalRepositoriesMap(modelMutableDescriptor.getLocalRepositoriesMap());
        configDescriptor.setLocalReplications(modelMutableDescriptor.getLocalReplications());
        configDescriptor.setBackups(modelMutableDescriptor.getBackups());
        saveDescriptor(configDescriptor);
    }

    public void syncAndSaveRemoteRepositories() {
        MutableCentralConfigDescriptor configDescriptor = getSavedMutableDescriptor();
        configDescriptor.setRemoteRepositoriesMap(modelMutableDescriptor.getRemoteRepositoriesMap());
        configDescriptor.setRemoteReplications(modelMutableDescriptor.getRemoteReplications());
        configDescriptor.setBackups(modelMutableDescriptor.getBackups());
        saveDescriptor(configDescriptor);
    }

    public void syncAndSaveVirtualRepositories(boolean updateRemotes, boolean updateLocals) {
        MutableCentralConfigDescriptor configDescriptor = getSavedMutableDescriptor();
        configDescriptor.setVirtualRepositoriesMap(modelMutableDescriptor.getVirtualRepositoriesMap());
        if (updateRemotes) {
            configDescriptor.setRemoteRepositoriesMap(modelMutableDescriptor.getRemoteRepositoriesMap());
            configDescriptor.setRemoteReplications(modelMutableDescriptor.getRemoteReplications());
        }
        if (updateLocals) {
            configDescriptor.setLocalRepositoriesMap(modelMutableDescriptor.getLocalRepositoriesMap());
            configDescriptor.setLocalReplications(modelMutableDescriptor.getLocalReplications());
        }
        saveDescriptor(configDescriptor);
    }

    public void setP2RepositoryModels(List<P2RepositoryModel> p2RepositoryModels) {
        this.p2RepositoryModels = p2RepositoryModels;
    }

    public List<P2RepositoryModel> getP2RepositoryModels() {
        return p2RepositoryModels;
    }

    protected void removeRepositoryAndSave(String repoKey) {
        modelMutableDescriptor.removeRepository(repoKey);
        MutableCentralConfigDescriptor savedDescriptor = getSavedMutableDescriptor();
        savedDescriptor.removeRepository(repoKey);
        saveDescriptor(savedDescriptor);
    }

    private void saveDescriptor(MutableCentralConfigDescriptor descriptor) {
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }

    /**
     * reload this repo descriptor in case user canceled (will work also for save)
     */
    public void reloadRepository(String repoKey) {
        CentralConfigDescriptor cc = centralConfigService.getMutableDescriptor();

        RepoDescriptor repoToReload = cc.getLocalRepositoriesMap().get(repoKey);
        if (repoToReload != null) {
            modelMutableDescriptor.getLocalRepositoriesMap().put(repoKey, (LocalRepoDescriptor) repoToReload);
            LocalReplicationDescriptor localReplication = cc.getLocalReplication(repoKey);
            if (localReplication != null) {
                LocalReplicationDescriptor localChangesToRemove = modelMutableDescriptor.getLocalReplication(repoKey);
                modelMutableDescriptor.removeLocalReplication(localChangesToRemove);
                modelMutableDescriptor.addLocalReplication(localReplication);
            }
            return;
        }
        repoToReload = cc.getRemoteRepositoriesMap().get(repoKey);
        if (repoToReload != null) {
            modelMutableDescriptor.getRemoteRepositoriesMap().put(repoKey, (RemoteRepoDescriptor) repoToReload);
            RemoteReplicationDescriptor existingReplication = cc.getRemoteReplication(repoKey);
            if (existingReplication != null) {
                RemoteReplicationDescriptor localChangesToRemove = modelMutableDescriptor.getRemoteReplication(repoKey);
                modelMutableDescriptor.removeRemoteReplication(localChangesToRemove);
                modelMutableDescriptor.addRemoteReplication(existingReplication);
            }
            return;
        }
        repoToReload = cc.getVirtualRepositoriesMap().get(repoKey);
        if (repoToReload != null) {
            modelMutableDescriptor.getVirtualRepositoriesMap().put(repoKey, (VirtualRepoDescriptor) repoToReload);
        }
    }

    /**
     * Reset the transient objects in the caching descriptor
     */
    public void reset() {
        // TODO: check if it's possible to reset the model (it is shared by all the tables)
        //modelMutableDescriptor = getSavedMutableDescriptor();
        p2RepositoryModels.clear();
    }
}
