package org.artifactory.ui.rest.resource.utils.group;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.utils.UtilsServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Path("groupPermission")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GroupPermissionResource extends BaseResource {

    @Autowired
    protected UtilsServiceFactory utilsFactory;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroup(Group group)
            throws Exception {
        return runService(utilsFactory.getGroupPermissions(), group);
    }
}
