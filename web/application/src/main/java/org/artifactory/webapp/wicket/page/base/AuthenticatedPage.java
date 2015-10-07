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

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.panel.feedback.FeedbackMessagesPanel;
import org.artifactory.common.wicket.component.panel.sidemenu.SubMenuPanel;
import org.artifactory.common.wicket.model.sitemap.MenuNode;
import org.artifactory.common.wicket.model.sitemap.SiteMap;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;

import java.util.Collections;
import java.util.List;

@AuthorizeInstantiation(AuthorizationService.ROLE_USER)
public abstract class AuthenticatedPage extends BasePage {

    protected AuthenticatedPage() {
        add(new Label("title", getPageName()));
        add(new SubMenuPanel("sideMenuPanel", getSecondLevelMenuNodes(), getMenuPageClass()));
    }

    @Override
    protected FeedbackMessagesPanel newFeedbackPanel(String id) {
        FeedbackMessagesPanel panel = super.newFeedbackPanel(id);
        panel.add(new CssClass("default-feedback"));
        return panel;
    }

    protected List<MenuNode> getSecondLevelMenuNodes() {
        SiteMap siteMap = ArtifactoryApplication.get().getSiteMap();
        MenuNode pageNode = siteMap.getPageNode(getMenuPageClass());
        if (pageNode == null) {
            return Collections.emptyList();
        }
        MenuNode current = pageNode;
        while (!current.getParent().equals(siteMap.getRoot())) {
            current = current.getParent();
        }
        return current.getChildren();
    }
}

