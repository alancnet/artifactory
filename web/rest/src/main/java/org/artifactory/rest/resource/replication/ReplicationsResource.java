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

import org.artifactory.api.rest.constant.ReplicationsRestConstants;
import org.artifactory.api.rest.replication.MultipleReplicationConfigRequest;
import org.artifactory.api.rest.replication.ReplicationConfigRequest;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.resource.BaseResource;
import org.artifactory.rest.services.ConfigServiceFactory;
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

/**
 * REST resource for configuring local and remote replication.
 *
 * @author mamo
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(ReplicationsRestConstants.ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
public class ReplicationsResource extends BaseResource {

    @Autowired
    ConfigServiceFactory configFactory;

    /**
     * get Single or multiple replications
     */
    @GET
    @Path("{repoKey: .+}")
    @Produces({ReplicationsRestConstants.MT_REPLICATION_CONFIG_REQUEST, ReplicationsRestConstants.MT_MULTI_REPLICATION_CONFIG_REQUEST, MediaType.APPLICATION_JSON})
    public Response getReplication() {
        return runService(configFactory.getReplication());
    }
    /**
     * create or replace single replication
     */
    @PUT
    @Path("{repoKey: .+}")
    @Consumes({ReplicationsRestConstants.MT_REPLICATION_CONFIG_REQUEST, MediaType.APPLICATION_JSON})
    public Response addOrReplace(ReplicationConfigRequest replicationRequest) {
        return runService(configFactory.createOrReplaceReplication(), replicationRequest);
    }
    /**
     * create or replace multiple replication
     */
    @PUT
    @Consumes({ReplicationsRestConstants.MT_MULTI_REPLICATION_CONFIG_REQUEST, MediaType.APPLICATION_JSON})
    @Path("multiple{repoKey: .+}")
    public Response addOrReplaceMultiple(MultipleReplicationConfigRequest replicationRequest) {
        return runService(configFactory.createMultipleReplication(), replicationRequest);
    }
    /**
     * update existing single replication
     */
    @POST
    @Consumes({ReplicationsRestConstants.MT_REPLICATION_CONFIG_REQUEST, MediaType.APPLICATION_JSON})
    @Path("{repoKey: .+}")
    public Response updateReplications(ReplicationConfigRequest replicationRequest) {
        return runService(configFactory.updateReplication(), replicationRequest);
    }

    /**
     * update existing multi replication
     */
    @POST
    @Consumes({ReplicationsRestConstants.MT_MULTI_REPLICATION_CONFIG_REQUEST, MediaType.APPLICATION_JSON})
    @Path("multiple{repoKey: .+}")
    public Response updateMultipleReplications(MultipleReplicationConfigRequest replicationRequest) {
        return runService(configFactory.updateMultipleReplications(), replicationRequest);
    }
    /**
     * delete  existing single / multi replication
     */
    @DELETE
    @Path("{repoKey: .+}")
    public Response deleteReplications() {
        return runService(configFactory.deleteReplicationsService());
    }
}
