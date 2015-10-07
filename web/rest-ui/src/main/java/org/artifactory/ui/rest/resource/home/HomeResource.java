package org.artifactory.ui.rest.resource.home;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.general.GeneralServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen keinan
 */

@Path("home")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HomeResource extends BaseResource {


    @Autowired
    GeneralServiceFactory generalFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHomeData()
            throws Exception {
        return runService(generalFactory.getHomePage());
    }
}
