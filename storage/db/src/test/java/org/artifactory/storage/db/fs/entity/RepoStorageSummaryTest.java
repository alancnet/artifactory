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

package org.artifactory.storage.db.fs.entity;

import org.artifactory.storage.fs.repo.RepoStorageSummary;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests the {@link org.artifactory.storage.fs.repo.RepoStorageSummary} entity.
 *
 * @author Yossi Shaul
 */
@Test
public class RepoStorageSummaryTest {

    public void simpleConstructor() {
        RepoStorageSummary r = new RepoStorageSummary("key", 6, 1, 456);
        assertThat(r.getRepoKey()).isEqualTo("key");
        assertThat(r.getFoldersCount()).isEqualTo(6);
        assertThat(r.getFilesCount()).isEqualTo(1);
        assertThat(r.getUsedSpace()).isEqualTo(456);
    }
}
