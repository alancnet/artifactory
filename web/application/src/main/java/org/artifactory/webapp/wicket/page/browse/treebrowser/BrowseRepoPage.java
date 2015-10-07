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

package org.artifactory.webapp.wicket.page.browse.treebrowser;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.actionable.CannonicalEnabledActionableFolder;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.artifactory.webapp.wicket.page.browse.home.RememberPageBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class BrowseRepoPage extends AuthenticatedPage implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(BrowseRepoPage.class);
    private static final String WEBAPP_URL_BROWSE_REPO = "/browserepo.html";

    @SpringBean
    private RepositoryService repositoryService;

    private String lastTabName;
    public static final String PATH_ID_PARAM = "pathId";

    public BrowseRepoPage() {
        add(new RememberPageBehavior());

        //Using request parameters instead of wicket's page parameters. See RTFACT-2843
        RepoPath repoPath = null;
        String pathId = WicketUtils.getParameter(PATH_ID_PARAM);
        if (StringUtils.isNotBlank(pathId)) {
            try {
                repoPath = InternalRepoPathFactory.fromId(pathId);
                if (!repositoryService.exists(repoPath)) {
                    repoPath = null;
                }
            } catch (Exception e) {
                error("Unable to find path " + pathId);
            }
        }
        displayBrowser(repoPath);
    }

    public BrowseRepoPage(RepoPath repoPath) {
        displayBrowser(repoPath);
    }

    @Override
    public String getPageName() {
        return "Repository Browser";
    }

    public String getLastTabName() {
        return lastTabName;
    }

    public void setLastTabName(String lastTabName) {
        this.lastTabName = lastTabName;
    }

    private void displayBrowser(RepoPath repoPath) {
        add(new BrowseRepoPanel("browseRepoPanel", new DefaultRepoTreeSelection(repoPath)));

        WebMarkupContainer scrollScript = new WebMarkupContainer("scrollScript");
        scrollScript.setVisible(repoPath != null);
        add(scrollScript);
    }

    /**
     * Builds an external url that points to resource inside the tree. Once used, the tree browser will open and select
     * the node represented by the repository item.
     * Utilizes the Wicket utils and request cycle.
     *
     * @param repoItem The repository item
     * @return External URL linking directly to the item in the tree browser
     */
    public static String getWicketDependableRepoPathUrl(RepoAwareActionableItem repoItem) {
        String artifactPath;
        if (repoItem instanceof CannonicalEnabledActionableFolder) {
            artifactPath = ((CannonicalEnabledActionableFolder) repoItem).getCanonicalPath().getPath();
        } else {
            artifactPath = repoItem.getRepoPath().getPath();
        }

        StringBuilder urlBuilder = new StringBuilder();
        if (NamingUtils.isChecksum(artifactPath)) {
            // checksums are not displayed in the tree, link to the target file
            artifactPath = PathUtils.stripExtension(artifactPath);
        }
        String repoPathId = InternalRepoPathFactory.create(repoItem.getRepo().getKey(), artifactPath).getId();

        String encodedPathId = null;
        try {
            encodedPathId = URLEncoder.encode(repoPathId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // NO-OP - we should never encounter an error, but still...
            throw new RuntimeException("UTF-8 encoding not supported?", e);
        }

        //Using request parameters instead of wicket's page parameters. See RTFACT-2843
        urlBuilder.append(WicketUtils.absoluteMountPathForPage(BrowseRepoPage.class)).append("?").
                append(PATH_ID_PARAM).append("=").append(encodedPathId);
        return urlBuilder.toString();
    }

    /**
     * Creates an HTML link to the given repo path ID in the tree browser
     *
     * @param artifactoryUrl URL to Artifactory (excluding context)
     * @param repoPathId     Repo path ID to link to
     * @return HTML link
     */
    public static String createLinkToBrowsableArtifact(String artifactoryUrl, String repoPathId) {
        return createLinkToBrowsableArtifact(artifactoryUrl, repoPathId, repoPathId);
    }

    /**
     * Creates an HTML link to the given repo path ID in the tree browser
     *
     * @param artifactoryUrl URL to Artifactory (excluding context)
     * @param repoPathId     Repo path ID to link to
     * @param linkLabel      Link label
     * @return HTML link
     */
    public static String createLinkToBrowsableArtifact(String artifactoryUrl, String repoPathId, String linkLabel) {
        String encodedPathId = HttpUtils.encodeQuery(repoPathId);
        String url = new StringBuilder().append(artifactoryUrl).append(HttpUtils.WEBAPP_URL_PATH_PREFIX)
                .append(WEBAPP_URL_BROWSE_REPO).append("?").append(PATH_ID_PARAM).append("=").append(encodedPathId)
                .toString();

        StringBuilder builder = new StringBuilder();
        builder.append("<a href=").append(url).append(" target=\"blank\"").append(">")
                .append(linkLabel).append("</a>");
        return builder.toString();
    }
}