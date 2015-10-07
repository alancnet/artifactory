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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import com.google.common.collect.Lists;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoAlphaComparator;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.md.Properties;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * This is the root node of the tree browser. It contains all the repository nodes.
 *
 * @author Chen Keinan
 *
 */
@JsonTypeName("root")
public class RootNode implements RestTreeNode {

    @Override
    public List<? extends INode> getChildren(AuthorizationService authService, boolean isCompact,
            ArtifactoryRestRequest request) {
        List<INode> repoNodes = new ArrayList<>();
        //Add a tree node for each file repository and local cache repository
        RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
        // get local repo descriptors
        addLocalRepoNodes(repoNodes, repositoryService);
        // add remote repo
        addRemoteRepoNodes(repoNodes, repositoryService, request);
        // add virtual repo
        addVirtualRepoNodes(repoNodes, repositoryService, request);
        return repoNodes;
    }

    /**
     * add virtual repo nodes to repo list
     *  @param repoNodes         - repository nodes list
     * @param repositoryService - repository service
     * @param request
     */
    private void addVirtualRepoNodes(List<INode> repoNodes, RepositoryService repositoryService,
            ArtifactoryRestRequest request) {
        List<VirtualRepoDescriptor> virtualDescriptors = repositoryService.getVirtualRepoDescriptors();
        removeNonPermissionRepositories(virtualDescriptors);
        Collections.sort(virtualDescriptors, new RepoComparator());
        repoNodes.addAll(getVirtualNodes(virtualDescriptors, request));
    }

    /**
     * add remote repo nodes to repo list
     *  @param repoNodes         - repository nodes list
     * @param repositoryService - repository service
     * @param request
     */
    private void addRemoteRepoNodes(List<INode> repoNodes, RepositoryService repositoryService,
            ArtifactoryRestRequest request) {
        List<RemoteRepoDescriptor> remoteDescriptors = repositoryService.getRemoteRepoDescriptors();
        removeNonPermissionRepositories(remoteDescriptors);
        Collections.sort(remoteDescriptors, new RepoComparator());
        repoNodes.addAll(getRemoteNodes(remoteDescriptors, request));
    }

    /**
     * add local repo nodes to repo list
     *
     * @param repoNodes         - repository nodes list
     * @param repositoryService - repository service
     */
    private void addLocalRepoNodes(List<INode> repoNodes, RepositoryService repositoryService) {
        List<LocalRepoDescriptor> localRepos = repositoryService.getLocalAndCachedRepoDescriptors();
        removeNonPermissionRepositories(localRepos);
        Collections.sort(localRepos, new LocalRepoAlphaComparator());
        repoNodes.addAll(getLocalNodes(localRepos));
    }

    /**
     * get Root Node items
     *
     * @param repos - list of repositories
     * @return - list of root node items
     */
    private List<INode> getLocalNodes(List<LocalRepoDescriptor> repos) {
        List<INode> items = Lists.newArrayListWithCapacity(repos.size());
        repos.forEach(repo -> {
            String repoType = repo.getKey().endsWith("-cache") ? "cached" : "local";
            RepositoryNode itemNodes = new RepositoryNode(repo, repoType);
            items.add(itemNodes);
        });
        return items;
    }

    /**
     * get Root Node items
     * @param repos - list of repositories
     * @param request
     * @return - list of root node items
     */
    private List<INode> getRemoteNodes(List<RemoteRepoDescriptor> repos, ArtifactoryRestRequest request) {
        List<INode> items = Lists.newArrayListWithCapacity(repos.size());
        repos.forEach(repo -> {
            if (repo.isListRemoteFolderItems()) {
                String repoType = "remote";
                VirtualRemoteRepositoryNode itemNodes = new VirtualRemoteRepositoryNode(repo, repoType, request);
                items.add(itemNodes);
            }
        });
        return items;
    }

    /**
     * get Root Node items
     *
     * @param repos - list of repositories
     * @param request
     * @return - list of root node items
     */
    private List<INode> getVirtualNodes(List<VirtualRepoDescriptor> repos, ArtifactoryRestRequest request) {
        List<INode> items = Lists.newArrayListWithCapacity(repos.size());
        repos.forEach(repo -> {
            String repoType = "virtual";
            VirtualRemoteRepositoryNode itemNodes = new VirtualRemoteRepositoryNode(repo, repoType, request);
            items.add(itemNodes);
        });
        return items;
    }

    /**
     * remove repositories which user not permit to access
     *
     * @param repositories
     */
    private void removeNonPermissionRepositories(List<? extends RepoDescriptor> repositories) {
        AuthorizationService authorizationService = ContextHelper.get().getAuthorizationService();
        Iterator<? extends RepoDescriptor> repoDescriptors = repositories.iterator();
        while (repoDescriptors.hasNext()) {
            RepoDescriptor repoDescriptor = repoDescriptors.next();
            if (!authorizationService.userHasPermissionsOnRepositoryRoot(repoDescriptor.getKey())) {
                repoDescriptors.remove();
            }
        }
    }

    @Override
    public List<RestModel> fetchItemTypeData(AuthorizationService authService, boolean isCompact,
            Properties props, ArtifactoryRestRequest request) {
        Collection<? extends RestTreeNode> items = getChildren(authService, isCompact, request);
        List<RestModel> treeModel = new ArrayList<>();
        items.forEach(item -> {
            ((INode) item).populateActions(authService);
            // populate tabs
            ((INode) item).populateTabs(authService);
            // update additional data
            ((INode) item).updateNodeData();
            treeModel.add(item);
        });
        return treeModel;
    }

    private static class RepoComparator implements Comparator<RepoBaseDescriptor> {
        @Override
        public int compare(RepoBaseDescriptor descriptor1, RepoBaseDescriptor descriptor2) {

            //Local repositories can be either ordinary or caches
            if (descriptor1 instanceof LocalRepoDescriptor) {
                boolean repo1IsCache = ((LocalRepoDescriptor) descriptor1).isCache();
                boolean repo2IsCache = ((LocalRepoDescriptor) descriptor2).isCache();

                //Cache repositories should appear in a higher priority
                if (repo1IsCache && !repo2IsCache) {
                    return 1;
                } else if (!repo1IsCache && repo2IsCache) {
                    return -1;
                }
            }
            return descriptor1.getKey().compareTo(descriptor2.getKey());
        }
    }
}