/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.api.repo.storage;

import org.testng.annotations.Test;

import static org.artifactory.api.repo.storage.RepoStorageSummaryInfo.RepositoryType;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link RepoStorageSummaryInfo}.
 *
 * @author Yossi Shaul
 */
@Test
public class RepoStorageSummaryInfoTest {

    @Test
    public void simpleConstructor() {
        RepoStorageSummaryInfo rs = new RepoStorageSummaryInfo("bla", RepositoryType.LOCAL, 100, 120, 1024, "maven");
        assertEquals(rs.getRepoKey(), "bla");
        assertEquals(rs.getRepoType(), RepositoryType.LOCAL);
        assertEquals(rs.getFoldersCount(), 100);
        assertEquals(rs.getFilesCount(), 120);
        assertEquals(rs.getUsedSpace(), 1024);
        assertEquals(rs.getItemsCount(), 220);
    }
}
