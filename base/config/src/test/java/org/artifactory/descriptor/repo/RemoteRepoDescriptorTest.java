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

package org.artifactory.descriptor.repo;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the RemoteRepoDescriptor class.
 *
 * @author Yossi Shaul
 */
@Test
public class RemoteRepoDescriptorTest {
    public void defaultConstructor() {
        RemoteRepoDescriptor remote = new RemoteRepoDescriptor() {
        };
        assertNull(remote.getKey());
        assertEquals(remote.getIncludesPattern(), "**/*");
        assertNull(remote.getExcludesPattern());
        assertNull(remote.getDescription());
        assertNull(remote.getRepoLayout());
        assertEquals(remote.getAssumedOfflinePeriodSecs(), 300);
        assertEquals(remote.getMaxUniqueSnapshots(), 0);
        assertEquals(remote.getMissedRetrievalCachePeriodSecs(), 1800);
        assertEquals(remote.getRetrievalCachePeriodSecs(), 600);
        assertEquals(remote.getChecksumPolicyType(), ChecksumPolicyType.GEN_IF_ABSENT);
        assertNull(remote.getUrl());
        assertNull(remote.getRemoteRepoLayout());
        assertFalse(remote.isOffline());
        assertFalse(remote.isBlackedOut());
        assertFalse(remote.isCache());
        assertFalse(remote.isLocal());
        assertFalse(remote.isHardFail());
        assertTrue(remote.isStoreArtifactsLocally());
        assertFalse(remote.isFetchJarsEagerly());
        assertFalse(remote.isFetchSourcesEagerly());
        assertTrue(remote.isSuppressPomConsistencyChecks(),
                "Default should not supress pom consistency checks");
        assertEquals(remote.getUnusedArtifactsCleanupPeriodHours(), 0);
        assertFalse(remote.isShareConfiguration());
        assertNotNull(remote.getPropertySets(), "Property sets list should not be null");
        assertFalse(remote.isRejectInvalidJars());
        assertNotEquals(remote.getType(), RepoType.NuGet, "NuGet should be off by default.");
    }
}
