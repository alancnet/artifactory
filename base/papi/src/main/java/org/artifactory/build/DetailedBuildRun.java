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

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A detailed build run info holder
 *
 * @author Noam Y. Tenne
 */
public interface DetailedBuildRun extends BuildRun {
    String getBuildAgent();

    String getAgent();

    long getDurationMillis();

    String getPrincipal();

    String getArtifactoryPrincipal();

    String getUrl();

    String getParentName();

    String getParentNumber();

    String getVcsRevision();

    @Nonnull
    List<Module> getModules();

    /**
     * Copies a build
     *
     * @param buildNumber
     * @return
     */
    @Nonnull
    DetailedBuildRun copy();

    /**
     * Copies a build with a new build number
     *
     * @param buildNumber
     * @return
     */
    @Nonnull
    DetailedBuildRun copy(String buildNumber);

    List<ReleaseStatus> getReleaseStatuses();
}
