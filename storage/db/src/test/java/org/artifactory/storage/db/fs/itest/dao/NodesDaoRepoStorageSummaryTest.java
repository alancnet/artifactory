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

package org.artifactory.storage.db.fs.itest.dao;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.fs.repo.RepoStorageSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests the repo summary methods of the {@link org.artifactory.storage.db.fs.dao.NodesDao}.
 *
 * @author Yossi Shaul
 */
@Test
public class NodesDaoRepoStorageSummaryTest extends DbBaseTest {

    @Autowired
    private NodesDao nodesDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-summary.sql");
    }

    public void testSummary() throws SQLException {
        Set<RepoStorageSummary> summaries = nodesDao.getRepositoriesStorageSummary();
        assertThat(summaries).isNotEmpty().hasSize(3).containsOnly(
                new RepoStorageSummary("repo1", 6, 4, 10),
                new RepoStorageSummary("repo2", 4, 4, 100),
                new RepoStorageSummary("repo3", 0, 0, 0)   // empty repository
        );

        // per field and reflection equality checks
        RepoStorageSummary repo1Summary = getSummaryFor("repo1", summaries);
        assertThat(repo1Summary).isNotNull();
        assertThat(repo1Summary.getRepoKey()).isEqualTo("repo1");
        assertThat(repo1Summary.getFoldersCount()).isEqualTo(6);
        assertThat(repo1Summary.getFilesCount()).isEqualTo(4);
        assertThat(repo1Summary.getUsedSpace()).isEqualTo(10);

        RepoStorageSummary repo2Summary = getSummaryFor("repo2", summaries);
        assertThat(repo2Summary).isNotNull();
        assertThat(EqualsBuilder.reflectionEquals(new RepoStorageSummary("repo2", 4, 4, 100), repo2Summary)).isTrue();

        // repo with no children
        RepoStorageSummary repo3Summary = getSummaryFor("repo3", summaries);
        assertThat(repo3Summary).isNotNull();
        assertThat(EqualsBuilder.reflectionEquals(new RepoStorageSummary("repo3", 0, 0, 0), repo3Summary)).isTrue();
    }

    private RepoStorageSummary getSummaryFor(String repoKey, Set<RepoStorageSummary> summaries) {
        for (RepoStorageSummary summary : summaries) {
            if (summary.getRepoKey().equals(repoKey)) {
                return summary;
            }
        }
        return null;
    }
}
