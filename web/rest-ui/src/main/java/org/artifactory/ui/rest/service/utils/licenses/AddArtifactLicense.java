package org.artifactory.ui.rest.service.utils.licenses;

import org.artifactory.api.properties.PropertiesService;
import org.artifactory.md.Properties;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * @author Chen Keinan
 */
public class AddArtifactLicense implements RestService {

    @Autowired
    private PropertiesService propsService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoKey = request.getQueryParamByKey("repoKey");
        String path = request.getQueryParamByKey("path");
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);

        Properties properties = propsService.getProperties(repoPath);
        Set<String> licenseNames = properties.get("artifactory.licenses");

    }
}
