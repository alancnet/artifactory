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

package org.artifactory.webapp.wicket.page.browse.simplebrowser.root;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.webapp.servlet.RequestUtils;
import org.artifactory.webapp.wicket.util.ItemCssClass;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Yoav Landman
 */
public class RepoListPanel extends TitledPanel {
    private static final long serialVersionUID = 1L;


    public RepoListPanel(String id, List<? extends RepoBaseDescriptor> repoDescriptorList) {
        super(id);
        Collections.sort(repoDescriptorList, new RepoComparator());
        String hrefPrefix = RequestUtils.getWicketServletContextUrl();
        if (!hrefPrefix.endsWith("/")) {
            hrefPrefix += "/";
        }
        final String contextUrl = hrefPrefix;
        add(new ListView<RepoBaseDescriptor>("repos", repoDescriptorList) {
            @Override
            protected void populateItem(ListItem<RepoBaseDescriptor> item) {
                RepoBaseDescriptor repo = item.getModelObject();
                String key = repo.getKey();

                Component browseLink = new ExternalLink("link",
                        contextUrl + ArtifactoryRequest.SIMPLE_BROWSING_PATH + "/" + key + "/", key);
                String cssClass = ItemCssClass.getRepoDescriptorCssClass(repo);
                browseLink.add(new CssClass(cssClass));
                item.add(browseLink);

                final ExternalLink listLink = new ExternalLink("listLink", contextUrl + "list/" + key + "/", " ");
                listLink.add(new AttributeModifier("title", "Directory Listing for " + key));
                listLink.add(new CssClass("ext-link"));
                item.add(listLink);
            }
        });
    }

    @Override
    public String getTitle() {
        return getString(getId(), null);
    }

    private static class RepoComparator implements Comparator<RepoBaseDescriptor> {
        @Override
        public int compare(RepoBaseDescriptor descriptor1, RepoBaseDescriptor descriptor2) {

            //Local repositories can be either ordinary or caches
            if (descriptor1 instanceof LocalRepoDescriptor) {
                boolean repo1IsCache = ((LocalRepoDescriptor) descriptor1).isCache();
                boolean repo2IsCache = ((LocalRepoDescriptor) descriptor2).isCache();

                //Cache repositories should appear in a higher priority
                if (repo1IsCache && !repo2IsCache) {
                    return 1;
                } else if (!repo1IsCache && repo2IsCache) {
                    return -1;
                }
            }
            return descriptor1.getKey().compareTo(descriptor2.getKey());
        }
    }
}