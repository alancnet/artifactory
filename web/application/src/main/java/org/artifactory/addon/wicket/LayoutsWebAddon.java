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

package org.artifactory.addon.wicket;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.webapp.wicket.page.config.layout.LayoutListPanel;

import java.util.List;

/**
 * The web interface of the repository layouts
 *
 * @author Noam Y. Tenne
 */
public interface LayoutsWebAddon extends Addon {

    /**
     * Adds the repository layout copy link
     *
     * @param links           Link list to add to
     * @param layoutToCopy    Repo layout to copy
     * @param linkId          Link ID
     * @param linkTitle       Link title
     * @param layoutListPanel Layout list panel instance
     */
    void addLayoutCopyLink(List<AbstractLink> links, RepoLayout layoutToCopy, String linkId, String linkTitle,
            LayoutListPanel layoutListPanel);

    /**
     * Returns the new repository layout link
     *
     * @param linkId          Link ID
     * @param linkTitle       Link title
     * @param layoutListPanel Layout list panel instance
     * @return New layout link
     */
    AbstractLink getNewLayoutItemLink(String linkId, String linkTitle, LayoutListPanel layoutListPanel);
}
