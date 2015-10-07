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
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.behavior.tooltip.TooltipBehavior;

/**
 * Base class that adds a globe icon to those paths that have not yet been cached locally with the message {@link
 * #TOOLTIP_MESSAGE}
 *
 * @author Tomer Cohen
 */
public abstract class RemoteBrowsableRepoPanel extends BaseRepoBrowserPanel {

    private static final String GLOBE_ID = "globe";
    private static final String TOOLTIP_MESSAGE = "This item is available remotely and has not yet been cached locally";

    public RemoteBrowsableRepoPanel(String id) {
        super(id);
    }

    /**
     * Add the globe icon to a markup container, with a flag that controls whether the icon should be visible or not.
     *
     * @param listItem The markup container to attach the globe to.
     * @param visible  A flag that controls the visibility of the icon.
     */
    protected void addGlobeIcon(WebMarkupContainer listItem, boolean visible) {
        WebMarkupContainer globe = getGlobe();
        listItem.add(globe);
        if (!visible) {
            globe.setVisible(false);
        }
    }

    private WebMarkupContainer getGlobe() {
        WebMarkupContainer globe = new WebMarkupContainer(GLOBE_ID);
        globe.add(new TooltipBehavior(Model.of(TOOLTIP_MESSAGE)));
        return globe;
    }
}
