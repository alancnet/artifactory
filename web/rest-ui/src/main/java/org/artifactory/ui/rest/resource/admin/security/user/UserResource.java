package org.artifactory.ui.rest.resource.admin.security.user;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.security.user.DeleteUsersModel;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.artifactory.ui.rest.model.builds.DeleteBuildsModel;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@Path("users")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UserResource extends BaseResource {

    @Autowired
    protected SecurityServiceFactory securityFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(User userModel)
            throws Exception {
        return runService(securityFactory.createUser(), userModel);
    }


    @PUT
    @Path("{id : [^/]+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(User userModel)
            throws Exception {
        return runService(securityFactory.updateUser(), userModel);
    }

    @POST
    @Path("userDelete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUsers(DeleteUsersModel deleteUsersModel) throws Exception {
        return runService(securityFactory.deleteUser(), deleteUsersModel);
    }

    @GET
    @Path("crud{id : (/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser()
            throws Exception {
        return runService(securityFactory.getUsers());
    }

    @GET
    @Path("permissions{id : /[^/]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserPermissions()
            throws Exception {
        return runService(securityFactory.getUserPermissions());
    }

    @POST
    @Path("externalStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkExternalStatus(User user)
            throws Exception {
        return runService(securityFactory.checkExternalStatus(), user);
    }
}
