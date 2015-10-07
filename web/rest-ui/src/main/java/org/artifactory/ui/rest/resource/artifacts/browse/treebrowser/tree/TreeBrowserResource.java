package org.artifactory.ui.rest.resource.artifacts.browse.treebrowser.tree;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.RestTreeNode;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.browse.BrowseServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Path("treebrowser")
@RolesAllowed({AuthorizationService.ROLE_ADMIN,AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TreeBrowserResource extends BaseResource {

    @Autowired
    BrowseServiceFactory browseFactory;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response browseTreeNodes(RestTreeNode node)
            throws Exception {
        return runService(browseFactory.getBrowseTreeNodesService(), node);
    }
}
