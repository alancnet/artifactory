package org.artifactory.ui.rest.resource.artifacts.search;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.artifacts.search.DeleteArtifactsModel;
import org.artifactory.ui.rest.model.artifacts.search.checksumsearch.ChecksumSearch;
import org.artifactory.ui.rest.model.artifacts.search.classsearch.ClassSearch;
import org.artifactory.ui.rest.model.artifacts.search.gavcsearch.GavcSearch;
import org.artifactory.ui.rest.model.artifacts.search.quicksearch.QuickSearch;
import org.artifactory.ui.rest.model.artifacts.search.remotesearch.RemoteSearch;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.search.SearchServiceFactory;
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
@Path("artifactsearch")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArtifactSearchResource extends BaseResource {

    @Autowired
    SearchServiceFactory searchFactory;

    @POST
    @Path("quick")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response quickSearch(QuickSearch quickSearch)
            throws Exception {
        return runService(searchFactory.quickSearchService(), quickSearch);
    }

    @POST
    @Path("class")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response classSearch(ClassSearch classSearch)
            throws Exception {
        return runService(searchFactory.classSearchService(), classSearch);
    }

    @POST
    @Path("gavc")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response gavcSearch(GavcSearch gavcSearch)
            throws Exception {
        return runService(searchFactory.gavcSearchService(), gavcSearch);
    }

    @POST
    @Path("checksum")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response checksumSearch(ChecksumSearch checksumSearch)
            throws Exception {
        return runService(searchFactory.checksumSearchService(), checksumSearch);
    }

    @POST
    @Path("remote")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response remoteSearch(RemoteSearch remoteSearch)
            throws Exception {
        return runService(searchFactory.remoteSearchService(), remoteSearch);
    }

    @POST
    @Path("deleteArtifact")
    public Response deleteArtifacts(DeleteArtifactsModel deleteArtifactsModel)
            throws Exception {
        return runService(searchFactory.deleteArtifactsService(), deleteArtifactsModel);
    }





}
