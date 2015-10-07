package org.artifactory.ui.rest.resource.admin.configuration.layouts;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.ui.rest.model.admin.configuration.layouts.LayoutConfigViewModel;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.configuration.ConfigServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Lior Hasson
 */
@Component
@Path("admin/repolayouts")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Produces(MediaType.APPLICATION_JSON)
public class RepoLayoutsResource extends BaseResource {

    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @Autowired
    protected CentralConfigService centralConfigService;

    @GET
    public Response getLayouts() throws Exception {
        return runService(configServiceFactory.getLayoutsService());
    }

    @GET
    @Path("{layoutKey}")
    public Response getLayoutConfig() throws Exception {
        return runService(configServiceFactory.getLayoutInfoService());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateLayout(RepoLayout repoLayout) throws Exception {
        return runService(configServiceFactory.updateLayoutService(), repoLayout);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createLayout(RepoLayout repoLayout) throws Exception {
        return runService(configServiceFactory.createLayoutService(), repoLayout);
    }

    @DELETE
    @Path("{layoutKey}")
    public Response deleteLayout() throws Exception {
        return runService(configServiceFactory.deleteLayoutService());
    }

    @POST
    @Path("testArtPath")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response testArtifactPathLayout(LayoutConfigViewModel repositoryLayoutModel) throws Exception {
        return runService(configServiceFactory.testArtPathService(), repositoryLayoutModel);
    }

    @POST
    @Path("resolveRegex")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resolveRegexLayout(RepoLayout repoLayout) throws Exception {
        return runService(configServiceFactory.resolveRegexService(), repoLayout);
    }
}
