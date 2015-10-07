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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.repo.BrowsableItem;
import org.artifactory.api.repo.BrowsableItemCriteria;
import org.artifactory.api.repo.RemoteBrowsableItem;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.webapp.servlet.RequestUtils;
import org.artifactory.webapp.wicket.page.browse.BrowseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * @author Yoav Landman
 */
public class RemoteRepoBrowserPanel extends RemoteBrowsableRepoPanel {
    private static final Logger log = LoggerFactory.getLogger(RemoteRepoBrowserPanel.class);

    @SpringBean
    private RepositoryBrowsingService repoBrowseService;

    @SpringBean
    private CentralConfigService centralConfigService;

    public RemoteRepoBrowserPanel(String id, final RepoPath repoPath, Properties requestProps) {
        super(id);

        add(new BreadCrumbsPanel("breadCrumbs", repoPath.getId()));
        List<BaseBrowsableItem> remoteChildren;
        try {
            BrowsableItemCriteria.Builder builder = new BrowsableItemCriteria.Builder(repoPath).requestProperties(
                    requestProps);
            boolean includeChecksums = !ConstantValues.uiHideChecksums.getBoolean();
            builder.includeChecksums(includeChecksums);
            BrowsableItemCriteria criteria = builder.build();
            remoteChildren = repoBrowseService.getRemoteRepoBrowsableChildren(criteria);
            if (!includeChecksums) {
                remoteChildren = BrowseUtils.filterChecksums(remoteChildren);
            }
        } catch (Exception e) {
            log.debug("Exception occurred while trying to get browsable children for repo path " + repoPath, e);
            throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
        remoteChildren.add(0, getPseudoUpLink(repoPath));
        final String hrefPrefix = RequestUtils.getWicketServletContextUrl();
        add(new ListView<BaseBrowsableItem>("items", remoteChildren) {

            @Override
            protected void populateItem(ListItem<BaseBrowsableItem> listItem) {
                BaseBrowsableItem browsableItem = listItem.getModelObject();
                AbstractLink link;
                if (isEmpty(browsableItem.getRepoKey())) {
                    link = createRootLink();
                } else if (browsableItem.isRemote() &&
                        ((RemoteBrowsableItem) browsableItem).getEffectiveUrl() != null) {
                    String href = ((RemoteBrowsableItem) browsableItem).getEffectiveUrl();
                    link = new ExternalLink("link", href, browsableItem.getName());
                } else {
                    String itemRelativePath = browsableItem.getRelativePath();
                    String name = browsableItem.getName();
                    if (browsableItem.isFolder() && StringUtils.isNotBlank(itemRelativePath)) {
                        itemRelativePath += "/";
                        name += name.equals(BrowsableItem.UP) ? "" : "/";
                    }
                    String href = hrefPrefix + "/" + ArtifactoryRequest.SIMPLE_BROWSING_PATH + "/" +
                            repoPath.getRepoKey() + "/" + itemRelativePath;
                    link = new ExternalLink("link", href, name);
                }
                link.add(new CssClass(getCssClass(browsableItem)));
                addGlobeIcon(listItem, browsableItem.isRemote());
                listItem.add(link);
            }
        });
    }
}