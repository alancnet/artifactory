/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.rest.resource.repositories;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.constant.RepositoriesRestConstants;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.repo.RepoDetails;
import org.artifactory.repo.RepoDetailsType;
import org.artifactory.rest.common.util.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import static org.artifactory.repo.RepoDetailsType.*;

/**
 * A resource to manage all repository related operations
 *
 * @author Noam Tenne
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(RepositoriesRestConstants.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class RepositoriesResource {

    private static final Logger log = LoggerFactory.getLogger(RepositoriesResource.class);


    @Context
    HttpServletRequest httpRequest;

    @Context
    HttpServletResponse httpResponse;

    @Context
    private HttpHeaders requestHeaders;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    AddonsManager addonsManager;

    @Autowired
    SearchService searchService;

    /**
     * Repository configuration resource delegator
     *
     * @return RepoConfigurationResource
     */
    @Path("{repoKey}/" + RepositoriesRestConstants.PATH_CONFIGURATION)
    @Deprecated
    public RepoConfigurationResource getRepoConfigResource(@PathParam("repoKey") String repoKey) {
        return new RepoConfigurationResource(repositoryService, repoKey);
    }

    /**
     * Get repository configuration depending on the repository's type
     *
     * @param repoKey The repo Key
     * @return The repository configuration JSON
     */
    @GET
    @Path("{repoKey: .+}")
    @Produces({RepositoriesRestConstants.MT_LOCAL_REPOSITORY_CONFIGURATION,
            RepositoriesRestConstants.MT_REMOTE_REPOSITORY_CONFIG,
            RepositoriesRestConstants.MT_VIRTUAL_REPOSITORY_CONFIGURATION, MediaType.APPLICATION_JSON})
    public Response getRepoConfig(@PathParam("repoKey") String repoKey) {
        MediaType mediaType = requestHeaders.getMediaType();
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return restAddon.getRepositoryConfiguration(repoKey, mediaType);
    }

    @PUT
    @Consumes(
            {RepositoriesRestConstants.MT_LOCAL_REPOSITORY_CONFIGURATION,
                    RepositoriesRestConstants.MT_REMOTE_REPOSITORY_CONFIG,
                    RepositoriesRestConstants.MT_VIRTUAL_REPOSITORY_CONFIGURATION, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{repoKey: .+}")
    public Response createOrReplaceRepository(@PathParam("repoKey") String repoKey,
            @QueryParam(RepositoriesRestConstants.POSITION) int position, Map repositoryConfiguration)
            throws IOException {
        MediaType mediaType = requestHeaders.getMediaType();
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return restAddon.createOrReplaceRepository(repoKey, repositoryConfiguration, mediaType, position);
    }

    @POST
    @Consumes(
            {RepositoriesRestConstants.MT_LOCAL_REPOSITORY_CONFIGURATION,
                    RepositoriesRestConstants.MT_REMOTE_REPOSITORY_CONFIG,
                    RepositoriesRestConstants.MT_VIRTUAL_REPOSITORY_CONFIGURATION, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{repoKey: .+}")
    public Response updateRepository(@PathParam("repoKey") String repoKey, Map repositoryConfiguration)
            throws IOException {
        MediaType mediaType = requestHeaders.getMediaType();
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return restAddon.updateRepository(repoKey, repositoryConfiguration, mediaType);
    }

    /**
     * Returns a JSON list of repository details.
     * <p/>
     * NOTE: Used by CI integration to get a list of deployment repository targets.
     *
     * @param repoType Name of repository type, as defined in {@link org.artifactory.repo.RepoDetailsType}. Can be null
     * @return JSON repository details list. Will return details of defined type, if given. And will return details of
     *         all types if not
     * @throws Exception
     */
    @GET
    @Produces({RepositoriesRestConstants.MT_REPOSITORY_DETAILS_LIST, MediaType.APPLICATION_JSON})
    public List<RepoDetails> getAllRepoDetails(@QueryParam(RepositoriesRestConstants.PARAM_REPO_TYPE)
    String repoType) throws Exception {
        return getRepoDetailsList(repoType);
    }
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{repoKey: .+}")
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response deleteRepository(@PathParam("repoKey") String repoKey) throws IOException {
        if (StringUtils.isBlank(repoKey)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Repo key must not be null\n").build();
        }
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return restAddon.deleteRepository(repoKey);
    }

    /**
     * Returns a list of repository details
     *
     * @param repoType Name of repository type, as defined in {@link org.artifactory.repo.RepoDetailsType}. Can be null
     * @return List of repository details
     */
    private List<RepoDetails> getRepoDetailsList(String repoType) throws Exception {
        List<RepoDetails> detailsList = Lists.newArrayList();

        boolean noTypeSelected = StringUtils.isBlank(repoType);
        RepoDetailsType selectedType = null;
        if (!noTypeSelected) {
            try {
                selectedType = valueOf(repoType.toUpperCase());
            } catch (IllegalArgumentException e) {
                //On an unfound type, return empty list
                return detailsList;
            }
        }

        if (noTypeSelected || LOCAL.equals(selectedType)) {
            addLocalOrVirtualRepoDetails(detailsList, repositoryService.getLocalRepoDescriptors(), LOCAL);
        }

        if (noTypeSelected || REMOTE.equals(selectedType)) {
            addRemoteRepoDetails(detailsList);
        }

        if (noTypeSelected || VIRTUAL.equals(selectedType)) {
            addLocalOrVirtualRepoDetails(detailsList, repositoryService.getVirtualRepoDescriptors(), VIRTUAL);
        }
        return detailsList;
    }

    /**
     * Adds a list of local or virtual repositories to the repository details list
     *
     * @param detailsList List that details should be appended to
     * @param reposToAdd  List of repositories to add details of
     * @param type        Type of repository which is being added
     */
    private void addLocalOrVirtualRepoDetails(List<RepoDetails> detailsList, List<? extends RepoDescriptor> reposToAdd,
            RepoDetailsType type) {
        for (RepoDescriptor repoToAdd : reposToAdd) {
            String key = repoToAdd.getKey();
            if (authorizationService.userHasPermissionsOnRepositoryRoot(key)) {
                detailsList.add(new RepoDetails(key, repoToAdd.getDescription(), type, getRepoUrl(key)));
            }
        }
    }

    /**
     * Adds a list of remote repositories to the repo details list
     *
     * @param detailsList List that details should be appended to
     */
    private void addRemoteRepoDetails(List<RepoDetails> detailsList) {
        List<RemoteRepoDescriptor> remoteRepos = repositoryService.getRemoteRepoDescriptors();

        for (RemoteRepoDescriptor remoteRepo : remoteRepos) {
            String key = remoteRepo.getKey();
            if (authorizationService.userHasPermissionsOnRepositoryRoot(key)) {
                String configUrl = null;
                if (remoteRepo.isShareConfiguration()) {
                    configUrl = getRepoConfigUrl(key);
                }

                detailsList.add(new RepoDetails(key, remoteRepo.getDescription(), REMOTE, remoteRepo.getUrl(),
                        configUrl));
            }
        }
    }

    /**
     * Returns the repository browse URL
     *
     * @param repoKey Key of repository to assemble URL for
     * @return Repository URL
     */
    private String getRepoUrl(String repoKey) {
        return new StringBuilder().append(RestUtils.getServletContextUrl(httpRequest)).append("/").append(repoKey)
                .toString();
    }

    /**
     * Returns the repository configuration URL
     *
     * @param repoKey Key of repository to assemble URL for
     * @return Repository configuration URL
     */
    private String getRepoConfigUrl(String repoKey) {
        return new StringBuilder().append(RestUtils.getRestApiUrl(httpRequest)).append("/")
                .append(RepositoriesRestConstants.PATH_ROOT).append("/").append(repoKey).append("/").
                        append(RepositoriesRestConstants.PATH_CONFIGURATION).toString();
    }
}
