package org.artifactory.ui.rest.resource.admin.advanced.securitydescriptor;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.advanced.securitydescriptor.SecurityDescriptorModel;
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
@Path("securitydescriptor")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SecurityDescriptorResource extends BaseResource {

    @Autowired
    protected AdvancedServiceFactory advanceFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSecurityDescriptor()
            throws Exception {
        return runService(advanceFactory.getSecurityDescriptorService());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSecurityDescriptor(SecurityDescriptorModel securityXml)
            throws Exception {
        return runService(advanceFactory.updateSecurityConfigService(), securityXml);
    }
}
