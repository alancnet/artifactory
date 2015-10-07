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

package org.artifactory.webapp.wicket.page.admin;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.AddonsWebManager;
import org.artifactory.addon.NoInstalledLicenseAction;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.artifactory.webapp.wicket.page.config.general.GeneralConfigPage;
import org.artifactory.webapp.wicket.page.config.license.LicensePage;
import org.artifactory.webapp.wicket.page.security.acl.AclsPage;

/**
 * @author Yoav Aharoni
 */
public class AdminPage extends AuthenticatedPage {

    @SpringBean
    private AuthorizationService authService;

    @SpringBean
    private AddonsManager addonsManager;

    @SpringBean
    private AddonsWebManager addonsWebManager;

    public AdminPage() {
        if (authService.isAdmin()) {
            // for now redirect all valid admin requests to the general configuration tab
            throw new RestartResponseException(GeneralConfigPage.class);
        } else if (authService.hasPermission(ArtifactoryPermission.MANAGE)) {
            throw new RestartResponseException(AclsPage.class);
        }

        // In this special condition when no license is installed we allow non-admin to visit the license page
        addonsWebManager.onNoInstalledLicense(false, new NoInstalledLicenseAction() {
            @Override
            public void act() {
                throw new RestartResponseException(LicensePage.class);
            }
        });

        // If non of the above is applicable, then the user is unauthorized!
        throw new UnauthorizedInstantiationException(getClass());
    }

    @Override
    public String getPageName() {
        return "Administration";
    }
}
