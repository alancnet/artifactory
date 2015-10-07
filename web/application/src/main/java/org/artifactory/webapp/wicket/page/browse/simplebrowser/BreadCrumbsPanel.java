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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.webapp.servlet.RequestUtils;

/**
 * @author Yoav Aharoni
 */
public class BreadCrumbsPanel extends Panel {
    public BreadCrumbsPanel(String id, String path) {
        super(id);
        add(new CssClass("bread-crumbs"));

        RepeatingView items = new RepeatingView("item");
        add(items);

        String[] folders = path.split("[:/]");
        StringBuilder url = new StringBuilder(RequestUtils.getWicketServletContextUrl());
        url.append("/");
        // add the /simple request
        url.append(ArtifactoryRequest.SIMPLE_BROWSING_PATH).append("/");
        // add repo root
        String repo = folders[0];
        url.append(repo);
        url.append("/");
        items.add(new BreadCrumbItem(items.newChildId(), repo, url, ":"));

        for (int i = 1; i < folders.length; i++) {
            String folder = folders[i];
            url.append(folder);
            url.append("/");
            items.add(new BreadCrumbItem(items.newChildId(), folder, url, "/"));
        }
    }

    private static class BreadCrumbItem extends WebMarkupContainer {
        private BreadCrumbItem(String id, String label, StringBuilder href, String sep) {
            super(id);
            add(new ExternalLink("link", href.toString(), label));
            add(new Label("sep", sep).setRenderBodyOnly(true));
        }
    }
}
