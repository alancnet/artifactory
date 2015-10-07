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

package org.artifactory.rest.resource.replication;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.constant.ReplicationRestConstants;
import org.artifactory.api.rest.replication.ReplicationRequest;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * REST resource for invoking local and remote replication procedures.
 *
 * @author Noam Y. Tenne
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Path(ReplicationRestConstants.ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class ReplicationResource {

    /**
     * Returns the latest replication status of the given path if annotated
     *
     * @param path Path to check for annotations
     * @return Response
     */
    @GET
    @Produces({ReplicationRestConstants.MT_REPLICATION_STATUS, MediaType.APPLICATION_JSON})
    @Path("{path: .+}")
    public Response getReplicationStatus(@PathParam("path") String path) {
        RepoPath repoPath = InternalRepoPathFactory.create(path);
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        return addonsManager.addonByType(RestAddon.class).getReplicationStatus(repoPath);
    }

    /**
     * Executes replication operations
     *
     * @param path               Replication root
     * @param replicationRequest Replication settings
     * @return Response
     */
    @POST
    @Consumes({ReplicationRestConstants.MT_REPLICATION_REQUEST, MediaType.APPLICATION_JSON})
    @Path("{path: .+}")
    public Response replicate(
            @PathParam("path") String path,
            ReplicationRequest replicationRequest) throws IOException {
        RepoPath repoPath = InternalRepoPathFactory.create(path);
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        return addonsManager.addonByType(RestAddon.class).replicate(repoPath, replicationRequest);
    }

    /**
     * Executes replication with default values and no JSON body
     * (using for pull, false properties and deletes and uses the remote repository URL and credentials)
     *
     * @param path Replication root
     */
    @POST
    @Path("{path: .+}")
    public Response replicateNoJson(@PathParam("path") String path) throws IOException {
        return replicate(path, new ReplicationRequest());
    }
}
