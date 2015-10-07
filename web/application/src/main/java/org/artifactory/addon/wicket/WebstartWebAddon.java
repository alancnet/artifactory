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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.artifactory.addon.Addon;
import org.artifactory.common.wicket.model.sitemap.MenuNode;

/**
 * Web interface of the webstart addon.
 *
 * @author Yossi Shaul
 */
public interface WebstartWebAddon extends Addon {
    /**
     * @return KeyPair menu node for the site map.
     */
    MenuNode getKeyPairMenuNode();

    /**
     * @param wicketId       Wicket ID to provide the container with
     * @param virtualRepoKey Key of virtual repo in configuration
     * @param isCreate       True if the current virtual repo is a new one. False if currently in editing
     * @return Key pair web markup container for the virtual repo configuration.
     */
    WebMarkupContainer getKeyPairContainer(String wicketId, String virtualRepoKey, boolean isCreate);
}
