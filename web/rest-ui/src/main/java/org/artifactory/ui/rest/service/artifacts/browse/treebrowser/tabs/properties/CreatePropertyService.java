package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.properties;

import org.artifactory.api.properties.PropertiesService;
import org.artifactory.exception.CancelException;
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

import javax.servlet.http.HttpServletResponse;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreatePropertyService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(DeletePropertyService.class);

    @Autowired
    private PropertiesService propsService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        PropertiesArtifactInfo propertiesTab = (PropertiesArtifactInfo) request.getImodel();
        RepoPath repoPath = RequestUtils.getPathFromRequest(request);
        boolean recursive = Boolean.valueOf(request.getQueryParamByKey("recursive"));
        // save property to db
        if (propertiesTab.getProperty() == null) {
            response.error("Cannot create an empty property");
            return;
        }
        savePropertyAndUpdateResponse(response, propertiesTab, repoPath, recursive);
    }

    /**
     * save property and update response
     *
     * @param artifactoryResponse - encapsulate artifactory response data
     * @param propertiesTab       - properties tab data
     * @param repoPath            - repo path
     */
    private void savePropertyAndUpdateResponse(RestResponse artifactoryResponse, PropertiesArtifactInfo propertiesTab,
            RepoPath repoPath, boolean recursive) {
        try {
            if (recursive) {
                propsService.addPropertyRecursively(repoPath, propertiesTab.getParent(), propertiesTab.getProperty(),
                        propertiesTab.getSelectedValues());
            } else {
                propsService.addProperty(repoPath, propertiesTab.getParent(), propertiesTab.getProperty(),
                        propertiesTab.getSelectedValues());
            }
            artifactoryResponse.info("Successfully created property '" + propertiesTab.getProperty().getName() + "'");
            artifactoryResponse.responseCode(HttpServletResponse.SC_CREATED);
        } catch (CancelException e) {
            log.error("Failed to create property:" + propertiesTab.getProperty().getName());
            artifactoryResponse.error("Failed to created property '" + propertiesTab.getProperty().getName() + "'");
        }
    }
}
