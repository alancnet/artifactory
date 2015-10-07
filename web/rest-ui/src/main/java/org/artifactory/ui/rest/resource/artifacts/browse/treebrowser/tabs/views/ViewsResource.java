package org.artifactory.ui.rest.resource.artifacts.browse.treebrowser.tabs.views;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.ViewArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.bower.BowerArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.DockerArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.ancestry.DockerAncestryArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.gems.GemsArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.npm.NpmArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.nugetinfo.NugetArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.pypi.PypiArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.rpm.RpmArtifactInfo;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.browse.BrowseServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@Path("views")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ViewsResource extends BaseResource {

    @Autowired
    BrowseServiceFactory browseFactory;

    @POST
    @Path("pom")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response viewPom(ViewArtifact viewArtifact)
            throws Exception {
        return runService(browseFactory.viewArtifactService(), viewArtifact);
    }

    @POST
    @Path("nuget")

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response viewNuget(NugetArtifactInfo nugetArtifactInfo)
            throws Exception {
        return runService(browseFactory.nugetViewService(), nugetArtifactInfo);
    }

    @POST
    @Path("gems")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response viewGems(GemsArtifactInfo gemsArtifactInfo)
            throws Exception {
        return runService(browseFactory.gemsViewService(), gemsArtifactInfo);
    }

    @POST
    @Path("npm")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response viewGems(NpmArtifactInfo npmArtifactInfo)
            throws Exception {
        return runService(browseFactory.npmViewService(), npmArtifactInfo);
    }

    @POST
    @Path("rpm")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response viewRpm(RpmArtifactInfo rpmArtifactInfo)
            throws Exception {
        return runService(browseFactory.rpmViewService(), rpmArtifactInfo);
    }

    @POST
    @Path("pypi")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response viewPypi(PypiArtifactInfo pypiArtifactInfo)
            throws Exception {
        return runService(browseFactory.pypiViewService(), pypiArtifactInfo);
    }

    @POST
    @Path("bower")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response viewBower(BowerArtifactInfo bowerArtifactInfo)
            throws Exception {
        return runService(browseFactory.bowerViewService(), bowerArtifactInfo);
    }

    @POST
    @Path("docker")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response viewDocker(DockerArtifactInfo dockerArtifactInfo)
            throws Exception {
        return runService(browseFactory.dockerViewService(), dockerArtifactInfo);
    }

    @POST
    @Path("dockerv2")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response viewDockerV2(DockerArtifactInfo dockerArtifactInfo)
            throws Exception {
        return runService(browseFactory.dockerV2ViewService(), dockerArtifactInfo);
    }

    @POST
    @Path("dockerancestry")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response viewDockerAncestry(DockerAncestryArtifactInfo dockerAncestryArtifactInfo)
            throws Exception {
        return runService(browseFactory.dockerAncestryViewService(), dockerAncestryArtifactInfo);
    }
}
