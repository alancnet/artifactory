/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.api.repo.cleanup;


import org.artifactory.api.common.BasicStatusHolder;

import javax.annotation.Nullable;

/**
 * Interface for the VirtualCacheCleanupService
 *
 * @author Yoav Luft
 */
public interface VirtualCacheCleanupService {

    /**
     * Manually schedule cleanup of virtual cache.
     *
     * @param multiStatusHolder Status holder that will hold the results
     * @return task ID of the clean up task or null if failed to start.
     */
    @Nullable
    String callVirtualCacheCleanup(BasicStatusHolder multiStatusHolder);
}
