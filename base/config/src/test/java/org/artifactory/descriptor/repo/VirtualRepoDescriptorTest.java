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

import com.google.common.collect.Lists;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the VirtualRepoDescriptor.
 *
 * @author Yossi Shaul
 */
@Test
public class VirtualRepoDescriptorTest {

    public void defaultConstructor() {
        VirtualRepoDescriptor virtualRepo = new VirtualRepoDescriptor();
        assertNull(virtualRepo.getKey());
        assertNull(virtualRepo.getRepoLayout());
        assertNull(virtualRepo.getKeyPair());
        assertTrue(virtualRepo.getRepositories().isEmpty());
        assertNotEquals(virtualRepo.getType(), RepoType.NuGet, "NuGet should be off by default.");
    }

    public void identicalCache() {
        VirtualRepoDescriptor virtualRepo1 = new VirtualRepoDescriptor();
        VirtualRepoDescriptor virtualRepo2 = new VirtualRepoDescriptor();

        assertTrue(virtualRepo1.identicalCache(virtualRepo2));

        LocalRepoDescriptor local1 = new LocalRepoDescriptor();
        local1.setKey("local1");

        LocalRepoDescriptor local2 = new LocalRepoDescriptor();
        local1.setKey("local2");

        virtualRepo1.setRepositories(Lists.<RepoDescriptor>newArrayList(local1));
        virtualRepo2.setRepositories(Lists.<RepoDescriptor>newArrayList(local2));

        assertFalse(virtualRepo1.identicalCache(virtualRepo2));
    }
}
