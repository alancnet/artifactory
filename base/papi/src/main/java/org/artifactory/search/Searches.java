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

package org.artifactory.search;

import com.google.common.collect.SetMultimap;
import org.artifactory.build.BuildRun;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * Public API for executing searches
 *
 * @author Yoav Landman
 */
public interface Searches {

    /**
     * Find artifacts by name
     *
     * @param query        The search query term for the artifact name
     * @param repositories A list of repositories to search in. Can be null to search all repositories
     * @return Artifacts found by the search, empty list if nothing was found
     */
    List<RepoPath> artifactsByName(String query, String... repositories);

    /**
     * Find artifacts by properties
     *
     * @param properties   A set of key-value properties to search for on artifacts or folders. All specified properties
     *                     need to exist on found items. If multiple values are used a contains() semantic is applied
     * @param repositories A list of repositories to search in. Can be null to search all repositories.
     * @return Artifacts matching the criteria
     */
    List<RepoPath> itemsByProperties(SetMultimap<String, String> properties, String... repositories);

    /**
     * Search the content of jar/zip archives
     *
     * @param query        The search term for the archive of the entry
     * @param repositories A list of repositories to search in. Can be null to search all repositories.
     * @return List of zip resources repo path matching the criteria
     */
    List<RepoPath> archiveEntriesByName(String query, String... repositories);

    List<RepoPath> artifactsByGavc(@Nullable String groupId, @Nullable String artifactId,
            @Nullable String version, @Nullable String classifier, String... repositories);

    /**
     * Find artifacts by their checksum values
     *
     * @param sha1         The sha1 checksum of the artifact
     * @param repositories A list of repositories to search in. Can be null to search all repositories
     * @return Set of repo paths that comply with the given checksums
     */
    Set<RepoPath> artifactsBySha1(String sha1, String... repositories);

    /**
     * Find artifacts created or modified within a date range
     *
     * @param from         The time to start the search exclusive (eg, >). If empty will start from 1st Jan 1970
     * @param to           The time to end search inclusive (eg, <=), If null, will not use current time as the limit
     * @param repositories A list of repositories to search in. Can be null to search all repositories.
     * @return List of file repo paths that were created or modified between the input time range and the date the file
     *         was modified.
     */
    List<RepoPath> artifactsCreatedOrModifiedInRange(
            @Nullable Calendar from, @Nullable Calendar to, String... repositories);

    /**
     * Find artifacts not downloaded since the specified date
     *
     * @param since         The time to start the search exclusive (eg, >). If null will start from 1st Jan 1970
     * @param createdBefore Only include artifacts created before the specified time. If null will default to the value
     *                      of since.
     * @param repositories  A list of repositories to search in. Can be null to search all repositories
     * @return List of file repo paths that were not downloaded since the specified date
     */
    List<RepoPath> artifactsNotDownloadedSince(@Nullable Calendar since, @Nullable Calendar createdBefore,
            String... repositories);

    /**
     * Find all build runs that produced the artifact with the provided sha1 checksum
     *
     * @param sha1 The sha1 cheksum of the build dependency to search
     * @return A list of BuildRuns (may be empty)
     */
    Set<BuildRun> buildsByArtifactSha1(String sha1);

    /**
     * Find all build runs that used a dependency with the provided sha1 checksum
     *
     * @param sha1 The sha1 cheksum of the build dependency to search
     * @return A list of BuildRuns (may be empty)
     */
    Set<BuildRun> buildsByDependencySha1(String sha1);
}
