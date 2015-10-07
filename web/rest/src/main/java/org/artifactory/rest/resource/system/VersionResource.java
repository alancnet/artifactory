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

package org.artifactory.rest.resource.system;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.config.VersionInfo;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.rest.search.result.VersionRestResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.security.ArtifactoryPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Resource to get information about Artifactory's current running version and revision as well as a list of currently
 * enabled addons in Artifactory.
 *
 * @author Tomer Cohen
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Path(SystemRestConstants.PATH_ROOT + "/" + SystemRestConstants.PATH_VERSION)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class VersionResource {

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * @return The artifactory version and enabled addons
     */
    @GET
    @Produces({SystemRestConstants.MT_VERSION_RESULT, MediaType.APPLICATION_JSON})
    public VersionRestResult getArtifactoryVersion() {
        VersionInfo versionInfo = centralConfigService.getVersionInfo();
        List<String> addonNames = addonsManager.getEnabledAddonNames();

        // Add the license to the JSON result object only for Artifactory Pro and user with deploy permissions
        String licenseKeyHash = null;
        if (authorizationService.hasPermission(ArtifactoryPermission.DEPLOY)) {
            licenseKeyHash = addonsManager.getLicenseKeyHash();
        }

        // By assigning null we prevent the field from appearing in the result (our parser ignores nulls)
        String license = "".equals(licenseKeyHash) ? null : licenseKeyHash;

        VersionRestResult versionRestResult =
                new VersionRestResult(versionInfo.getVersion(), versionInfo.getRevision(), addonNames, license);
        return versionRestResult;
    }
}
