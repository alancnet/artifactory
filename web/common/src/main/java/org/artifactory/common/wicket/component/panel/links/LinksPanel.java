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

package org.artifactory.common.wicket.component.panel.links;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.ResourceModel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;

/**
 * @author Yoav Aharoni
 */
public class LinksPanel extends Panel {
    private RepeatingView repeatingView;
    public static final String LINK_ID = "link";

    public LinksPanel(String id) {
        super(id);

        repeatingView = new RepeatingView("item");
        add(repeatingView);
    }

    public LinksPanel addLink(AbstractLink link) {
        WebMarkupContainer item = new WebMarkupContainer(repeatingView.newChildId());
        repeatingView.add(item);
        item.add(link);

        return this;
    }

    public boolean hasAnyLinks() {
        return repeatingView.iterator().hasNext();
    }

    public LinksPanel addLinkFor(final Class<? extends Page> pageClass) {
        WebMarkupContainer item = new WebMarkupContainer(repeatingView.newChildId());
        repeatingView.add(item);

        String className = pageClass.getSimpleName();
        ResourceModel linkTitleModel = new ResourceModel(className, className);
        AbstractLink link = new TitledAjaxLink(LINK_ID, linkTitleModel) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(pageClass);
            }
        };
        item.add(link);

        return this;
    }

    public LinksPanel addSeperator() {
        WebMarkupContainer item = new WebMarkupContainer(repeatingView.newChildId());
        item.add(new WebMarkupContainer(LINK_ID).setVisible(false));
        item.add(new CssClass("sep"));
        repeatingView.add(item);

        return this;
    }
}
