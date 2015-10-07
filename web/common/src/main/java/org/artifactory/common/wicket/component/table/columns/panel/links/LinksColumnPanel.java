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

package org.artifactory.common.wicket.component.table.columns.panel.links;

import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.common.wicket.component.panel.links.LinksPanel;
import org.artifactory.common.wicket.component.template.HtmlTemplate;
import org.artifactory.common.wicket.contributor.ResourcePackage;

/**
 * @author Yoav Aharoni
 */
public class LinksColumnPanel extends Panel {
    private LinksPanel linksPanel;

    public LinksColumnPanel(String id) {
        super(id);

        add(ResourcePackage.forJavaScript(LinksColumnPanel.class));

        linksPanel = new LinksPanel("links");
        linksPanel.setOutputMarkupId(true);
        add(linksPanel);

        WebComponent icon = new WebComponent("icon");
        icon.setOutputMarkupId(true);
        add(icon);

        HtmlTemplate initScript = new HtmlTemplate("initScript");
        initScript.setParameter("iconId", new PropertyModel(icon, "markupId"));
        initScript.setParameter("panelId", new PropertyModel(linksPanel, "markupId"));
        add(initScript);
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && linksPanel.hasAnyLinks();
    }

    public void addLink(AbstractLink link) {
        linksPanel.addLink(link);
    }
}
