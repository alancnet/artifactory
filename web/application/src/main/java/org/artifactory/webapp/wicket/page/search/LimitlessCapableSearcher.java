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

package org.artifactory.webapp.wicket.page.search;

import org.artifactory.api.search.ItemSearchResult;

import java.io.Serializable;
import java.util.List;

/**
 * An interface of a component that's able to execute the limitless search query
 *
 * @author Noam Y. Tenne
 */
public interface LimitlessCapableSearcher<T extends ItemSearchResult> extends Serializable {

    /**
     * Performs a limitless search
     *
     * @return List of search results
     */
    List<T> searchLimitlessArtifacts();
}