package org.artifactory.ui.rest.resource.artifacts.search;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.artifactory.ui.rest.model.artifacts.search.propertysearch.PropertySearch;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.search.SearchServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@Path("searchResults")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SearchResultsResource extends BaseResource {

    @Autowired
    SearchServiceFactory searchFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveSearchResults(BaseSearchResult baseSearchResult)
            throws Exception {
        return runService(searchFactory.propertySearchService(), baseSearchResult);
    }
}
