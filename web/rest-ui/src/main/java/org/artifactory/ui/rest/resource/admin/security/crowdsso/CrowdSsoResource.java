package org.artifactory.ui.rest.resource.admin.security.crowdsso;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.security.crowdsso.CrowdGroupModel;
import org.artifactory.ui.rest.model.admin.security.crowdsso.CrowdIntegration;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Chen Keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("crowd")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CrowdSsoResource extends BaseResource {

    @Autowired
    private SecurityServiceFactory securityFactory;

    @PUT
    public Response updateCrowdIntegration(CrowdIntegration crowdIntegration)
            throws Exception {
        return runService(securityFactory.updateCrowdIntegration(), crowdIntegration);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCrowdIntegration() {
        return runService(securityFactory.getCrowdIntegration());
    }

    @POST
    @Path("refresh{name:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshCrowdGroups(CrowdIntegration crowdIntegration) {
        return runService(securityFactory.refreshCrowdGroups(), crowdIntegration);
    }

    @POST
    @Path("import")
    @Produces(MediaType.APPLICATION_JSON)
    public Response importCrowdGroups(List<CrowdGroupModel> crowdGroupsModelList) {
        return runService(securityFactory.importCrowdGroups(), crowdGroupsModelList);
    }

    @POST
    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testConnection(CrowdIntegration crowdIntegration) {
        return runService(securityFactory.testCrowdConnectionService(), crowdIntegration);
    }
}
