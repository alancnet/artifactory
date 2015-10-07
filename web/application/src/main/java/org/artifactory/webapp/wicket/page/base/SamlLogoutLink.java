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

package org.artifactory.webapp.wicket.page.base;

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.SamlAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.component.links.TitledPageLink;
import org.artifactory.webapp.wicket.application.ArtifactoryWebSession;

/**
 * Saml logout link
 *
 * @author Gidi Shabat
 */
public class SamlLogoutLink extends TitledPageLink {
    @SpringBean
    private AuthorizationService authorizationService;

    @SpringBean
    private AddonsManager addons;

    public SamlLogoutLink(String id, String caption) {
        super(id, caption,
                ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                        SamlAddon.class).getSamlLogoutRequestPageClass());
    }


    @Override
    public boolean isVisible() {
        return ArtifactoryWebSession.get().isSignedIn() && !authorizationService.isAnonymous();
    }
}