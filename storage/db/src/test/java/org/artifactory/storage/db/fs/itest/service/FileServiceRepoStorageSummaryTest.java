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

package org.artifactory.storage.db.fs.itest.service;

import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.fs.repo.RepoStorageSummary;
import org.artifactory.storage.fs.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Low level integration tests for the repo summary in the file service.
 *
 * @author Yossi Shaul
 */
@Test
public class FileServiceRepoStorageSummaryTest extends DbBaseTest {

    @Autowired
    private FileService fileService;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-summary.sql");
    }

    public void testSummary() {
        Set<RepoStorageSummary> summaries = fileService.getRepositoriesStorageSummary();
        assertThat(summaries).isNotEmpty().hasSize(3).containsOnly(
                new RepoStorageSummary("repo1", 6, 4, 10),
                new RepoStorageSummary("repo2", 4, 4, 100),
                new RepoStorageSummary("repo3", 0, 0, 0)   // empty repository
        );
    }
}
