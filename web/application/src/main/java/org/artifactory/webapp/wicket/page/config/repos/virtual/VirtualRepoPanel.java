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

package org.artifactory.webapp.wicket.page.config.repos.virtual;

import com.google.common.collect.Lists;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.artifactory.addon.p2.P2Repository;
import org.artifactory.addon.p2.P2RepositoryModel;
import org.artifactory.addon.p2.P2WebAddon;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.util.CollectionUtils;
import org.artifactory.webapp.wicket.page.config.repos.CachingDescriptorHelper;
import org.artifactory.webapp.wicket.page.config.repos.RepoConfigCreateUpdatePanel;

import java.util.List;
import java.util.Map;

/**
 * Virtual repository configuration panel.
 *
 * @author Yossi Shaul
 */
public class VirtualRepoPanel extends RepoConfigCreateUpdatePanel<VirtualRepoDescriptor> {

    private final CreateUpdateAction action;

    public VirtualRepoPanel(CreateUpdateAction action, VirtualRepoDescriptor repoDescriptor,
            CachingDescriptorHelper cachingDescriptorHelper) {
        super(action, repoDescriptor, cachingDescriptorHelper);
        this.action = action;
    }

    @Override
    protected List<ITab> getConfigurationTabs() {
        List<ITab> tabs = Lists.newArrayList();

        tabs.add(new AbstractTab(Model.of("Basic Settings")) {
            @Override
            public Panel getPanel(String panelId) {
                return new VirtualRepoBasicPanel(panelId, action, getRepoDescriptor(), getCachingDescriptorHelper());
            }
        });

        tabs.add(new AbstractTab(Model.of("Advanced Settings")) {
            @Override
            public Panel getPanel(String panelId) {
                return new VirtualRepoAdvancedPanel(panelId, action, getRepoDescriptor(), form);
            }
        });

        tabs.add(addons.addonByType(P2WebAddon.class).getVirtualRepoConfigurationTab(
                "P2", getRepoDescriptor(), getCachingDescriptorHelper()));

        tabs.add(new AbstractTab(Model.of("Packages")) {
            @Override
            public Panel getPanel(String panelId) {
                return new VirtualRepoPackagesPanel(panelId, entity, isCreate());
            }
        });

        return tabs;
    }

    @Override
    public void addAndSaveDescriptor(VirtualRepoDescriptor virtualRepo) {
        CachingDescriptorHelper helper = getCachingDescriptorHelper();
        MutableCentralConfigDescriptor mccd = helper.getModelMutableDescriptor();
        mccd.addVirtualRepository(virtualRepo);

        processP2Configuration(virtualRepo, helper);
    }

    @Override
    public void saveEditDescriptor(VirtualRepoDescriptor repoDescriptor) {
        CachingDescriptorHelper helper = getCachingDescriptorHelper();
        //update the model being saved
        Map<String, VirtualRepoDescriptor> virtualRepos =
                helper.getModelMutableDescriptor().getVirtualRepositoriesMap();
        if (virtualRepos.containsKey(repoDescriptor.getKey())) {
            virtualRepos.put(repoDescriptor.getKey(), repoDescriptor);
        }

        processP2Configuration(repoDescriptor, helper);
    }

    private void processP2Configuration(VirtualRepoDescriptor virtualRepo, CachingDescriptorHelper helper) {
        if(virtualRepo.getP2() == null || !virtualRepo.getType().equals(RepoType.P2)) {
            helper.syncAndSaveVirtualRepositories(false, false);
            return;
        }
        boolean updateRemotes = false;
        boolean updateLocals = false;
        // go over the p2 remote and local repos if any and perform required action
        List<P2RepositoryModel> p2RepositoryModels = helper.getP2RepositoryModels();
        if (CollectionUtils.notNullOrEmpty(p2RepositoryModels)) {
            for (P2RepositoryModel p2RepositoryModel : p2RepositoryModels) {
                // only perform action if the action checkbox is selected
                if (p2RepositoryModel.isSelected()) {
                    P2Repository p2Repository = p2RepositoryModel.getP2Repository();
                    if (p2Repository.isToCreate()) {
                        updateRemotes = handleP2ToCreate(helper, p2Repository);
                    } else if (p2Repository.isModified()) {
                        if (p2Repository.isRemote()) {
                            handleIsModifiedRemote(helper, p2Repository);
                            updateRemotes = true;
                        } else {
                            handleIsModifiedLocal(helper, p2Repository);
                            updateLocals = true;
                        }
                    }

                    if (!p2Repository.isAlreadyIncluded()) {
                        // add the local/remote repository to the aggregation list of the virtual
                        List<RepoDescriptor> repositories = virtualRepo.getRepositories();
                        if (!repositories.contains(p2Repository.getDescriptor())) {
                            repositories.add(p2Repository.getDescriptor());
                            if (p2Repository.isRemote()) {
                                updateRemotes = true;
                            } else {
                                updateLocals = true;
                            }
                        }
                    }
                }
            }
        }

        helper.syncAndSaveVirtualRepositories(updateRemotes, updateLocals);
    }

    private void handleIsModifiedLocal(CachingDescriptorHelper helper, P2Repository p2Repository) {
        LocalRepoDescriptor localRepoDescriptor = (LocalRepoDescriptor) p2Repository.getDescriptor();
        // replace local repository configuration
        helper.getModelMutableDescriptor().getLocalRepositoriesMap().put(
                localRepoDescriptor.getKey(), localRepoDescriptor);
    }

    private void handleIsModifiedRemote(CachingDescriptorHelper helper, P2Repository p2Repository) {
        RemoteRepoDescriptor remoteRepoDescriptor = (RemoteRepoDescriptor) p2Repository.getDescriptor();
        // replace remote repository configuration
        helper.getModelMutableDescriptor().getRemoteRepositoriesMap().put(remoteRepoDescriptor.getKey(),
                remoteRepoDescriptor);
    }

    private boolean handleP2ToCreate(CachingDescriptorHelper helper, P2Repository p2Repository) {
        if (p2Repository.isRemote()) {
            // add new remote repository
            helper.getModelMutableDescriptor().addRemoteRepository((RemoteRepoDescriptor) p2Repository.getDescriptor());
            return true;
        }
        return false;
    }

    @Override
    protected boolean validate(VirtualRepoDescriptor repoDescriptor) {
        return true;
    }
}
