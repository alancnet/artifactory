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

package org.artifactory.rest.resource.artifact;


import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.AuthorizationRestException;
import org.artifactory.addon.smartrepo.SmartRepoAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.model.xstream.fs.StatsImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.rest.common.exception.NotFoundException;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.storage.service.StatsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.artifactory.api.rest.constant.ArtifactRestConstants.*;

/**
* Created by Michael Pasternak on 8/13/15.
 *
 * This resources processes remote statistics events
*/
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(PATH_ROOT + "/" + PATH_STATISTICS + "/{" + PATH_PARAM + ": .+}")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class ArtifactStatisticsResource {

    private static final Logger log = LoggerFactory.getLogger(ArtifactStatisticsResource.class);

    @Context
    private HttpServletRequest request;

    @Autowired
    private AuthorizationService authorizationService;

    @PathParam(PATH_PARAM)
    private String path;

    @Autowired
    private InternalRepositoryService repositoryService;

    @Autowired
    private CentralConfigService centralConfig;

    @Autowired
    private StatsServiceImpl statsService;

    /**
     * Updates artifact statistics triggered by remote host
     *
     * @param statsInfos a collection of events for the given repository
     *
     * @return {@link Response}
     *
     * @throws IOException when artifact is not exist/readable
     */
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    public Response updateRemoteStats(StatsImpl[] statsInfos) throws IOException {

        String remoteHost = request.getRemoteHost();
        for (StatsImpl statsInfo : statsInfos) {

            RepoPath remoteRepoPath = RestUtils.calcRepoPathFromRequestPath(path + "/" + statsInfo.getRepoPath());
            log.debug("Updating remote statistics (triggered by host \"{}\") on artifact \"{}\".",
                    remoteHost, remoteRepoPath
            );

            if (!authorizationService.canRead(remoteRepoPath)) {
                return unAuthorizedResponse(remoteRepoPath);
            }
            RepoPath localRepoPath = RepoPathFactory.create(remoteRepoPath.getRepoKey().replace("-cache", ""),
                    remoteRepoPath.getPath());


            boolean existsInRemote = repositoryService.exists(remoteRepoPath);
            boolean existsILocal = repositoryService.exists(localRepoPath);

            if (!existsInRemote && !existsILocal) {
                throw new NotFoundException("Unable to find item");
            }

            RepoPath repoPath = existsInRemote ? remoteRepoPath : localRepoPath;
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            SmartRepoAddon smartRepoAddon = addonsManager.addonByType(SmartRepoAddon.class);
            smartRepoAddon.fileDownloadedRemotely(statsInfo, remoteHost, repoPath);
        }
        return Response.ok().build();
    }

    /**
     * Prepares appropriate response
     *
     * @param unAuthorizedResource
     * @return
     * @throws IOException
     */
    private Response unAuthorizedResponse(RepoPath unAuthorizedResource) throws IOException {
        boolean hideUnauthorizedResources = centralConfig.getDescriptor().getSecurity().isHideUnauthorizedResources();
        if (hideUnauthorizedResources) {
            throw new NotFoundException("Resource not found");
        } else {
            throw new AuthorizationRestException("Request for '" + unAuthorizedResource + "' is forbidden for user '" +
                    authorizationService.currentUsername() + "'.");
        }
    }
}
