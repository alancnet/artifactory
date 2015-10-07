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

package org.artifactory.addon.wicket;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.artifactory.addon.Addon;

import javax.annotation.Nullable;

/**
 * An interface of the SAML functionality in the SSO addon.
 *
 * @author Gidi Shabat
 */
public interface SamlAddon extends Addon {

    String REALM = "saml";

    @Nullable
    Class<? extends WebPage> getSamlLoginRequestPageClass();

    @Nullable
    Class<? extends Page> getSamlLoginResponsePageClass();

    @Nullable
    Class<? extends WebPage> getSamlLogoutRequestPageClass();

    boolean isSamlEnabled();

    boolean isAutoRedirectToSamlIdentityProvider();

    String getSamlLoginIdentityProviderUrl();

    AbstractLink getLoginLink(String wicketId);

    AbstractLink getLogoutLink(String wicketId);
}
