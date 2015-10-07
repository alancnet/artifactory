package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.pomview;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions.ViewArtifactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PomViewService implements RestService {

    @Autowired
    ViewArtifactService viewArtifactService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        // currently view artifact and view pom are the same
        viewArtifactService.execute(request, response);
    }
}
