package org.artifactory.ui.rest.resource.artifacts.browse.treebrowser.tabs.generalinfo;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.GeneralArtifactInfo;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.browse.BrowseServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Dan Feldman
 */
@Path("filteredResource")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FilteredResourceResource extends BaseResource {

    public static final String SET_FILTERED_QUERY_PARAM = "setFiltered";

    @Autowired
    BrowseServiceFactory browseFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setFilteredResource(GeneralArtifactInfo artifact) throws Exception {
        return runService(browseFactory.setFilteredResource(), artifact);
    }
}
