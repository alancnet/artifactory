package org.artifactory.ui.rest.resource.builds;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.builds.BuildGovernanceInfo;
import org.artifactory.ui.rest.model.builds.BuildLicenseModel;
import org.artifactory.ui.rest.model.builds.DeleteBuildsModel;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.builds.BuildsServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
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
@Path("builds")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BuildsResource extends BaseResource {

    @Autowired
    BuildsServiceFactory buildsFactory;

    @Autowired
    @Qualifier("streamingRestResponse")
    public void setArtifactoryResponse(RestResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBuilds()
            throws Exception {
        return runService(buildsFactory.getAllBuilds());
    }

    @GET
    @Path("history{name:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildHistory()
            throws Exception {
        return runService(buildsFactory.getBuildHistory());
    }

    @GET
    @Path("buildInfo/{name}/{number}{date:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildGeneralInfo()
            throws Exception {
        return runService(buildsFactory.getBuildGeneralInfo());
    }

    @GET
    @Path("publishedModules/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublishedModules()
            throws Exception {
        return runService(buildsFactory.getPublishedModules());
    }

    @GET
    @Path("modulesArtifact/{name}/{number}/{date}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModulesArtifact()
            throws Exception {
        return runService(buildsFactory.getModuleArtifacts());
    }

    @GET
    @Path("modulesDependency/{name}/{number}/{date}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModulesDependency()
            throws Exception {
        return runService(buildsFactory.getModuleDependency());
    }

    @POST
    @Path("buildsDelete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBuild(DeleteBuildsModel deleteBuildsModel) throws Exception {
        return runService(buildsFactory.deleteBuild(), deleteBuildsModel);
    }

    @POST
    @Path("deleteAllBuilds")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllBuild(DeleteBuildsModel deleteBuildsModel)
            throws Exception {
        return runService(buildsFactory.deleteAllBuilds(),deleteBuildsModel);
    }

    @GET
    @Path("buildJson/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildJson()
            throws Exception {
        return runService(buildsFactory.getBuildJson());
    }

    @GET
    @Path("artifactDiff/{name}/{number}/{date}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response artifactDiff()
            throws Exception {
        return runService(buildsFactory.diffBuildModuleArtifact());
    }

    @GET
    @Path("buildArtifactDiff/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buildArtifactDiff()
            throws Exception {
        return runService(buildsFactory.diffBuildArtifact());
    }

    @GET
    @Path("buildDependencyDiff/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buildDependencyDiff()
            throws Exception {

        return runService(buildsFactory.diffBuildDependencies());
    }

    @GET
    @Path("buildPropsDiff/{name}/{number}/{date}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buildPropsDiff()
            throws Exception {
        return runService(buildsFactory.diffBuildProps());
    }

    @GET
    @Path("buildProps/env/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEnvBuildProps()
            throws Exception {
        return runService(buildsFactory.getEnvBuildProps());
    }


    @GET
    @Path("buildDiff/{name}/{number}/{date}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buildDiff()
            throws Exception {
        return runService(buildsFactory.buildDiff());
    }


    @GET
    @Path("buildProps/system/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystemBuildProps()
            throws Exception {
        return runService(buildsFactory.getSystemBuildProps());
    }


    @GET
    @Path("buildIssues/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildIssues()
            throws Exception {
        return runService(buildsFactory.getBuildIssues());
    }

    @GET
    @Path("buildLicenses/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildLicense()
            throws Exception {
        return runService(buildsFactory.buildLicenses());
    }

    @POST
    @Path("exportLicenses")
    @Consumes("application/x-www-form-urlencoded")
    public Response exportLicense(@FormParam("data") String data)
            throws Exception {
        BuildLicenseModel buildLicenseModel = (BuildLicenseModel) JsonUtil.mapDataToModel(data, BuildLicenseModel.class);
        return runService(buildsFactory.exportLicenseToCsv(), buildLicenseModel);
    }

    @PUT
    @Path("overrideLicenses/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response overrideLicense(BuildLicenseModel buildLicenseModel)
            throws Exception {
        return runService(buildsFactory.overrideSelectedLicenses(), buildLicenseModel);
    }

    @GET
    @Path("changeLicenses/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChangeLicenseValues()
            throws Exception {
        return runService(buildsFactory.changeBuildLicense());
    }


    @GET
    @Path("releaseHistory/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildReleaseHistory()
            throws Exception {
        return runService(buildsFactory.buildReleaseHistory());
    }


    @GET
    @Path("dependencyDiff/{name}/{number}/{date}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response dependencyDiff()
            throws Exception {
        return runService(buildsFactory.diffBuildModuleDependency());
    }

    @GET
    @Path("prevBuild/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPrevBuild()
            throws Exception {
        return runService(buildsFactory.getPrevBuildList());
    }

    @GET
    @Path("buildGovernance/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildGovernance()
            throws Exception {
        return runService(buildsFactory.getBuildGovernance());
    }

    @PUT
    @Path("updateGovernance/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBuildGovernance(BuildGovernanceInfo buildGovernanceInfo)
            throws Exception {
        return runService(buildsFactory.updateGovernanceRequest(), buildGovernanceInfo);
    }
}
