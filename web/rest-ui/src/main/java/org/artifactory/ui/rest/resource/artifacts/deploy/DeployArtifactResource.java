package org.artifactory.ui.rest.resource.artifacts.deploy;

import com.sun.jersey.multipart.FormDataMultiPart;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadArtifactInfo;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.deploy.DeployServiceFactory;
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
@Path("artifact")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeployArtifactResource extends BaseResource {

    @Autowired
    DeployServiceFactory deployFactory;

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadArtifact(FormDataMultiPart formParams)
            throws Exception {
        UploadArtifactInfo uploadArtifactInfo = new UploadArtifactInfo(formParams);
        return runService(deployFactory.artifactUpload(), uploadArtifactInfo);
    }

    @POST
    @Path("cancelupload")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelUploadArtifact(UploadArtifactInfo uploadArtifactInfo)
            throws Exception {
        return runService(deployFactory.cancelArtifactUpload(), uploadArtifactInfo);
    }

    @POST
    @Path("deploy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deployArtifact(UploadArtifactInfo uploadArtifactInfo)
            throws Exception {
        return runService(deployFactory.deployArtifact(), uploadArtifactInfo);
    }

    @POST
    @Path("deploy/bundle")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadBundleArtifact(UploadArtifactInfo uploadArtifactInfo)
            throws Exception {
        return runService(deployFactory.artifactDeployBundle(), uploadArtifactInfo);
    }

    @POST
    @Path("deploy/multi")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deployMultiArtifact(FormDataMultiPart formParams)
            throws Exception {
        UploadArtifactInfo uploadArtifactInfo = new UploadArtifactInfo(formParams);
        return runService(deployFactory.artifactMultiDeploy(), uploadArtifactInfo);
    }
}
