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

package org.artifactory.search.property;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.property.PropertySearchControls;
import org.artifactory.api.search.property.PropertySearchResult;
import org.artifactory.fs.ItemInfo;
import org.artifactory.sapi.search.VfsComparatorType;
import org.artifactory.sapi.search.VfsQuery;
import org.artifactory.sapi.search.VfsQueryResult;
import org.artifactory.sapi.search.VfsQueryResultType;
import org.artifactory.sapi.search.VfsQueryRow;
import org.artifactory.search.SearcherBase;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Noam Tenne
 */
public class PropertySearcher extends SearcherBase<PropertySearchControls, PropertySearchResult> {

    @Override
    public ItemSearchResults<PropertySearchResult> doSearch(PropertySearchControls controls) {
        LinkedHashSet<PropertySearchResult> globalResults = Sets.newLinkedHashSet();

        // TODO: [by FSI] Create a real full DB query aggregating properties conditions
        // Get all open property keys and search through them
        Set<String> openPropertyKeys = controls.getPropertyKeysByOpenness(PropertySearchControls.OPEN);
        long totalResultCount = executeOpenPropSearch(controls, openPropertyKeys, globalResults);

        // Get all closed property keys and search through them
        Set<String> closedPropertyKeys = controls.getPropertyKeysByOpenness(PropertySearchControls.CLOSED);
        totalResultCount += executeClosedPropSearch(controls, closedPropertyKeys, globalResults);

        //Return global results list
        return new ItemSearchResults<>(new ArrayList<>(globalResults), totalResultCount);
    }

    /**
     * Searches and aggregates results of open properties
     *
     * @param openPropertyKeys Keys to search through
     */
    private long executeOpenPropSearch(PropertySearchControls controls,
            Set<String> openPropertyKeys,
            Set<PropertySearchResult> globalResults) {
        if (openPropertyKeys.isEmpty()) {
            return 0;
        }
        VfsQuery repoQuery = createQuery(controls).expectedResult(VfsQueryResultType.ANY_ITEM);
        for (String key : openPropertyKeys) {
            Set<String> values = controls.get(key);
            for (String value : values) {
                repoQuery.prop(key).val(value);
            }
        }
        VfsQueryResult queryResult = repoQuery.execute(getLimit(controls));
        return processResults(controls, queryResult, globalResults);
    }

    /**
     * Searches and aggregates results of closed properties
     *
     * @param closedPropertyKeys Keys to search through
     */
    @SuppressWarnings({"WhileLoopReplaceableByForEach"})
    private long executeClosedPropSearch(PropertySearchControls controls,
            Set<String> closedPropertyKeys,
            Set<PropertySearchResult> globalResults) {
        if (closedPropertyKeys.isEmpty()) {
            return 0;
        }
        VfsQuery repoQuery = createQuery(controls).expectedResult(VfsQueryResultType.ANY_ITEM);
        // TODO: Should support any boolean
        for (String key : closedPropertyKeys) {
            Set<String> values = controls.get(key);
            for (String value : values) {
                repoQuery.prop(key).comp(VfsComparatorType.EQUAL).val(value);
            }
        }
        VfsQueryResult queryResult = repoQuery.execute(getLimit(controls));
        return processResults(controls, queryResult, globalResults);
    }

    /**
     * Processes, filters and aggregates query results into the global results list The filtering creates an AND like
     * action. The first batch of results for the session automatically gets put in the global results list. Any batch
     * of results after that is compared with the global list. If the new batch of results contains a result that does
     * not exist in the global list, we discard it (Means the result does not fall under both searches, thus failing the
     * AND requirement
     *
     * @param queryResult Result object
     */
    private long processResults(PropertySearchControls controls, VfsQueryResult queryResult,
            Set<PropertySearchResult> globalResults) {
        /**
         * If the global results is empty (either first query made, or there were no results from queries executed up
         * until now
         */
        boolean noGlobalResults = globalResults.isEmpty();
        int limit = getLimit(controls);

        long resultCount = 0L;
        List<PropertySearchResult> currentSearchResults = Lists.newArrayList();
        for (VfsQueryRow row : queryResult.getAllRows()) {
            if (globalResults.size() >= limit) {
                break;
            }
            ItemInfo item = row.getItem();
            if (!isResultAcceptable(item.getRepoPath())) {
                continue;
            }

            PropertySearchResult searchResult = new PropertySearchResult(item);

            //Make sure that we don't get any double results
            if (!currentSearchResults.contains(searchResult)) {
                resultCount++;
                currentSearchResults.add(searchResult);
            }
        }

        /**
         * If the global results list is empty, simply add all our results to set it as a comparison standard for the
         * next set of results
         */
        if (noGlobalResults) {
            globalResults.addAll(currentSearchResults);
        } else {
            //Create a copy of the global results so we can iterate and remove at the same time
            ArrayList<PropertySearchResult> globalCopy = new ArrayList<>(globalResults);
            for (PropertySearchResult globalResult : globalCopy) {
                //If the received results do not exist in the global results, discard them
                if (!currentSearchResults.contains(globalResult)) {
                    globalResults.remove(globalResult);
                    resultCount--;
                }
            }
        }
        return resultCount;
    }
}