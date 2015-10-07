package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.DownloadArtifact;
import org.artifactory.util.HttpUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DownloadArtifactService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        DownloadArtifact downloadArtifact = (DownloadArtifact) request.getImodel();
        String repoKey = downloadArtifact.getRepoKey();
        String path = downloadArtifact.getPath();
        RestModel artifactDownloadModel = getArtifactDownloadModel(request, repoKey, path);
        response.iModel(artifactDownloadModel);
    }

    /**
     * build download url and update download model
     * @param artifactoryRequest - encapsulate data related to request
      * @param repoKey - repo key
     * @param path - path
     */
    private RestModel getArtifactDownloadModel(ArtifactoryRestRequest artifactoryRequest,
            String repoKey, String path) {
        HttpServletRequest httpRequest =artifactoryRequest.getServletRequest();
        String servletContextUrl = HttpUtils.getServletContextUrl(httpRequest);
        DownloadArtifact downloadArtifact = new DownloadArtifact();
        String downloadPath = servletContextUrl + "/" + repoKey + "/" + path;
        downloadArtifact.setPath(downloadPath);
        return downloadArtifact;
    }
}
