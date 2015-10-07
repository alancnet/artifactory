package org.artifactory.ui.rest.resource.builds;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.builds.BintrayModel;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.builds.BuildsServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Path("pushToBintray")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BuildBintrayResource extends BaseResource {

    @Autowired
    @Qualifier("streamingRestResponse")
    public void setArtifactoryResponse(RestResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    @Autowired
    BuildsServiceFactory buildsFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBintrayRepositories()
            throws Exception {
        return runService(buildsFactory.getBintrayRepositories());
    }

    @GET
    @Path("build/pkg")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBintrayPackages()
            throws Exception {
        return runService(buildsFactory.getBintrayPackages());
    }

    @GET
    @Path("build/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBintrayVersions()
            throws Exception {
        return runService(buildsFactory.getBintrayVersions());
    }

    @POST
    @Path("build/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pushToBintray(BintrayModel bintrayModel)
            throws Exception {
        return runService(buildsFactory.pushToBintray(), bintrayModel);
    }

    @GET
    @Path("artifact")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBintrayArtifactData()
            throws Exception {
        return runService(buildsFactory.getBintrayArtifact());
    }

    @POST
    @Path("artifact")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pushArtifactToBintray(BintrayModel bintrayModel)
            throws Exception {
        return runService(buildsFactory.pushArtifactToBintray(), bintrayModel);
    }
}
