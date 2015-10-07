package org.artifactory.ui.rest.service.setmeup;

import com.google.common.collect.Sets;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.setmeup.SetMeUpModel;
import org.artifactory.ui.rest.model.utils.repositories.RepoKeyType;
import org.artifactory.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author chen Keinan
 * @author Lior Hasson
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSetMeUpService implements RestService {

    @Autowired
    AclService aclService;

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        /// get list of repositories descriptor by user permissions
        List<RepoKeyType> repoKeyTypes = getRepoKeyTypes();
        // update set me up model
        SetMeUpModel setMeUpModel = getSetMeUpModel(request, repoKeyTypes);
        response.iModel(setMeUpModel);
    }

    private SetMeUpModel getSetMeUpModel(ArtifactoryRestRequest artifactoryRequest, List<RepoKeyType> repoKeyTypes) {
        SetMeUpModel setMeUpModel = new SetMeUpModel();
        setMeUpModel.setRepoKeyTypes(repoKeyTypes);
        String servletContextUrl = HttpUtils.getServletContextUrl(artifactoryRequest.getServletRequest());
        setMeUpModel.setBaseUrl(servletContextUrl);
        setMeUpModel.setServerId(centralConfigService.getServerName());
        return setMeUpModel;
    }

    /**
     * get user keys type list
     * @return - list of user keys types
     */
    private List<RepoKeyType> getRepoKeyTypes() {
        Set<RepoBaseDescriptor> userRepos = getUserRepos();
        List<RepoKeyType> repoKeyTypes = new ArrayList<>();

        userRepos.forEach(userRepo -> {
            if (!userRepo.getKey().endsWith("-cache")) {
                RepoKeyType repoKeyType = new RepoKeyType(userRepo.getType(), userRepo.getKey());
                // update can read or deploy
                updateCanReadOrDeploy(userRepo, repoKeyType);

                if(userRepo instanceof LocalRepoDescriptor){
                    repoKeyType.setIsLocal(true);
                }

                repoKeyTypes.add(repoKeyType);
            }
        });

        return repoKeyTypes;
    }

    private void updateCanReadOrDeploy(RepoBaseDescriptor userRepo, RepoKeyType repoKeyType) {
        RepoPath repoPath;
        repoKeyType.setCanRead(false);
        repoKeyType.setCanDeploy(false);

        if(userRepo instanceof HttpRepoDescriptor){
            repoPath = InternalRepoPathFactory.repoRootPath(userRepo.getKey() + "-cache");
        }
        else {
            repoPath = InternalRepoPathFactory.repoRootPath(userRepo.getKey());
        }

        if(authorizationService.canRead(repoPath)) {
            repoKeyType.setCanRead(true);
        }
        if(authorizationService.canDeploy(repoPath)) {
            repoKeyType.setCanDeploy(true);
        }
    }

    /**
     * get list of repositories allowed for this user to deploy
     *
     * @return - list of repositories
     */
    private Set<RepoBaseDescriptor> getUserRepos() {
        Set<RepoBaseDescriptor> baseDescriptors = Sets.newTreeSet(new RepoComparator());
        List<LocalRepoDescriptor> localDescriptors = repositoryService.getLocalAndCachedRepoDescriptors();
        removeNonPermissionRepositories(localDescriptors);
        baseDescriptors.addAll(localDescriptors);
        // add remote repo
        List<RemoteRepoDescriptor> remoteDescriptors = repositoryService.getRemoteRepoDescriptors();
        removeNonPermissionRepositories(remoteDescriptors);
        baseDescriptors.addAll(remoteDescriptors);
        // add virtual repo
        List<VirtualRepoDescriptor> virtualDescriptors = repositoryService.getVirtualRepoDescriptors();
        removeNonPermissionRepositories(virtualDescriptors);
        baseDescriptors.addAll(virtualDescriptors);

        return baseDescriptors;
    }

    /**
     *
     * filter non permitted repositories
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
