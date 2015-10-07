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

package org.artifactory.search.archive;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.archive.ArchiveSearchControls;
import org.artifactory.api.search.archive.ArchiveSearchResult;
import org.artifactory.common.ConstantValues;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.search.ArchiveEntryRow;
import org.artifactory.sapi.search.InvalidQueryRuntimeException;
import org.artifactory.sapi.search.VfsBoolType;
import org.artifactory.sapi.search.VfsComparatorType;
import org.artifactory.sapi.search.VfsQuery;
import org.artifactory.sapi.search.VfsQueryResult;
import org.artifactory.sapi.search.VfsQueryResultType;
import org.artifactory.sapi.search.VfsQueryRow;
import org.artifactory.search.SearcherBase;
import org.artifactory.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Noam Tenne
 */
public class ArchiveSearcher extends SearcherBase<ArchiveSearchControls, ArchiveSearchResult> {

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = LoggerFactory.getLogger(ArchiveSearcher.class);

    @Override
    public ItemSearchResults<ArchiveSearchResult> doSearch(ArchiveSearchControls controls) {
        VfsQuery query = createQuery(controls);

        String path = controls.getPath();
        if (path != null && !path.isEmpty() && !controls.isWildcardsOnly(path)) {
            if (path.contains(".") && !path.contains("/")) {
                path = path.replace(".", "/");
            }
            validateQueryLength(path);
            query.archivePath(path).nextBool(VfsBoolType.AND);
        }

        String name = controls.getName();
        if (controls.isSearchClassResourcesOnly()) {
            if (StringUtils.isEmpty(name)) {
                name = "*";
            }
            if (!name.endsWith(".class")) {
                name += ".class";
            }
        }
        if (name != null && !name.isEmpty() && !controls.isWildcardsOnly(name)) {
            validateQueryLength(name);
            query.archiveName(name);
        }

        query.expectedResult(VfsQueryResultType.ARCHIVE_ENTRY);

        if (controls.isExcludeInnerClasses()) {
            query.archiveName("*$*").comp(VfsComparatorType.NOT_CONTAINS);
        }
        int limit = getLimit(controls);
        VfsQueryResult queryResult = query.execute(limit);

        List<ArchiveSearchResult> resultList = Lists.newArrayList();
        for (VfsQueryRow row : queryResult.getAllRows()) {
            //If the search results are limited, stop when reached more than max results + 1
            if (resultList.size() < limit) {
                ItemInfo item = row.getItem();
                RepoPath repoPath = item.getRepoPath();
                if (!isResultAcceptable(repoPath)) {
                    continue;
                }

                boolean shouldCalc = controls.shouldCalcEntries();
                if (shouldCalc) {
                    Iterable<ArchiveEntryRow> archiveEntries = row.getArchiveEntries();
                    /**
                     * Handle normal archive search (needs to calculate entry paths for display and results were
                     * returned)
                     */
                    for (ArchiveEntryRow entry : archiveEntries) {
                        String entryName = entry.getEntryName();
                        if (StringUtils.isEmpty(entryName)) {
                            entryName = name;
                        }
                        resultList.add(new ArchiveSearchResult(item,
                                entryName, entry.getEntryPath() + "/" + entryName, true));
                    }
                } else {
                    /**
                     * Create generic entries when we don't need to calculate paths (performing a search for the
                     * "saved search results") or if the search query was too ambiguous (no results returned because
                     * there were too many)
                     */
                    resultList.add(new ArchiveSearchResult(item, "Empty",
                            "Entry path calculation is disabled.", false));
                }
            }
        }
        return new ItemSearchResults<>(resultList, resultList.size());
    }

    /**
     * Validates the length of the given query
     *
     * @param query Query to validate
     * @throws InvalidQueryRuntimeException If whitespace and wildcard-stripped query is less than 3 characters
     */
    private void validateQueryLength(String query) {
        String trimmedQuery = PathUtils.trimWhitespace(query);

        if (trimmedQuery.startsWith("*")) {
            trimmedQuery = trimmedQuery.substring(1);
        }
        if (trimmedQuery.endsWith("*")) {
            trimmedQuery = trimmedQuery.substring(0, trimmedQuery.length() - 1);
        }

        if (trimmedQuery.length() < ConstantValues.searchArchiveMinQueryLength.getLong()) {
            throw new InvalidQueryRuntimeException("Search term must be at least " +
                    ConstantValues.searchArchiveMinQueryLength.getString() + " characters long.");
        }
    }
}