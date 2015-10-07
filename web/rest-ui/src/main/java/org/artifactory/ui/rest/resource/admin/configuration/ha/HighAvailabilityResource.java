package org.artifactory.ui.rest.resource.admin.configuration.ha;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.configuration.ConfigServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@Path("highAvailability")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HighAvailabilityResource extends BaseResource {

    @Autowired
    protected ConfigServiceFactory configServiceFactory;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHighAvailabilityData()
            throws Exception {
        return runService(configServiceFactory.getHighAvailabilityMembers());
    }

    @DELETE
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeServer() throws Exception {
        return runService(configServiceFactory.removeServer());
    }
}
