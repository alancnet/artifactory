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

package org.artifactory.repo;

/**
 * An utility object that caches a repoPath with its repository
 * <p/>
 *
 * @author yoavl
 */
public final class RepoRepoPath<R extends Repo> {

    private final R repo;
    private final RepoPath repoPath;

    public RepoRepoPath(R repo, RepoPath repoPath) {
        this.repo = repo;
        this.repoPath = repoPath;
    }

    public R getRepo() {
        return repo;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RepoRepoPath that = (RepoRepoPath) o;
        return repoPath.equals(that.repoPath);
    }

    @Override
    public int hashCode() {
        return repoPath.hashCode();
    }
}