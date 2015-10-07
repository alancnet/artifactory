package org.artifactory.ui.rest.resource.admin.configuration.repositories;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.configuration.ConfigServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.artifactory.ui.rest.resource.admin.configuration.repositories.RepoResourceConstants.PATH_REPOSITORIES;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@Component
@Path(PATH_REPOSITORIES)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Produces(MediaType.APPLICATION_JSON)
public class RepoConfigResource extends BaseResource {

    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRepositoryConfig(RepositoryConfigModel repositoryConfigModel) throws Exception {
        return runService(configServiceFactory.updateRepositoryConfig(), repositoryConfigModel);
    }

    @GET
    @Path("validatereponame")
    public Response validateRepoName() throws Exception {
        return runService(configServiceFactory.validateRepoName());
    }

    @GET
    @Path("{repoType: local|remote|virtual}/{repoKey}")
    public Response getRepositoryConfigByType() throws Exception {
        return runService(configServiceFactory.getRepositoryConfig());
    }

    @GET
    @Path("availablechoices")
    public Response getAvailableRepositoryFieldChoices() throws Exception {
        return runService(configServiceFactory.getAvailableRepositoryFieldChoices());
    }

    @GET
    @Path("defaultvalues")
    public Response getRepoConfigDefaultValues() throws Exception {
        return runService(configServiceFactory.getDefaultRepositoryValues());
    }

    @GET
    @Path("remoteUrlMap")
    public Response getRemoteReposUrlMapping() throws Exception {
        return runService(configServiceFactory.getRemoteReposUrlMapping());
    }

    @GET
    @Path("{repoType: local|remote|virtual}/info")
    public Response getRepositoriesInfo() throws Exception {
        return runService(configServiceFactory.getRepositoriesInfo());
    }

    @GET
    @Path("availablerepositories")
    public Response getAvailableRepositories() {
        return runService(configServiceFactory.getAvailableRepositories());
    }

    @GET
    @Path("indexeravailablerepositories")
    public Response getIndexerAvailableRepositories() {
        return runService(configServiceFactory.getIndexerAvailableRepositories());
    }

    @POST
    @Path("resolvedrepositories")
    public Response getResolvedRepositories(VirtualRepositoryConfigModel virtualConfigModel) {
        return runService(configServiceFactory.getResolvedRepositories(), virtualConfigModel);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRepository(RepositoryConfigModel repositoryConfigModel) throws Exception {
        return runService(configServiceFactory.createRepositoryConfig(), repositoryConfigModel);
    }

    @POST
    @Path("testremote")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response remoteRepositoryUrlTest(RemoteRepositoryConfigModel remoteRepositoryModel) {
        return runService(configServiceFactory.remoteRepositoryTestUrl(), remoteRepositoryModel);
    }

    @POST
    @Path("isartifactory")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response isArtifactoryInstance(RemoteRepositoryConfigModel remoteRepositoryModel) {
        return runService(configServiceFactory.isArtifactoryInstance(), remoteRepositoryModel);
    }

    @POST
    @Path("validatelocalreplication")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validateLocalReplicationConfig(LocalReplicationConfigModel localReplication) {
        return runService(configServiceFactory.validateLocalReplication(), localReplication);
    }

    @POST
    @Path("testlocalreplication")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response testLocalReplicationConfig(LocalRepositoryConfigModel localRepoModel) {
        return runService(configServiceFactory.testLocalReplication(), localRepoModel);
    }

    @POST
    @Path("testremotereplication")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response testRemoteReplicationConfig(RemoteRepositoryConfigModel remoteRepositoryModel) {
        return runService(configServiceFactory.testRemoteReplication(), remoteRepositoryModel);
    }

    @POST
    @Path("exeucteremotereplication")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeRemoteReplicationNow() {
        return runService(configServiceFactory.executeImmediateReplication());
    }

    @DELETE
    @Path("{repoKey}")
    public Response deleteRepository() throws Exception {
        return runService(configServiceFactory.deleteRepositoryConfig());
    }

    @POST
    @Path("executeall")
    public Response executeAllLocalReplications() throws Exception {
        return runService(configServiceFactory.executeAllLocalReplications());
    }

    @POST
    @Path("executereplicationnow")
    public Response executeLocalReplication(LocalRepositoryConfigModel localRepoModel) {
        return runService(configServiceFactory.executeLocalReplication(), localRepoModel);
    }

    @POST
    @Path("{repoType: local|remote|virtual}/reorderrepositories")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reorderRepositories(List<String> newOrderList) {
        return runService(configServiceFactory.reorderRepositories(), newOrderList);
    }

    @GET
    @Path("isjcenterconfigured")
    @RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
    public Response isJcenterConfigured() {
        return runService(configServiceFactory.isJcenterConfigured());
    }

    @POST
    @Path("createdefaultjcenterrepo")
    public Response createDefaultJcenterRepo() {
        return runService(configServiceFactory.createDefaultJcenterRepo());
    }
}