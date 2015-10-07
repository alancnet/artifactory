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

package org.artifactory.search.gavc;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.gavc.GavcSearchControls;
import org.artifactory.api.search.gavc.GavcSearchResult;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.LocalRepo;
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
 * Holds the GAVC search logic
 *
 * @author Noam Tenne
 */
public class GavcSearcher extends SearcherBase<GavcSearchControls, GavcSearchResult> {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = LoggerFactory.getLogger(GavcSearcher.class);

    @Override
    public ItemSearchResults<GavcSearchResult> doSearch(GavcSearchControls controls) {
        //Validate and escape all input values
        String groupInput = escapeGroupPath(controls.getGroupId());

        //Build search path from inputted group
        VfsQuery query = createQuery(controls)
                .expectedResult(VfsQueryResultType.FILE)
                .addPathFilter(groupInput)
                .addPathFilters(controls.getArtifactId(), controls.getVersion());

        String classifier = controls.getClassifier();
        if (!StringUtils.isBlank(classifier)) {
            query.name("*-" + classifier + "*");
        }

        int limit = getLimit(controls);
        VfsQueryResult queryResult = query.execute(limit);
        List<GavcSearchResult> results = Lists.newArrayList();
        for (VfsQueryRow row : queryResult.getAllRows()) {
            if (results.size() >= limit) {
                break;
            }
            ItemInfo item = row.getItem();
            String repoKey = item.getRepoKey();
            LocalRepo localRepo = getRepoService().localOrCachedRepositoryByKey(repoKey);
            if (localRepo == null || !isResultAcceptable(item.getRepoPath(), localRepo)) {
                continue;
            }

            ModuleInfo moduleInfo = localRepo.getItemModuleInfo(item.getRelPath());

            if (moduleInfo.isValid()) {
                results.add(new GavcSearchResult(item, moduleInfo));
            }
        }

        return new ItemSearchResults<>(results, queryResult.getCount());
    }

    /**
     * Swaps all backward-slashes ('\') to forward ones ('/'). Removes leading and trailing slashes (if any), Replaces
     * all periods ('.') to forward slashes.
     *
     * @param groupInput The inputted group path
     * @return String - Group path after escape
     */
    private String escapeGroupPath(String groupInput) {
        if (StringUtils.isBlank(groupInput)) {
            groupInput = "";
        }
        groupInput = groupInput.replace('\\', '/');
        groupInput = PathUtils.trimSlashes(groupInput).toString();
        groupInput = groupInput.replace('.', '/');
        // wildcard in the beginning means all values/as many folders begin
        if (groupInput.startsWith("*") && !groupInput.startsWith(VfsQuery.ALL_PATH_VALUE)) {
            groupInput = "*" + groupInput;
        }
        // Same for the end
        if (groupInput.endsWith("*") && !groupInput.endsWith(VfsQuery.ALL_PATH_VALUE)) {
            groupInput = groupInput + "*";
        }
        return groupInput;
    }
}