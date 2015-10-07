/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

import org.testng.annotations.Test;

import static org.artifactory.repo.InternalRepoPathFactory.*;
import static org.testng.Assert.assertEquals;

/**
 * Tests {@link InternalRepoPathFactory}.
 * This test cannot be in the capi module because it has runtime dependencies on the storage-common.
 *
 * @author Shay Yaakov
 */
@Test
public class InternalRepoPathFactoryTest {

    public void cacheRepoPathFromRemote() throws Exception {
        RepoPath cacheRepoPath = cacheRepoPath(create("repo", "boo"));
        assertEquals(cacheRepoPath, create("repo-cache", "boo"));
    }

    public void cacheRepoPathFromCache() throws Exception {
        RepoPath cacheRepoPath = cacheRepoPath(create("test-cache", "la/la/la"));
        assertEquals(cacheRepoPath, create("test-cache-cache", "la/la/la"));
    }

    public void cacheRepoPathFromRootRepoPath() throws Exception {
        RepoPath cacheRepoPath = cacheRepoPath(repoRootPath("rootrepo"));
        assertEquals(cacheRepoPath, create("rootrepo-cache"));
    }
}
