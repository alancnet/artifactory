package org.artifactory.ui.rest.resource.artifacts.search;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.artifacts.search.propertysearch.PropertySearch;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.search.SearchServiceFactory;
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
@Path("artifactsearch/property")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PropertySearchResource extends BaseResource {

    @Autowired
    SearchServiceFactory searchFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response propertySearch(PropertySearch propertySearch)
            throws Exception {
        return runService(searchFactory.propertySearchService(), propertySearch);
    }


    @GET
    @Path("keyvalue")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPropertySets()
            throws Exception {
        return runService(searchFactory.getPropertySetsService());
    }
}
