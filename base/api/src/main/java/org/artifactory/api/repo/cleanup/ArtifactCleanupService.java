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

package org.artifactory.api.repo.cleanup;


import org.artifactory.api.common.BasicStatusHolder;

import javax.annotation.Nullable;

/**
 * The main interface of the clean-up service
 *
 * @author Noam Tenne
 */
public interface ArtifactCleanupService {

    /**
     * Induce the artifact cleanup manually, and wait for the cleanup to start running.
     *
     * @param statusHolder A status holder for the results.
     * @return The task id or null if failed to start the task
     */
    @Nullable
    String callManualArtifactCleanup(BasicStatusHolder statusHolder);
}
