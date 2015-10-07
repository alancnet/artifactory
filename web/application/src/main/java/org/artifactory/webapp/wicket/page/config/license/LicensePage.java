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

package org.artifactory.webapp.wicket.page.config.license;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.util.CookieUtils;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;

/**
 * Artifactory licensing information and installation.
 *
 * @author Yossi Shaul
 */
@AuthorizeInstantiation(AuthorizationService.ROLE_USER)
public class LicensePage extends AuthenticatedPage {
    public static final String COOKIE_LICENSE_PAGE_VISITED = "license-page-visited";

    @SpringBean
    private AddonsManager addonsManager;

    @SpringBean
    private AuthorizationService authService;

    public LicensePage() {
        Form form = new SecureForm("form");
        add(form);

        LicensePanel licensePanel = new LicensePanel("licensePanel");
        form.add(licensePanel);

        form.add(licensePanel.createSaveButton(form));
        form.add(createCancelButton());

        if (addonsManager.isLicenseInstalled() && !authService.isAdmin()) {
            throw new UnauthorizedInstantiationException(getClass());
        }

        CookieUtils.setCookie(LicensePage.COOKIE_LICENSE_PAGE_VISITED, "true");
    }

    /**
     * Creates a cancel button for the panel
     *
     * @return TitledAjaxLink - The cancel button
     */
    private TitledAjaxLink createCancelButton() {
        return new TitledAjaxLink("cancel", "Cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(LicensePage.class);
            }
        };
    }

    @Override
    public String getPageName() {
        return "Artifactory Pro License";
    }
}