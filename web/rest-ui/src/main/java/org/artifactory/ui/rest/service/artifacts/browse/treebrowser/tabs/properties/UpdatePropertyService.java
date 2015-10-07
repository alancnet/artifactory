package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.properties;

import org.artifactory.api.properties.PropertiesService;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.PropertiesArtifactInfo;
import org.artifactory.ui.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdatePropertyService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(UpdatePropertyService.class);

    @Autowired
    private PropertiesService propsService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        PropertiesArtifactInfo propertiesTab = (PropertiesArtifactInfo) request.getImodel();
        try {
            RepoPath repoPath = RequestUtils.getPathFromRequest(request);
            propsService.editProperty(repoPath, propertiesTab.getParent(), propertiesTab.getProperty(),
                    propertiesTab.getSelectedValues());
            response.info("Successfully updated property '" + propertiesTab.getProperty().getName() + "'");
        }catch (Exception e){
            log.error("Failed to create property '" + propertiesTab.getProperty().getName() +  "'");
            response.info("Failed to update property '" + propertiesTab.getProperty().getName() +  "'");
        }
    }
}
