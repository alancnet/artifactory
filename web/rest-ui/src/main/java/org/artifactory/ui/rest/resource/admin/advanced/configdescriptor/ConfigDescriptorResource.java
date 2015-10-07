package org.artifactory.ui.rest.resource.admin.advanced.configdescriptor;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.advanced.configdescriptor.ConfigDescriptorModel;
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
 * @author Chen keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("configdescriptor")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ConfigDescriptorResource extends BaseResource {

    @Autowired
    protected AdvancedServiceFactory advanceFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfigDescriptor()
            throws Exception {
        return runService(advanceFactory.getConfigDescriptorService());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateConfigDescriptor(ConfigDescriptorModel configXml)
            throws Exception {
        return runService(advanceFactory.updateConfigDescriptorService(), configXml);
    }
}