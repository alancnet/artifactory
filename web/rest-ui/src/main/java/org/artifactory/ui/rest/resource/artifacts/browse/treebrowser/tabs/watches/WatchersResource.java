package org.artifactory.ui.rest.resource.artifacts.browse.treebrowser.tabs.watches;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.watchers.DeleteWatchersModel;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.browse.BrowseServiceFactory;
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
@Path("artifactwatches")
@RolesAllowed({AuthorizationService.ROLE_ADMIN,AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WatchersResource extends BaseResource {

    @Autowired
    BrowseServiceFactory browseFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWatchers()
            throws Exception {
        return runService(browseFactory.getWatchersService());
    }

    @POST
    @Path("remove")
    public Response removeWatchers(DeleteWatchersModel model)
            throws Exception {
        return runService(browseFactory.getRemoveWatchersService(),model);
    }

    @GET
    @Path("status")
    public Response watchersStatus()
            throws Exception {
        return runService(browseFactory.watchStatusService());
    }
}
