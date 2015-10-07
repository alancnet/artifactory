package org.artifactory.ui.rest.resource.artifacts.browse.treebrowser.tabs.generalinfo;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.RestGeneralTab;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.BaseInfo;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.browse.BrowseServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@Path("artifactgeneral")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GeneralArtifactResource extends BaseResource {

    @Autowired
    BrowseServiceFactory browseFactory;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response populateTreeBrowser(RestGeneralTab generalTab) throws Exception {
        return runService(browseFactory.getGetGeneralArtifactsService(), generalTab);
    }

    @POST
    @Path("bintray")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response populateBintrayInfo() throws Exception {
        return runService(browseFactory.getGetGeneralBintrayService());
    }

    @POST
    @Path("artifactsCount")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getArtifactCount(BaseInfo baseInfo) {
        return runService(browseFactory.getArtifactsCount(), baseInfo);
    }

}
