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

package org.artifactory.search.stats;

import com.google.common.collect.Lists;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.stats.StatsSearchControls;
import org.artifactory.api.search.stats.StatsSearchResult;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.sapi.search.VfsQuery;
import org.artifactory.sapi.search.VfsQueryResult;
import org.artifactory.sapi.search.VfsQueryRow;
import org.artifactory.schedule.TaskInterruptedException;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.search.SearcherBase;

import java.util.Calendar;
import java.util.List;

import static org.artifactory.sapi.search.VfsBoolType.AND;
import static org.artifactory.sapi.search.VfsBoolType.OR;
import static org.artifactory.sapi.search.VfsComparatorType.LOWER_THAN;
import static org.artifactory.sapi.search.VfsComparatorType.NONE;
import static org.artifactory.sapi.search.VfsQueryResultType.FILE;

/**
 * @author Noam Y. Tenne
 */
public class LastDownloadedSearcher extends SearcherBase<StatsSearchControls, StatsSearchResult> {

    @Override
    public ItemSearchResults<StatsSearchResult> doSearch(StatsSearchControls controls) {
        Calendar since = controls.getDownloadedSince();
        Calendar createdBefore = since;
        if (controls.hasCreatedBefore()) {
            createdBefore = controls.getCreatedBefore();
        }

        // TODO: [by dan] reverted this until smart remote is stable again
        VfsQuery repoQuery = createQuery(controls)
                .expectedResult(FILE)
                .prop("last_downloaded").comp(LOWER_THAN).val(since).nextBool(OR)
                .startGroup()
                .prop("last_downloaded").comp(NONE).nextBool(AND)
                .prop("created").comp(LOWER_THAN).val(createdBefore)
                .endGroup(null);

        VfsQueryResult queryResult = repoQuery.execute(getLimit(controls));

        List<StatsSearchResult> results = Lists.newArrayList();
        InternalRepositoryService repoService = getRepoService();
        int iterationCount = 0;
        for (VfsQueryRow row : queryResult.getAllRows()) {
            ItemInfo item = row.getItem();
            if ((++iterationCount % 10 == 0) && TaskUtils.pauseOrBreak()) {
                throw new TaskInterruptedException();
            }
            LocalRepo localRepo = repoService.localOrCachedRepositoryByKey(item.getRepoKey());
            if (localRepo == null || !isResultAcceptable(item.getRepoPath(), localRepo)) {
                continue;
            }

            // TODO: Change the query to return the stats info
            StatsSearchResult result =
                    new StatsSearchResult(item, repoService.getStatsInfo(item.getRepoPath()));
            results.add(result);
        }
        return new ItemSearchResults<>(results, queryResult.getCount());
    }
}
