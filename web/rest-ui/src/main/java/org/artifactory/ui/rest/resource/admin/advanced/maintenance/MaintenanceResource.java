package org.artifactory.ui.rest.resource.admin.advanced.maintenance;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.advanced.maintenance.Maintenance;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.advanced.AdvancedServiceFactory;
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
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("maintenance")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MaintenanceResource extends BaseResource {

    @Autowired
    protected AdvancedServiceFactory advanceFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMaintenance()
            throws Exception {
        return runService(advanceFactory.getMaintenance());
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveMaintenance(Maintenance maintenance)
            throws Exception {
        return runService(advanceFactory.saveMaintenance(), maintenance);
    }

    @POST
    @Path("garbageCollection")
    @Produces(MediaType.APPLICATION_JSON)
    public Response garbageCollection()
            throws Exception {
        return runService(advanceFactory.garbageCollection());
    }

    @POST
    @Path("cleanUnusedCache")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cleanUnusedCache()
            throws Exception {
        return runService(advanceFactory.cleanUnusedCached());
    }

    @POST
    @Path("cleanVirtualRepo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cleanVirtualRepo()
            throws Exception {
        return runService(advanceFactory.cleanupVirtualRepo());
    }

    @POST
    @Path("prune")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pruneUnreferencedData()
            throws Exception {
        return runService(advanceFactory.pruneUnReferenceData());
    }

    @POST
    @Path("compress")
    @Produces(MediaType.APPLICATION_JSON)
    public Response compressInternalData()
            throws Exception {
        return runService(advanceFactory.compressInternalData());
    }
}
