package org.artifactory.ui.rest.resource.admin.services.indexer;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.services.indexer.Indexer;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.services.ServicesServiceFactory;
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

@Path("indexer")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MavenIndexerResource extends BaseResource {

    @Autowired
    protected ServicesServiceFactory serviceFactory;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateIndexer(Indexer indexer)
            throws Exception {
        return runService(serviceFactory.updateIndexerService(), indexer);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIndexer()
            throws Exception {
        return runService(serviceFactory.getIndexerService());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response runIndexNow()
            throws Exception {
        return runService(serviceFactory.runIndexNowService());
    }
}
