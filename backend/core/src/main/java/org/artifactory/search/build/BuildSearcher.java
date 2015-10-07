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

package org.artifactory.search.build;

import com.google.common.collect.Sets;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchControls;
import org.artifactory.build.BuildRun;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.search.SearcherBase;
import org.artifactory.storage.build.service.BuildSearchCriteria;
import org.artifactory.storage.build.service.BuildStoreService;

import java.util.Set;

/**
 * Holds the build search logic
 *
 * @author Noam Y. Tenne
 */
public class BuildSearcher extends SearcherBase {

    /**
     * Returns a set of build concentrated by name and latest date
     *
     * @return Set of latest builds by name
     * @throws RepositoryException Any exception that might occur while executing the query
     */
    public Set<BuildRun> getLatestBuildsByName() throws Exception {
        BuildStoreService buildStoreService = ContextHelper.get().beanForType(BuildStoreService.class);
        return buildStoreService.getLatestBuildsByName();
    }

    /**
     * Locates builds with deployed artifacts that have the given checksum
     *
     * @param sha1 SHA1 checksum to search for. Can be blank.
     * @param md5  MD5 checksum to search for. Can be blank.
     * @return List of basic build infos that deployed at least one artifact with the given checksum
     */
    public Set<BuildRun> findBuildsByArtifactChecksum(String sha1, String md5) {
        return findBuildsByItemChecksum(BuildSearchCriteria.IN_ARTIFACTS, sha1, md5);
    }

    /**
     * Locates builds with dependencies that have the given checksum
     *
     * @param sha1 SHA1 checksum to search for. Can be blank.
     * @param md5  MD5 checksum to search for. Can be blank.
     * @return List of basic build infos that depend on the artifact with the given checksum
     */
    public Set<BuildRun> findBuildsByDependencyChecksum(String sha1, String md5) {
        return findBuildsByItemChecksum(BuildSearchCriteria.IN_DEPENDENCIES, sha1, md5);
    }

    /**
     * DO NOT USE - NOT IMPLEMENTED
     */
    @Override
    public ItemSearchResults doSearch(SearchControls controls) {
        return null;
    }

    /**
     * Locates builds that produced or depended on an item with the given checksum and adds them to the given list
     *
     * @param criteria Where the checksum should be search for (dependencies or artifacts)
     * @param sha1     SHA1 checksum value
     * @param md5      MD5 checksum value
     */
    private Set<BuildRun> findBuildsByItemChecksum(BuildSearchCriteria criteria, String sha1, String md5) {
        Set<BuildRun> results = Sets.newHashSet();
        boolean validSha1 = ChecksumType.sha1.isValid(sha1);
        boolean validMd5 = ChecksumType.md5.isValid(md5);

        if (!validSha1 && !validMd5) {
            return results;
        }
        BuildStoreService buildStoreService = ContextHelper.get().beanForType(BuildStoreService.class);
        if (validSha1) {
            results.addAll(buildStoreService.findBuildsForChecksum(criteria, ChecksumType.sha1, sha1));
        }
        if (validMd5) {
            results.addAll(buildStoreService.findBuildsForChecksum(criteria, ChecksumType.md5, md5));
        }
        return results;
    }
}