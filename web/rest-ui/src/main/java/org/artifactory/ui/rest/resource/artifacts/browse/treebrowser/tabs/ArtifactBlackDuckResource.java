package org.artifactory.ui.rest.resource.artifacts.browse.treebrowser.tabs;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.blackduck.BlackDuckArtifactInfo;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.browse.BrowseServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@Path("artifactblackduck")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArtifactBlackDuckResource extends BaseResource {

    @Autowired
    BrowseServiceFactory browseFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchBlackDuck(BlackDuckArtifactInfo blackDuckArtifactInfo)
            throws Exception {
        return runService(browseFactory.getBlackDuckArtifact(), blackDuckArtifactInfo);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBlackDuckComponentId(BlackDuckArtifactInfo blackDuckArtifactInfo)
            throws Exception {
        return runService(browseFactory.updateBlackDuckComponentId(), blackDuckArtifactInfo);
    }
}
