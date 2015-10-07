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

package org.artifactory.request;

import org.artifactory.md.Properties;

/**
 * Request context that might be passed to the repository when requesting for info.
 *
 * @author Yossi Shaul
 */
public interface RequestContext {
    boolean isFromAnotherArtifactory();

    String getResourcePath();

    String getServletContextUrl();

    Properties getProperties();

    /**
     * Return the client request. This might be null when doing internal requests.
     *
     * @return The request or null if not set.
     */
    Request getRequest();

    void setAttribute(String name, String value);

    String getAttribute(String name);
}
