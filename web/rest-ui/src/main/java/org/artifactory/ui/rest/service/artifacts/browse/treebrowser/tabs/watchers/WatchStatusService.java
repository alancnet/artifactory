package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.watchers;

import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.watch.ArtifactWatchAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.fs.WatchersInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.BaseArtifact;
import org.artifactory.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WatchStatusService implements RestService {

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ArtifactWatchAddon watchAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                ArtifactWatchAddon.class);
        String path = request.getQueryParamByKey("path");
        String repoKey = request.getQueryParamByKey("repoKey");
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        LocalRepoDescriptor localRepoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
        if (localRepoDescriptor != null && addonsManager.isAddonSupported(AddonType.WATCH)) {
            // get watch status
            BaseArtifact watchStatus = getWatchStatus(watchAddon, repoPath);
            // update status
            response.iModel(watchStatus);
        }
    }

    /**
     * return watch status
     *
     * @param watchAddon - watch add onn
     * @param repoPath   - repo path
     * @return watch status
     */
    private BaseArtifact getWatchStatus(ArtifactWatchAddon watchAddon, RepoPath repoPath) {
        if (authorizationService.canRead(repoPath) && !authorizationService.isAnonymous() &&
                !isThisBranchHasWatchAlready(authorizationService, watchAddon, repoPath)) {
            if (isUserWatchingRepoPath(authorizationService, repoPath, watchAddon)) {
                return new BaseArtifact("Unwatch");
            } else {
                return new BaseArtifact("Watch");
            }
        }
        return null;
    }

    /**
     * check if anyone is watching this path branch already
     *
     * @param authService - authorization service
     * @param watchAddon  - watch addon
     * @return if true - this path branch has watch already
     */
    private boolean isThisBranchHasWatchAlready(AuthorizationService authService,
            ArtifactWatchAddon watchAddon, RepoPath repoPath) {
        Pair<RepoPath, WatchersInfo> nearestWatch = watchAddon.getNearestWatchDefinition(
                repoPath, authService.currentUsername());
        return nearestWatch != null && !(nearestWatch.getFirst().getPath().equals(repoPath.getPath()));
    }

    /**
     * check if user watching repo path
     *
     * @param authService        - authorization service
     * @param repoPath           - repo path
     * @param artifactWatchAddon watch addon
     * @return if true - user is watching repo service
     */
    protected boolean isUserWatchingRepoPath(AuthorizationService authService, RepoPath repoPath,
            ArtifactWatchAddon artifactWatchAddon) {
        return artifactWatchAddon.isUserWatchingRepo(repoPath, authService.currentUsername());
    }
}
