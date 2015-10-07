package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tree;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.md.Properties;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.RestTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author Chen Keinan
 */
@Component()
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BrowseTreeNodesService implements RestService {

    @Autowired
    private AuthorizationService authService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean isCompact = Boolean.valueOf(request.getQueryParamByKey("compacted"));
        Properties props = (Properties) request.getServletRequest().getAttribute("artifactory.request_properties");
        // get branch model
        RestTreeNode itemNode = (RestTreeNode) request.getImodel();
        // populate branch items
        Collection<? extends RestModel> items = populateBranchItems(itemNode, isCompact, props, request);
        // update response data
        updateResponseData(response, items);
    }

    /**
     * update response data
     *
     * @param artifactoryResponse - encapsulate artifactory response data
     * @param items               - items
     */
    private void updateResponseData(RestResponse artifactoryResponse, Collection<? extends RestModel> items) {
        // update response
        artifactoryResponse.iModelList(items);
    }

    /**
     * populate branch childes
     *
     * @param itemNode - current branch
     * @param request
     */
    private Collection<? extends RestModel> populateBranchItems(RestTreeNode itemNode, boolean isCompact,
            Properties props, ArtifactoryRestRequest request) {
        return itemNode.fetchItemTypeData(authService, isCompact, props, request);
    }
}
