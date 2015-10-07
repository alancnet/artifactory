package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.RestGeneralTab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetGeneralArtifactsService implements RestService {

    @Autowired
    AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RestGeneralTab generalTab = (RestGeneralTab) request.getImodel();
        // populate  general tab data
        generalTab.populateGeneralData(request, authorizationService);
        // update response data
        response.iModel(generalTab);
    }
}
