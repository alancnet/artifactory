package org.artifactory.ui.rest.resource.artifacts.browse.treebrowser.tabs;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.PropertiesArtifactInfo;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.browse.BrowseServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@Path("artifactproperties{name:(/[^/]+?)?}")
@RolesAllowed({AuthorizationService.ROLE_ADMIN,AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArtifactsPropertiesResource extends BaseResource {
    @Autowired
    BrowseServiceFactory browseFactory;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProperty(PropertiesArtifactInfo propertiesTab)
            throws Exception {
        return runService(browseFactory.getCreatePropertyService(),propertiesTab);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
     public Response getProperty()
            throws Exception {
        return runService(browseFactory.getGetPropertyService());
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProperty(PropertiesArtifactInfo propertiesTab)
            throws Exception {
        return runService(browseFactory.getUpdatePropertyService(),propertiesTab);
    }
}
