package org.artifactory.ui.rest.service.setmeup;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.distributionmngt.DistributionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetMavenDistributionMgntService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    RepositoryService repositoryService;


    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoKey = request.getQueryParamByKey("repoKey");
        DistributionManagement distributionManagement = new DistributionManagement();
        LocalRepoDescriptor localRepoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
        // populate distribution management
        if (localRepoDescriptor != null) {
            StringBuilder dm = distributionManagement.populateDistributionManagement(localRepoDescriptor,
                    centralConfigService, request.getServletRequest());
            distributionManagement.setDistributedManagement(dm.toString());
            response.iModel(distributionManagement);
        }
    }
}
