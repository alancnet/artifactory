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

package org.artifactory.webapp.wicket.page.home;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.AddonsWebManager;
import org.artifactory.addon.NoInstalledLicenseAction;
import org.artifactory.addon.wicket.WebApplicationAddon;
import org.artifactory.common.wicket.util.CookieUtils;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.artifactory.webapp.wicket.page.config.license.LicensePage;

/**
 * @author Yoav Landman
 */
public class HomePage extends AuthenticatedPage {

    @SpringBean
    private AddonsManager addonsManager;

    @SpringBean
    private AddonsWebManager addonsWebManager;

    public HomePage() {
        add(new WelcomeBorder("welcomeBorder"));
        WebApplicationAddon applicationAddon = addonsManager.addonByType(WebApplicationAddon.class);
        WebMarkupContainer addonsInfoPanel = applicationAddon.getAddonsInfoPanel("addonsInfoPanel");
        add(addonsInfoPanel);

        boolean userVisitedLicensePage = CookieUtils.getCookie(LicensePage.COOKIE_LICENSE_PAGE_VISITED) != null;
        addonsWebManager.onNoInstalledLicense(userVisitedLicensePage, new NoInstalledLicenseAction() {
            @Override
            public void act() {
                throw new RestartResponseException(LicensePage.class);
            }
        });
    }

    @Override
    public String getPageName() {
        return "Welcome";
    }
}
