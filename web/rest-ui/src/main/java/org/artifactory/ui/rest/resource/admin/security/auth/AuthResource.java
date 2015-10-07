package org.artifactory.ui.rest.resource.admin.security.auth;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * @author chen keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Path("auth")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AuthResource extends BaseResource {

    @Autowired
    protected SecurityServiceFactory securityFactory;

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UserLogin userLoginModel) throws Exception {
        return runService(securityFactory.loginService(), userLoginModel);
    }

    @POST
    @Path("logout")
    public Response logout()
            throws Exception {
        return runService(securityFactory.logoutService());
    }

    @GET
    @Path("issaml")
    public Response isSamlAuthentication() throws Exception {
        return runService(securityFactory.isSamlAuthentication());
    }


    @POST
    @Path("forgotpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forgotPassword(UserLogin userLoginModel)
            throws Exception {
        return runService(securityFactory.forgotPassword(),userLoginModel);
    }

    @POST
    @Path("validatetoken")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateToken()
            throws Exception {
        return runService(securityFactory.validateToken());
    }

    @POST
    @Path("resetpassword")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(UserLogin userLoginModel)
            throws Exception {
        return runService(securityFactory.resetPassword(), userLoginModel);
    }

    @POST
    @Path("loginRelatedData")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchLoginRelatedData()
            throws Exception {
        return runService(securityFactory.loginRelatedData());
    }

    @GET
    @Path("current")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentUser()
            throws Exception {
        return runService(securityFactory.getCurrentUser());
    }

    @GET
    @Path("canAnnotate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response canAnnotate() throws Exception {
        return runService(securityFactory.getCanAnnotateService());
    }
}

