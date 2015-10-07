package org.artifactory.ui.rest.resource.admin.security.general;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("crypto{action:(/[^/]+?)?}")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CryptoSecurityDescriptorResource extends BaseResource{

    @Autowired
    private SecurityServiceFactory securityFactory;

    @POST
    public Response encryptDecrypt()
            throws Exception {
        return runService(securityFactory.encryptPassword());
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response hasMasterKey(){
        return runService(securityFactory.getMasterKey());
    }
}


