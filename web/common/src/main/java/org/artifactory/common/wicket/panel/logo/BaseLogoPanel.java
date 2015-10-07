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

package org.artifactory.common.wicket.panel.logo;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.image.ExternalImage;

/**
 * @author Tomer Cohen
 */
public abstract class BaseLogoPanel extends Panel {
    @WicketProperty
    private String logoUrl;

    public BaseLogoPanel(String id) {
        super(id);
        setOutputMarkupId(true);
        final Class<? extends Page> pageClass = getLinkPage();
        MarkupContainer link = newLink(pageClass);
        link.add(new ExternalImage("logoImage", new PropertyModel(this, "logoUrl")));
        link.add(new CssClass(new CssModel()));
        add(link);

        add(new CssClass("app-logo"));
    }

    @Override
    protected void onBeforeRender() {
        logoUrl = getLogoUrl();
        super.onBeforeRender();
    }

    protected MarkupContainer newLink(Class<? extends Page> pageClass) {
        if (pageClass == null) {
            return new WebMarkupContainer("homeLink");
        }
        return new BookmarkablePageLink<>("homeLink", pageClass);
    }

    protected abstract Class<? extends Page> getLinkPage();

    protected abstract String getLogoUrl();

    private class CssModel extends AbstractReadOnlyModel {
        @Override
        public Object getObject() {
            return StringUtils.isEmpty(logoUrl) ? " artifactory-logo" : "company-logo";
        }
    }
}