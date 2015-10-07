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

import org.artifactory.addon.Addon;
import org.artifactory.common.wicket.model.sitemap.MenuNode;

/**
 * Addon for HTTP SSO capabilities.
 *
 * @author Noam Tenne
 */
public interface HttpSsoAddon extends Addon {

    String DEFAULT_REQUEST_VARIABLE = "REMOTE_USER";

    String REALM = "http-sso";

    /**
     * Returns the HTTP SSO site map builder menu node
     *
     * @param nodeName Name to give to menu node
     * @return Real menu node if addon is enabled. Disabled if not
     */
    MenuNode getHttpSsoMenuNode(String nodeName);


}
