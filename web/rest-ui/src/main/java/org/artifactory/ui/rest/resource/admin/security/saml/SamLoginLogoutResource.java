package org.artifactory.ui.rest.resource.admin.security.saml;

import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Gidi Shabat
 */
@Component
@Path("saml")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SamLoginLogoutResource extends BaseResource {
    private static final Logger log = LoggerFactory.getLogger(SamlResource.class);

    @Autowired
    private SecurityServiceFactory securityFactory;

    @Path("loginRequest")
    @GET
    public Response loginRequest() {
        return runService(securityFactory.handleLoginRequest());
    }


    @Path("loginResponse")
    @POST
    public Response loginResponse() {
        return runService(securityFactory.handleLoginResponse());
    }

    @Path("logoutRequest")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response logoutRequest() {
        return runService(securityFactory.handleLogoutRequest());
    }
}
