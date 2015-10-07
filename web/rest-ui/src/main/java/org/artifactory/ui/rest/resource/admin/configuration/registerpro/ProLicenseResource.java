package org.artifactory.ui.rest.resource.admin.configuration.registerpro;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.configuration.registerpro.ProLicense;
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
@Path("registerlicense")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProLicenseResource extends BaseResource {
    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateLicense(ProLicense proLicense)
            throws Exception {
        return runService(configServiceFactory.updateLicenseKeyService(), proLicense);
    }

    @GET
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLicense()
            throws Exception {
        return runService(configServiceFactory.getLicenseKeyService());
    }

}
