package org.artifactory.ui.rest.resource.admin.configuration.blackduck;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.configuration.blackduck.BlackDuck;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.configuration.ConfigServiceFactory;
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
@Path("blackduck")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlackDuckResource extends BaseResource {

    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBlackDuck(BlackDuck blackDuck)
            throws Exception {
        return runService(configServiceFactory.updateBlackDuckService(), blackDuck);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlackDuck()
            throws Exception {
        return runService(configServiceFactory.getBlackDuckService());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response testBlackDuck(BlackDuck blackDuck)
            throws Exception {
        return runService(configServiceFactory.testBlackDuckService(), blackDuck);
    }
}
