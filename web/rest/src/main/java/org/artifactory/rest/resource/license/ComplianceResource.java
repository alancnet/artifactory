/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.rest.resource.license;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.blackduck.BlackDuckAddon;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.compliance.FileComplianceInfo;
import org.artifactory.api.rest.constant.ComplianceConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.util.RestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author mamo
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Path(ComplianceConstants.PATH_ROOT)
public class ComplianceResource {

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private RepositoryService repositoryService;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("{path: .+}")
    public Response getCachedExternalInfo(@PathParam("path") String path) {
        BlackDuckAddon blackDuckAddon = addonsManager.addonByType(BlackDuckAddon.class);
        RepoPath repoPath = RestUtils.calcRepoPathFromRequestPath(path);

        if (!repositoryService.exists(repoPath)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Could not find artifact for " + path).build();
        }

        try {
            FileComplianceInfo fileComplianceInfo = blackDuckAddon.getExternalInfoFromMetadata(repoPath);

            if (fileComplianceInfo == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Could not find compliance info for " + path).
                        build();
            }

            return Response.ok(fileComplianceInfo).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity("An error occurred while trying to get compliance info for " + path).build();
        }
    }
}
