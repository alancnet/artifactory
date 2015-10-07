package org.artifactory.ui.rest.resource.artifacts.browse.treebrowser.actions.deleteversions;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.DeleteArtifactVersion;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.browse.BrowseServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Path("artifactactions/deleteversions")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteVersionsResource extends BaseResource {

    @Autowired
    BrowseServiceFactory browseFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeleteVersionList()
            throws Exception {
        return runService(browseFactory.getDeleteVersionsService());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteVersionList(List<DeleteArtifactVersion> deleteArtifactVersions)
            throws Exception {
        return runService(browseFactory.deleteVersionService(), deleteArtifactVersions);
    }
}
