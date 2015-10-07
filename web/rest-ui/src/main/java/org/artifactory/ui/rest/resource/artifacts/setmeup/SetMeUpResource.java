package org.artifactory.ui.rest.resource.artifacts.setmeup;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.setmeup.GradleSettingModel;
import org.artifactory.ui.rest.model.setmeup.IvySettingModel;
import org.artifactory.ui.rest.model.setmeup.MavenSettingModel;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.general.GeneralServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 *
 */
@Path("setMeUp")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SetMeUpResource extends BaseResource {

    @Autowired
    @Qualifier("streamingRestResponse")
    public void setArtifactoryResponse(RestResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    @Autowired
    GeneralServiceFactory generalFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRepoKeyTypeForSetMeUp()
            throws Exception {
        return runService(generalFactory.getSetMeUp());
    }

    @GET
    @Path("mavenSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMavenSettings()
            throws Exception {
        return runService(generalFactory.mavenSettingGenerator());
    }

    @GET
    @Path("gradleSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGradleSettings()
            throws Exception {
        return runService(generalFactory.gradleSettingGenerator());
    }

    @GET
    @Path("ivySettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIvySettings()
            throws Exception {
        return runService(generalFactory.ivySettingGenerator());
    }

    @POST
    @Path("mavenSnippet")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateMavenSnippet(MavenSettingModel mavenSettingModel)
    throws Exception {
        return runService(generalFactory.getMavenSettingSnippet(),mavenSettingModel);
    }

    @POST
    @Path("gradleSnippet")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateGradleSnippet(GradleSettingModel gradleSettingModel)
            throws Exception {
        return runService(generalFactory.getGradleSettingSnippet(),gradleSettingModel);
    }

    @POST
    @Path("ivySnippet")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateIvySnippet(IvySettingModel ivySettingModel)
            throws Exception {
        return runService(generalFactory.GetIvySettingSnippet(), ivySettingModel);
    }

    @POST
    @Path("downloadBuildGradle")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_PLAIN)
    public Response downloadGradleProperties(@FormParam("data") String data)
            throws Exception {
        GradleSettingModel gradleSettingModel = (GradleSettingModel) JsonUtil.mapDataToModel(data,
                GradleSettingModel.class);
        return runService(generalFactory.getGradleSettingSnippet(), gradleSettingModel);
    }

    @POST
    @Path("downloadBuildMaven")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_XML)
    public Response downloadMavenSnippet(@FormParam("data") String data)
            throws Exception {
        MavenSettingModel mavenSettingModel = (MavenSettingModel) JsonUtil.mapDataToModel(data,
                MavenSettingModel.class);
        return runService(generalFactory.getMavenSettingSnippet(), mavenSettingModel);
    }

    @POST
    @Path("downloadBuildIvy")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_XML)
    public Response generateIvySnippet(@FormParam("data") String data)
            throws Exception {
        IvySettingModel ivySettingModel = (IvySettingModel) JsonUtil.mapDataToModel(data,
                IvySettingModel.class);
        return runService(generalFactory.GetIvySettingSnippet(), ivySettingModel);
    }

    @GET
    @Path("mavenDistributionManagement")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mavenDistributionManagement()
            throws Exception {
        return runService(generalFactory.getMavenDistributionMgnt());
    }
}
