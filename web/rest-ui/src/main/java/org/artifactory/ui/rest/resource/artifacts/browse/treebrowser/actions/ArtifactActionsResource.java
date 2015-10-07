package org.artifactory.ui.rest.resource.artifacts.browse.treebrowser.actions;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.CopyArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.DeleteArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.DownloadArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.MoveArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.ViewArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.WatchArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.ZapArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.recalculateindex.BaseIndexCalculator;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.browse.BrowseServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@Path("artifactactions")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArtifactActionsResource extends BaseResource {

    @Autowired
    BrowseServiceFactory browseFactory;

    @Autowired
    @Qualifier("streamingRestResponse")
    public void setArtifactoryResponse(RestResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    @POST
    @Path("copy")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response copyArtifact(CopyArtifact copyArtifact)
            throws Exception {
        return runService(browseFactory.copyArtifactService(), copyArtifact);
    }

    @POST
    @Path("move")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response moveArtifact(MoveArtifact copyAction)
            throws Exception {
        return runService(browseFactory.moveArtifactService(), copyAction);
    }

    @POST
    @Path("download")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response downloadArtifact(DownloadArtifact downloadArtifact)
            throws Exception {
        return runService(browseFactory.downloadArtifactService(), downloadArtifact);
    }

    @GET
    @Path("downloadfolderinfo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFolderDownloadInfo() throws Exception {
        return runService(browseFactory.getDownloadFolderInfo());
    }

    @GET
    @Path("downloadfolder")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFolder() throws Exception {
        return runService(browseFactory.downloadFolder());
    }

    @POST
    @Path("watch")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response watchArtifact(WatchArtifact watchArtifact)
            throws Exception {
        return runService(browseFactory.watchArtifactService(), watchArtifact);
    }

    @POST
    @Path("delete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteArtifact(DeleteArtifact deleteArtifact)
            throws Exception {
        return runService(browseFactory.deleteArtifactService(), deleteArtifact);
    }

    @POST
    @Path("view")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response viewArtifact(ViewArtifact viewArtifact)
            throws Exception {
        return runService(browseFactory.viewArtifactService(), viewArtifact);
    }

    @POST
    @Path("zap")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response zapArtifact(ZapArtifact zapArtifact)
            throws Exception {
        return runService(browseFactory.zapArtifactService(), zapArtifact);
    }

    @POST
    @Path("calculateIndex")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response calculateIndex(BaseIndexCalculator baseIndexCalculator)
            throws Exception {
        return runService(browseFactory.recalculateIndex(), baseIndexCalculator);
    }

    @POST
    @Path("zapVirtual")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response zapVirtual(ZapArtifact zapArtifact)
            throws Exception {
        return runService(browseFactory.zapCachesVirtual(), zapArtifact);
    }
}
