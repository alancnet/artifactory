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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.artifact.ArtifactSearchControls;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.search.artifact.ChecksumSearchControls;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.search.VfsQuery;
import org.artifactory.sapi.search.VfsQueryResult;
import org.artifactory.sapi.search.VfsQueryResultType;
import org.artifactory.sapi.search.VfsQueryRow;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * User: freds Date: Jul 27, 2008 Time: 6:04:39 PM
 */
public class ArtifactSearcher extends SearcherBase<ArtifactSearchControls, ArtifactSearchResult> {

    @Override
    public ItemSearchResults<ArtifactSearchResult> doSearch(ArtifactSearchControls controls) {
        String providedQuery = controls.getQuery();
        String relativePath = controls.getRelativePath();
        List<ArtifactSearchResult> results = Lists.newArrayList();
        int limit = getLimit(controls);

        VfsQuery query = createQuery(controls);
        if (StringUtils.isNotBlank(relativePath)) {
            query.addPathFilter(relativePath);
        }
        query.expectedResult(VfsQueryResultType.FILE)
                .prop("node_name").val(providedQuery);
        VfsQueryResult queryResult = query.execute(limit);

        for (VfsQueryRow row : queryResult.getAllRows()) {
            if (limit > 0 && results.size() >= limit) {
                break;
            }
            ItemInfo item = row.getItem();
            if (isResultAcceptable(item.getRepoPath())) {
                results.add(new ArtifactSearchResult(item));
            }
        }
        return new ItemSearchResults<>(results, queryResult.getCount());
    }

    /**
     * Searches for artifacts by their checksum values
     *
     * @param searchControls Search controls
     * @return Set of repo paths that comply with the given checksums
     */
    public Collection<ItemInfo> searchArtifactsByChecksum(ChecksumSearchControls searchControls) {
        Map<RepoPath, ItemInfo> results = Maps.newHashMap();

        EnumMap<ChecksumType, String> checksums = searchControls.getChecksums();
        for (Map.Entry<ChecksumType, String> checksumEntry : checksums.entrySet()) {
            if (results.isEmpty() && StringUtils.isNotBlank(checksumEntry.getValue())) {
                findArtifactsByChecksum(checksumEntry.getKey(), checksumEntry.getValue(), searchControls, results);
            }
        }
        return results.values();
    }

    /**
     * Locates artifacts by the given checksum value and adds them to the given list
     *
     * @param checksumType           Checksum type (sha1, md5) to search for
     * @param checksumValue          Checksum value to match
     * @param checksumSearchControls controls
     * @param results                Set of items to append the results to
     */
    private void findArtifactsByChecksum(ChecksumType checksumType, String checksumValue,
            ChecksumSearchControls checksumSearchControls,
            Map<RepoPath, ItemInfo> results) {
        VfsQuery query = createQuery(checksumSearchControls);
        query.expectedResult(VfsQueryResultType.FILE);
        query.prop(checksumType.name() + "_actual").val(checksumValue);
        VfsQueryResult queryResult = query.execute(Integer.MAX_VALUE);
        for (VfsQueryRow row : queryResult.getAllRows()) {
            ItemInfo item = row.getItem();
            LocalRepo localRepo = getRepoService().localOrCachedRepositoryByKey(item.getRepoKey());
            if (localRepo == null) {
                // Some left over in DB or the node is in a virtual repo
                continue;
            }
            results.put(item.getRepoPath(), item);
        }
    }
}
