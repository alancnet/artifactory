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

package org.artifactory.storage.tx;

import java.util.List;

/**
 * @author freds
 * @date Sep 22, 2008
 */
public interface SessionResourceManager extends SessionResource {

    /**
     * Gets or creates by reflection a new session attached managed resource.
     *
     * @param resourceClass The class of the resource to create
     */
    <T extends SessionResource> T getOrCreateResource(Class<T> resourceClass);

    /**
     * @return A list of {@link SessionResource} with pending job. Useful for debugging.
     */
    List<SessionResource> pendingResources();
}
