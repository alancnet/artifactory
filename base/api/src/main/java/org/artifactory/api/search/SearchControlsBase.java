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

package org.artifactory.api.search;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A base implementation for all search control classes
 *
 * @author Noam Y. Tenne
 */
public abstract class SearchControlsBase implements SearchControls {

    /**
     * Regexp to test if string contains only wildcards ('*' or '?' only).
     */
    private static Pattern wildcardsOnlyPattern = Pattern.compile("(\\*|\\?)+");

    private boolean limitSearchResults = true;
    protected List<String> selectedRepoForSearch;

    @Override
    @Nullable
    public List<String> getSelectedRepoForSearch() {
        return selectedRepoForSearch;
    }

    /**
     * Limit the search to the specified repository keys. Search in any repo if empty.
     *
     * @param selectedRepoForSearch List of repository keys to search in.
     */
    public void setSelectedRepoForSearch(List<String> selectedRepoForSearch) {
        this.selectedRepoForSearch = selectedRepoForSearch;
    }

    @Override
    public boolean isLimitSearchResults() {
        return limitSearchResults;
    }

    /**
     * Sets the search result limit indicator
     *
     * @param limitSearchResults True if the search results should be limited
     */
    public void setLimitSearchResults(boolean limitSearchResults) {
        this.limitSearchResults = limitSearchResults;
    }

    @Override
    public void resetResultLimit() {
        limitSearchResults = true;
    }

    @Override
    public boolean isSpecificRepoSearch() {
        return selectedRepoForSearch != null && !selectedRepoForSearch.isEmpty();
    }

    public void addRepoToSearch(String repoKey) {
        if (selectedRepoForSearch == null) {
            selectedRepoForSearch = Lists.newArrayList();
        }
        selectedRepoForSearch.add(repoKey);
    }

    /**
     * @param str String to check
     * @return True is the input is empty or it contains wildcards only
     */
    public boolean isWildcardsOnly(String str) {
        return StringUtils.isBlank(str) || wildcardsOnlyPattern.matcher(str).matches();
    }
}
