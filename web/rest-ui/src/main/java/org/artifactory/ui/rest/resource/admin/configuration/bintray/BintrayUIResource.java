package org.artifactory.ui.rest.resource.admin.configuration.bintray;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.configuration.bintray.BintrayUIModel;
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
@Path("bintraysetting")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BintrayUIResource extends BaseResource {

    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBintray(BintrayUIModel bintray)
            throws Exception {
        return runService(configServiceFactory.updateBintrayService(), bintray);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBintray()
            throws Exception {
        return runService(configServiceFactory.getBintrayService());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
    public Response testBintray(BintrayUIModel bintray)
            throws Exception {
        return runService(configServiceFactory.testBintrayService(), bintray);
    }
}
