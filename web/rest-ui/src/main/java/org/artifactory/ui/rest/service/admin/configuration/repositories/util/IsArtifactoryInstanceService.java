package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.rest.common.service.ResearchService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteNetworkRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Checks whether remoteRepo is an Artifactory instance
 *
 * @author Michael Pasternak
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class IsArtifactoryInstanceService<T extends RemoteRepositoryConfigModel> extends RemoteRepositoryProvider implements RestService<T> {
    protected static final Logger log = LoggerFactory.getLogger(IsArtifactoryInstanceService.class);

    @Autowired
    ResearchService researchService;

    @Override
    public void execute(ArtifactoryRestRequest<T> artifactoryRequest, RestResponse artifactoryResponse) {

        RemoteRepositoryConfigModel repositoryModel = artifactoryRequest.getImodel();
        RemoteAdvancedRepositoryConfigModel remoteRepoAdvancedModel = repositoryModel.getAdvanced();
        if (remoteRepoAdvancedModel == null) {
            artifactoryResponse.error("Network details was not sent.")
                    .responseCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        RemoteBasicRepositoryConfigModel basicModel = repositoryModel.getBasic();
        if (basicModel == null || StringUtils.isEmpty(basicModel.getUrl())) {
            artifactoryResponse.error("Remote Url was not sent.")
                    .responseCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        String remoteRepoUrl = PathUtils.addTrailingSlash(basicModel.getUrl());
        if (remoteRepoUrl == null) {
            artifactoryResponse.error("Remote repo url was not sent.")
                    .responseCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        RemoteNetworkRepositoryConfigModel networkModel = remoteRepoAdvancedModel.getNetwork();
        CloseableHttpClient client = getRemoteRepoTestHttpClient(remoteRepoUrl, networkModel);

        artifactoryResponse
                .info(Boolean.toString(
                        researchService.isArtifactory(
                                remoteRepoUrl,
                                client
                        )
                ))
                .responseCode(HttpStatus.SC_OK);
        return;
    }
}
