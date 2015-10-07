package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general;

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.filteredresources.FilteredResourcesAddon;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.GeneralArtifactInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.artifactory.ui.rest.resource.artifacts.browse.treebrowser.tabs.generalinfo.FilteredResourceResource.SET_FILTERED_QUERY_PARAM;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SetFilteredResourceService implements RestService {

    @Autowired
    AuthorizationService authService;

    @Autowired
    AddonsManager addonsManager;

    @Autowired
    RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        GeneralArtifactInfo artifact = (GeneralArtifactInfo) request.getImodel();
        RepoPath path = RepoPathFactory.create(artifact.getRepoKey(), artifact.getPath());
        if (!addonsManager.isAddonSupported(AddonType.FILTERED_RESOURCES)) {
            response.error("The Filtered Resource addon is not enabled").responseCode(HttpStatus.SC_FORBIDDEN);
        } else if (!authService.canAnnotate(path)) {
            response.error("You do not have annotate permissions on this path").responseCode(HttpStatus.SC_UNAUTHORIZED);
        } else if (repoService.getItemInfo(path).isFolder()) {
            response.error(path.getPath() + " is a folder").responseCode(HttpStatus.SC_BAD_REQUEST);
        } else {
            addonsManager.addonByType(FilteredResourcesAddon.class).toggleResourceFilterState(path,
                    Boolean.valueOf(request.getQueryParamByKey(SET_FILTERED_QUERY_PARAM)));
        }
    }
}
