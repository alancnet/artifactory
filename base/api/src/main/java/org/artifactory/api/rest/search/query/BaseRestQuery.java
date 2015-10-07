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

package org.artifactory.api.rest.search.query;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

/**
 * REST API search query base class
 *
 * @author Noam Y. Tenne
 */
public abstract class BaseRestQuery implements Serializable {

    private List<String> reposToSearch = Lists.newArrayList();

    /**
     * Default constructor
     */
    protected BaseRestQuery() {
    }

    /**
     * Returns the list of repositories to search in
     *
     * @return List of repositories to search in
     */
    public List<String> getReposToSearch() {
        return reposToSearch;
    }

    /**
     * Sets the list of repositories to search in
     *
     * @param reposToSearch List of repositories to search in
     */
    public void setReposToSearch(List<String> reposToSearch) {
        this.reposToSearch = reposToSearch;
    }

    /**
     * Adds a key of a repository to search in
     *
     * @param repoKey Key of repository to search in
     */
    public void addRepoToSearch(String repoKey) {
        reposToSearch.add(repoKey);
    }
}