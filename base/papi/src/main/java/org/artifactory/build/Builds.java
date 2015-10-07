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

package org.artifactory.build;

import org.artifactory.common.StatusHolder;
import org.artifactory.fs.FileInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * @author Noam Y. Tenne
 */
public interface Builds {

    /**
     * @return a list of the names of all builds deployed to Artifactory
     */
    List<String> getBuildNames();

    /**
     * Retrieve builds
     *
     * @param name    Builds name
     * @param number  An optional build number - can be null to retrieve all builds
     * @param started An optional start time
     * @return
     */
    @Nonnull
    List<BuildRun> getBuilds(@Nonnull String name, @Nullable String number, @Nullable String started);

    /**
     * @param buildRun A lightweight build run
     * @return Detailed build run details
     */
    @Nullable
    DetailedBuildRun getDetailedBuild(@Nonnull BuildRun buildRun);

    /**
     * Removes the build of the given details. Build artifacts or dependencies are not removed.
     *
     * @param buildRun Build info details
     * @return Operation status holder
     */
    @Nonnull
    StatusHolder deleteBuild(@Nonnull BuildRun buildRun);

    /**
     * Locates the file info objects of all the given build's produced artifacts
     *
     * @param buildRun Build run to locate artifacts of
     * @return All found artifact file infos
     */
    @Nonnull
    Set<FileInfo> getArtifactFiles(@Nonnull BuildRun buildRun);

    /**
     * Saves or updates the given build configuration
     *
     * @param detailedBuildRun Build configuration to save
     */
    void saveBuild(@Nonnull DetailedBuildRun detailedBuildRun);
}
