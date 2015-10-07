package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general;

import com.google.common.collect.ImmutableMap;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.BaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Aviad Shikloshi
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetArtifactsCount implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetArtifactsCount.class);

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BaseInfo baseInfo = (BaseInfo) request.getImodel();
        try {
            RepoPath repoPath = RepoPathFactory.create(baseInfo.getRepositoryPath());
            long artifactCount = repositoryService.getArtifactCount(repoPath);
            response.iModel(ImmutableMap.of("artifactsCount", artifactCount));
        } catch (Exception e) {
            log.error("Error while counting artifacts.", e);
            response.error("Unable to count artifacts.");
        }
    }
}
