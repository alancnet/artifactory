package org.artifactory.ui.rest.resource.admin.security.general;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.security.general.SecurityConfig;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("securityconfig")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GeneralSecurityConfigResource extends BaseResource {

    @Autowired
    private SecurityServiceFactory securityFactory;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateConfig(SecurityConfig securityConfig)
            throws Exception {
        return runService(securityFactory.updateSecurityConfig(),securityConfig);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfig()
            throws Exception {
        return runService(securityFactory.getSecurityConfig());
    }
}
