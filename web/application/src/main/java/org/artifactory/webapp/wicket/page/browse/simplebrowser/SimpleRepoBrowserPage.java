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

package org.artifactory.webapp.wicket.page.browse.simplebrowser;

import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.servlet.RepoFilter;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.artifactory.webapp.wicket.page.base.BasePage;
import org.artifactory.webapp.wicket.page.browse.simplebrowser.root.SimpleBrowserRootPage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleRepoBrowserPage extends AuthenticatedPage {
    public static final String PATH = "_repoBrowser";

    @SpringBean
    private AuthorizationService authorizationService;

    @SpringBean
    private RepositoryService repoService;

    public SimpleRepoBrowserPage() {
        setStatelessHint(false);
        setVersioned(false);

        //Retrieve the repository path from the request
        HttpServletRequest httpRequest = WicketUtils.getHttpServletRequest();
        RepoPath repoPath = (RepoPath) httpRequest.getAttribute(RepoFilter.ATTR_ARTIFACTORY_REPOSITORY_PATH);
        if (repoPath == null) {
            //Happens on refresh after login redirection - return a 404
            throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND);
        }

        String repoKey = repoPath.getRepoKey();
        RemoteRepoDescriptor remoteRepoDescriptor = repoService.remoteRepoDescriptorByKey(repoKey);
        if (authorizationService.isAdmin() && (remoteRepoDescriptor != null) &&
                !remoteRepoDescriptor.isListRemoteFolderItems()) {
            warn("Remote content browsing is disabled for this repository." +
                    "\n You can turn on remote browsing by enabling the 'List Remote Folder Items' flag for this repository.");
        }

        Properties requestProps = (Properties) httpRequest.getAttribute(RepoFilter.ATTR_ARTIFACTORY_REQUEST_PROPERTIES);
        if (repoService.remoteRepoDescriptorByKey(repoKey) != null) {
            add(new RemoteRepoBrowserPanel("browseRepoPanel", repoPath, requestProps));
        } else if (repoService.virtualRepoDescriptorByKey(repoKey) != null) {
            add(new VirtualRepoBrowserPanel("browseRepoPanel", repoPath, requestProps));
        } else if (repoService.localOrCachedRepoDescriptorByKey(repoKey) != null) {
            add(new LocalRepoBrowserPanel("browseRepoPanel", repoPath, requestProps));
        } else {
            throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected Class<? extends BasePage> getMenuPageClass() {
        return SimpleBrowserRootPage.class;
    }

    @Override
    public String getPageName() {
        return "Repository Browser";
    }
}