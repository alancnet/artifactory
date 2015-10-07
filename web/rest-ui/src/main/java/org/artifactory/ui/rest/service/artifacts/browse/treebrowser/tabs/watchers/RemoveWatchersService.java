package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.watchers;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.watch.ArtifactWatchAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.watchers.DeleteWatcher;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.watchers.DeleteWatchersModel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RemoveWatchersService<T extends DeleteWatchersModel> implements RestService<T> {
    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        T imodel = request.getImodel();
        for (DeleteWatcher watcher : imodel.getWatches()) {
            RepoPath repoPath = InternalRepoPathFactory.create(watcher.getRepoKey(), watcher.getPath());
            // remove watcher
            removeWatcherAndUpdateResponse(response, watcher.getName(), repoPath);
        }
        if(imodel.getWatches().size()>1){
            response.info("Successfully removed " + imodel.getWatches().size() + " watchers");
        }else if(imodel.getWatches().size()==1){
            response.info("Successfully removed watcher '" + imodel.getWatches().get(0).getName() + "'");
        }
    }

    /**
     * remove watcher and update response
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param watchName           - watcher name
     * @param repoPath            - repo path
     * @param repoPath            - repo path
     */
    private void removeWatcherAndUpdateResponse(RestResponse artifactoryResponse, String watchName, RepoPath repoPath) {
        ArtifactWatchAddon artifactWatchAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                ArtifactWatchAddon.class);
        artifactWatchAddon.removeWatcher(repoPath, watchName);
    }
}