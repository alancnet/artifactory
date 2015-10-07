package org.artifactory.ui.rest.resource.admin.security.group;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.security.group.DeleteGroupsModel;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
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
@Path("groups")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GroupResource extends BaseResource {

    @Autowired
    protected SecurityServiceFactory securityFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGroup(Group groupModel)
            throws Exception {
        return runService(securityFactory.createGroup(), groupModel);
    }

    @PUT
    @Path("{id : [^/]+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateGroup(Group groupModel)
            throws Exception {
        return runService(securityFactory.updateGroup(), groupModel);
    }

    @POST
    @Path("groupsDelete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteGroups(DeleteGroupsModel deleteGroupsModel) throws Exception {
        return runService(securityFactory.deleteGroup(), deleteGroupsModel);
    }

    @GET
    @Path("crud{id:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroup()
            throws Exception {
        return runService(securityFactory.getGroup());
    }
}
