package org.artifactory.ui.rest.service.utils.repositories;

import com.google.common.collect.Lists;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.utils.repositories.RepoKeyType;
import org.artifactory.ui.rest.model.utils.repositories.RepositoriesData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllRepositoriesService implements RestService {

    @Autowired
    AuthorizationService authorizationService;
    @Autowired
    AclService aclService;

    @Autowired
    RepositoryService repoService;

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        Boolean userOnly = Boolean.valueOf(request.getQueryParamByKey("user"));
        Boolean localOnly = Boolean.valueOf(request.getQueryParamByKey("local"));
        Boolean searchOnly = Boolean.valueOf(request.getQueryParamByKey("search"));
        Boolean localRemoteOnly = Boolean.valueOf(request.getQueryParamByKey("all"));
        Boolean permission = Boolean.valueOf(request.getQueryParamByKey("permission"));


        RepositoriesData repositoriesData = new RepositoriesData();
        // get repo data for search if require
        if (searchOnly) {
            searchRepoData(response, repositoriesData);
            return;
        } else if (localRemoteOnly) {
            List<RepoKeyType> allRepositoriesData = getAllRepositoriesData();
            repositoriesData.setRepoTypesList(allRepositoriesData);
            response.iModelList(allRepositoriesData);
            return;
        } else if (permission) {
            List<RepoKeyType> allRepositoriesData = getPermissionRepoData();
            repositoriesData.setRepoTypesList(allRepositoriesData);
            response.iModel(repositoriesData);
        } else {
            /// get repo data for local or user
            getRepositoriesData(userOnly, localOnly, repositoriesData);
            response.iModel(repositoriesData);
        }
    }

    /**
     * get repository list for search
     *
     * @param artifactoryResponse - encapsulated data related to response
     * @param repositoriesData    - repository data
     */
    private void searchRepoData(RestResponse artifactoryResponse, RepositoriesData repositoriesData) {
        List<RepoKeyType> orderdRepoKeys = getOrderdRepoKeys();
        repositoriesData.setRepoTypesList(orderdRepoKeys);
        artifactoryResponse.iModel(repositoriesData);
    }

    /**
     * return repositories data for user / local / all
     *
     * @param userOnly  - if true return only user repositories
     * @param localOnly - if true return only local repositories
     * @return - list of repositories
     */
    private void getRepositoriesData(Boolean userOnly, Boolean localOnly, RepositoriesData repositoriesData) {
        List<String> repoData;
        if (userOnly) {
            repoData = getRepoData(true);
            List<RepoKeyType> deployReposes = new ArrayList<>();
            repoData.forEach(repoKey -> {
                Map<String, LocalRepoDescriptor> localRepositoriesMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
                LocalRepoDescriptor localRepoDescriptor = localRepositoriesMap.get(repoKey);
                deployReposes.add(new RepoKeyType(localRepoDescriptor.getType(), repoKey));
            });
            if (authorizationService.isAdmin()) {
                repositoriesData.setRepoTypesList(deployReposes);
            } else {
                repositoriesData.setRepoTypesList(getUserRepoForDeploy(deployReposes));
            }
        } else {
            repoData = getRepoData(localOnly);
            repositoriesData.setRepoList(repoData);
        }
    }

    /**
     * return repo data list from config descriptor
     *
     * @return repo data list
     */
    private List<String> getRepoData(boolean localOnly) {
        List<String> repos = new ArrayList<>();
        Map<String, LocalRepoDescriptor> localRepoDescriptorMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
        // add remote repositories
        if (!localOnly) {
            Map<String, RemoteRepoDescriptor> remoteRepoDescriptorMap = centralConfigService.getDescriptor().getRemoteRepositoriesMap();
            repos.addAll(remoteRepoDescriptorMap.keySet());
        }
        repos.addAll(localRepoDescriptorMap.keySet());
        return repos;
    }

    /**
     * get list of local repository allowed for this user to deploy
     *
     * @return - list of repositories
     */
    private List<RepoKeyType> getUserRepoForDeploy(List<RepoKeyType> localRepos) {
        List<RepoKeyType> userAuthorizedRepos = Lists.newArrayList();
        localRepos.forEach(repoType -> {
            RepoPath repoPath = InternalRepoPathFactory.create(repoType.getRepoKey(), "");
            if (!repoType.getRepoKey().endsWith("-cached") && authorizationService.canDeploy(repoPath)) {
                userAuthorizedRepos.add(repoType);
            }
        });
        return userAuthorizedRepos;
    }


    private List<RepoKeyType> getOrderdRepoKeys() {
        List<LocalRepoDescriptor> repoSet = Lists.newArrayList();
        List<LocalRepoDescriptor> localAndCachedRepoDescriptors = repoService.getLocalAndCachedRepoDescriptors();
        Collections.sort(localAndCachedRepoDescriptors, new LocalAndCachedDescriptorsComparator());
        localAndCachedRepoDescriptors
                .stream()
                .filter(descriptor -> authorizationService.canRead(
                        InternalRepoPathFactory.repoRootPath(descriptor.getKey())))
                .forEach(repoSet::add);
        List<RepoKeyType> repoKeys = Lists.newArrayList();
        for (LocalRepoDescriptor descriptor : repoSet) {
            String type;
            if (descriptor.isCache()) {
                type = "remote";
            } else {
                type = "local";
            }
            repoKeys.add(new RepoKeyType(type, descriptor.getKey()));
        }
        return repoKeys;
    }

    /**
     * Comparator that compares local and cached repositories according to their type (local or cached local) and then
     * internally sorting them by their key.
     */
    private static class LocalAndCachedDescriptorsComparator implements Comparator<RepoDescriptor> {
        @Override
        public int compare(RepoDescriptor o1, RepoDescriptor o2) {
            if (o1 instanceof LocalCacheRepoDescriptor && !(o2 instanceof LocalCacheRepoDescriptor)) {
                return 1;
            } else if (!(o1 instanceof LocalCacheRepoDescriptor) && (o2 instanceof LocalCacheRepoDescriptor)) {
                return -1;
            } else {
                return o1.getKey().toLowerCase().compareTo(o2.getKey().toLowerCase());
            }
        }
    }

    /**
     * return remote and local repository data
     *
     * @return list of repositories repo keys
     */
    private List<RepoKeyType> getAllRepositoriesData() {
        List<RepoKeyType> repos = getLocalRepoKeyTypes();
        Map<String, RemoteRepoDescriptor> remoteRepoDescriptorMap = centralConfigService.getDescriptor().getRemoteRepositoriesMap();
        remoteRepoDescriptorMap.keySet().forEach(key -> repos.add(new RepoKeyType("remote", key)));
        return repos;
    }

    private List<RepoKeyType> getPermissionRepoData() {
        List<RepoKeyType> repos = new ArrayList<>();
        List<RepoDescriptor> localAndRemoteRepoDescriptors = repoService.getLocalAndRemoteRepoDescriptors();
        localAndRemoteRepoDescriptors.forEach(reppoDesc -> {
            if (reppoDesc instanceof RemoteRepoDescriptor) {
                repos.add(new RepoKeyType("remote", reppoDesc.getKey()));
            } else {
                repos.add(new RepoKeyType("local", reppoDesc.getKey()));
            }
        });
        return repos;

    }

    private List<RepoKeyType> getLocalRepoKeyTypes() {
        List<RepoKeyType> repos = new ArrayList<>();
        Map<String, LocalRepoDescriptor> localRepoDescriptorMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
        localRepoDescriptorMap.values().forEach(desc -> repos.add(new RepoKeyType(desc.getType(), desc.getKey())));
        return repos;
    }
}
