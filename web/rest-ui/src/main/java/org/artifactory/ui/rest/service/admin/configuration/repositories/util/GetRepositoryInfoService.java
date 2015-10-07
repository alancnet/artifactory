package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.info.RepositoryInfo;
import org.artifactory.ui.rest.model.admin.configuration.repository.info.RepositoryInfoListFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Aviad Shikloshi
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetRepositoryInfoService implements RestService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private CentralConfigService configService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoType = request.getPathParamByKey("repoType");
        List<RepositoryInfo> repoInfo = RepositoryInfoListFactory.createRepositoryInfo(repoType, configService, repositoryService);
        response.iModelList(repoInfo);
    }
}
