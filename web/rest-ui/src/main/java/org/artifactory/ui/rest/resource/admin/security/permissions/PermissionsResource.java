package org.artifactory.ui.rest.resource.admin.security.permissions;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.security.permissions.DeletePermissionTargetModel;
import org.artifactory.ui.rest.model.admin.security.permissions.PermissionTargetModel;
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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Path("targetPermissions")
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PermissionsResource extends BaseResource {

    @Autowired
    private SecurityServiceFactory securityFactory;

    @GET
    @Path("crud{name:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPermissionTarget()
            throws Exception {
        return runService(securityFactory.getPermissionsTarget());
    }

    @GET
    @Path("allUsersGroups")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsersGroups() throws Exception {
        return runService(securityFactory.getAllUsersAndGroups());
    }

    @PUT
    @Path("{name : [^/]+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePermissionTarget(PermissionTargetModel permissionTargetModel)
            throws Exception {
        return runService(securityFactory.updatePermissionsTarget(), permissionTargetModel);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPermissionTarget(PermissionTargetModel permissionTargetModel)
            throws Exception {
        return runService(securityFactory.createPermissionsTarget(), permissionTargetModel);
    }

    @POST
    @Path("deleteTargetPermissions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePermissionTarget(DeletePermissionTargetModel deletePermissionTargetModel) throws Exception {
        return runService(securityFactory.deletePermissionsTarget(), deletePermissionTargetModel);
    }
}
