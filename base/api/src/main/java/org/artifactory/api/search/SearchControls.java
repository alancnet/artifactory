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

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

public interface SearchControls extends Serializable {
    /**
     * @return true if the search controls are considered "empty" so that a search should not even be attempted.
     */
    boolean isEmpty();

    /**
     * @return true if the search controls contains wildcards only. User search should be attempted.
     */
    boolean isWildcardsOnly();

    /**
     * Indicates if the search results should be limited as in the system spec
     *
     * @return True if the search results should be limited
     */
    boolean isLimitSearchResults();

    /**
     * Resets the result limit indicator to it's default - true
     */
    void resetResultLimit();

    /**
     * Returns list of repository keys to search in. Search in any repo if null or empty.
     */
    @Nullable
    List<String> getSelectedRepoForSearch();

    /**
     * @return True if the search is limited to specific repositories.
     */
    boolean isSpecificRepoSearch();
}