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
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.constant.ArtifactRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.artifactory.api.rest.constant.ArtifactRestConstants.PATH_COPY;

/**
 * REST API used to copy an artifact from one path to another.
 *
 * @author Tomer Cohen
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Path(PATH_COPY)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class CopyResource {

    /**
     * Copy an item (file or folder) from one path to another.
     *
     * @param path            The source path.
     * @param target          The target path.
     * @param dryRun          Flag whether to perform a dry run before executing the actual copy.
     * @param suppressLayouts Flag to indicate whether path translation across different layouts should be suppressed.
     * @param failFast        Flag to indicate whether the operation should fail upon encountering an error.
     * @return The operation result
     * @throws Exception
     */
    @POST
    @Path("{path: .+}")
    @Produces({ArtifactRestConstants.MT_COPY_MOVE_RESULT, MediaType.APPLICATION_JSON})
    public Response copy(
            // The path of the source item to be moved/copied
            @PathParam("path") String path,
            // The target repository to to move/copy the item.
            @QueryParam(ArtifactRestConstants.PARAM_TARGET) String target,
            // Flag to indicate whether to perform a dry run first. default false
            @QueryParam(ArtifactRestConstants.PARAM_DRY_RUN) int dryRun,
            // Flag to indicate whether path translation across different layouts should be suppressed. default false
            @QueryParam(ArtifactRestConstants.PARAM_SUPPRESS_LAYOUTS) int suppressLayouts,
            //Flag to indicate whether the operation should fail upon encountering an error. default false
            @QueryParam(ArtifactRestConstants.PARAM_FAIL_FAST) int failFast) throws Exception {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        return addonsManager.addonByType(RestAddon.class).copy(path, target, dryRun, suppressLayouts, failFast);
    }
}
