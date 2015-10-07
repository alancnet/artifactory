package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.blackduck;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.blackduck.BlackDuckAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.blackduck.BlackDuckArtifactInfo;
import org.artifactory.ui.rest.service.admin.configuration.blackduck.GetBlackDuckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.codehaus.plexus.util.StringUtils.isBlank;
import static org.codehaus.plexus.util.StringUtils.isNotBlank;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateBlackDuckComponentIdService implements RestService {

    @Autowired
    GetBlackDuckService getBlackDuckService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BlackDuckArtifactInfo duckArtifactInfo = (BlackDuckArtifactInfo) request.getImodel();
        // update black duck component id
        updateBlackDuckComponentId(response, duckArtifactInfo);
    }

    /**
     * update Black Suck componentId
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param duckArtifactInfo    - black duck artifact info model
     */
    private void updateBlackDuckComponentId(RestResponse artifactoryResponse, BlackDuckArtifactInfo duckArtifactInfo) {
        String path = duckArtifactInfo.getPath();
        String repoKey = duckArtifactInfo.getRepoKey();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        BlackDuckAddon blackDuckAddon = addonsManager.addonByType(BlackDuckAddon.class);
        String editedId = duckArtifactInfo.getComponentId();
        String idFromInfo = duckArtifactInfo.getOrigComponentId();
        // update component id
        if (isNotBlank(editedId) && (isBlank(idFromInfo) || !editedId.trim().equals(idFromInfo))) {
            blackDuckAddon.setComponentExternalIdProperty(repoPath, editedId.trim());
            artifactoryResponse.info("External Component ID changed successfully to " + editedId.trim());
        } else if (editedId != null && isNotBlank(idFromInfo)) {
            blackDuckAddon.clearComponentIdProperty(repoPath);
            artifactoryResponse.info("External Component ID was successfully cleared");
        }
    }
}
