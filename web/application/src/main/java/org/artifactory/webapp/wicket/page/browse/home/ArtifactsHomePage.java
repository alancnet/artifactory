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

package org.artifactory.webapp.wicket.page.browse.home;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Page;
import org.apache.wicket.request.handler.ComponentNotFoundException;
import org.artifactory.common.wicket.util.CookieUtils;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.util.HttpUtils;
import org.artifactory.webapp.wicket.page.browse.treebrowser.BrowseRepoPage;

/**
 * @author Yoav Aharoni
 */
public class ArtifactsHomePage extends Page {
    public ArtifactsHomePage() {
        setVersioned(false);
        setStatelessHint(true);

    }

    @Override
    public void renderPage() {
        String lastPage = CookieUtils.getCookie(RememberPageBehavior.COOKIE_NAME);
        if (StringUtils.isEmpty(lastPage)) {
            setResponsePage(BrowseRepoPage.class);
        } else {
            String serverUrl = HttpUtils.getServerUrl(WicketUtils.getHttpServletRequest());
            try {
                WicketUtils.getWebResponse().sendRedirect(serverUrl + lastPage);
            } catch (ComponentNotFoundException e) {
                setResponsePage(BrowseRepoPage.class);
            }
        }
    }
}
