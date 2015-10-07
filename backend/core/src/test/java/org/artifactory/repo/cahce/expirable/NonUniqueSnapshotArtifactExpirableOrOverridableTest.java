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

package org.artifactory.repo.cahce.expirable;

import org.artifactory.api.module.ModuleInfoBuilder;
import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.repo.cache.expirable.NonUniqueSnapshotArtifactExpirableOrOverridable;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Y. Tenne
 */
public class NonUniqueSnapshotArtifactExpirableOrOverridableTest {

    NonUniqueSnapshotArtifactExpirableOrOverridable expirable = new NonUniqueSnapshotArtifactExpirableOrOverridable();

    @Test
    public void testIsExpirableOnNoIntegrationModuleInfo() throws Exception {
        LocalCacheRepo localCacheRepo = EasyMock.createMock(LocalCacheRepo.class);

        EasyMock.expect(localCacheRepo.getItemModuleInfo(EasyMock.isA(String.class)))
                .andReturn(new ModuleInfoBuilder().build()).times(2);

        EasyMock.replay(localCacheRepo);
        expectUnExpirable(localCacheRepo, "momo");
        EasyMock.verify(localCacheRepo);
    }

    @Test
    public void testIsExpirable() throws Exception {
        expectExpirable("g/a/1.0-SNAPSHOT/artifact-5.4-SNAPSHOT.pom");
        expectExpirable("g/a/1.0/artifact-5.4-20081214.090217-4.pom");
        expectExpirable("g/a/1.0-20081214.090217-4/artifact-5.4-20081214.090217-4.pom");

        LocalCacheRepo localCacheRepo = EasyMock.createMock(LocalCacheRepo.class);

        EasyMock.expect(localCacheRepo.getItemModuleInfo(EasyMock.isA(String.class)))
                .andReturn(new ModuleInfoBuilder().folderIntegrationRevision("asdfa").build()).times(2);

        EasyMock.replay(localCacheRepo);
        expectUnExpirable(localCacheRepo, "g/a/1.0-SNAPSHOT/artifact-5.4-20081214.090217-4.pom");
        EasyMock.verify(localCacheRepo);
    }

    private void expectExpirable(String artifactPath) {
        LocalCacheRepo localCacheRepo = EasyMock.createMock(LocalCacheRepo.class);

        EasyMock.expect(localCacheRepo.getItemModuleInfo(EasyMock.isA(String.class)))
                .andReturn(new ModuleInfoBuilder().folderIntegrationRevision("asdfa").build()).times(2);

        EasyMock.replay(localCacheRepo);
        assertTrue(expirable.isExpirable(localCacheRepo, artifactPath),
                artifactPath + " Should be expirable.");
        assertTrue(expirable.isOverridable(localCacheRepo, artifactPath),
                artifactPath + " Should be overridable.");
        EasyMock.verify(localCacheRepo);
    }

    private void expectUnExpirable(LocalCacheRepo localCacheRepo, String artifactPath) {
        assertFalse(expirable.isExpirable(localCacheRepo, artifactPath),
                artifactPath + " Shouldn't be expirable.");
        assertFalse(expirable.isOverridable(localCacheRepo, artifactPath),
                artifactPath + " Shouldn't be overridable.");
    }
}