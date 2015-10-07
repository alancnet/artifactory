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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests the VirtualRepoResolver.
 *
 * @author Yossi Shaul
 */
@Test
public class VirtualRepoResolverTest {
    private final HttpRepoDescriptor remote1 = new HttpRepoDescriptor();
    private final HttpRepoDescriptor remote2 = new HttpRepoDescriptor();
    private final HttpRepoDescriptor remote3 = new HttpRepoDescriptor();
    private final LocalRepoDescriptor local1 = new LocalRepoDescriptor();
    private final LocalRepoDescriptor local2 = new LocalRepoDescriptor();
    private final LocalRepoDescriptor local3 = new LocalRepoDescriptor();

    @BeforeMethod
    public void initTestRepos() {
        remote1.setKey("remote1");
        remote2.setKey("remote2");
        remote3.setKey("remote3");
        local1.setKey("local1");
        local2.setKey("local2");
        local3.setKey("local3");
    }

    public void virtualWithLocalAndRemote() {
        VirtualRepoDescriptor virtual = new VirtualRepoDescriptor();
        virtual.setKey("virtual");
        List<RepoDescriptor> reposList = getRepoList(remote1, local1, remote2, local2, local3);
        virtual.setRepositories(reposList);

        VirtualRepoResolver resolver = new VirtualRepoResolver(virtual);

        List<LocalRepoDescriptor> localRepos = resolver.getLocalRepos();
        List<RemoteRepoDescriptor> remoteRepos = resolver.getRemoteRepos();
        List<RealRepoDescriptor> orderedRepos = resolver.getOrderedRepos();
        assertNotNull(localRepos, "Local repo list should not be null");
        assertNotNull(remoteRepos, "Remote repo list should not be null");
        assertNotNull(orderedRepos, "Ordered repo list should not be null");
        assertEquals(localRepos.size(), 3, "Expecting 3 local repositories");
        assertEquals(remoteRepos.size(), 2, "Expecting 2 remote repositories");
        assertEquals(orderedRepos.size(), 5, "Expecting 5 repositories");

        // check the order in each list
        assertSame(localRepos.get(0), local1);
        assertSame(localRepos.get(1), local2);
        assertSame(localRepos.get(2), local3);

        assertSame(remoteRepos.get(0), remote1);
        assertSame(remoteRepos.get(1), remote2);

        assertSame(orderedRepos.get(0), local1);
        assertSame(orderedRepos.get(1), local2);
        assertSame(orderedRepos.get(2), local3);
        assertSame(orderedRepos.get(3), remote1);
        assertSame(orderedRepos.get(4), remote2);

    }

    public void virtualWithinVirtualNoCycles() {
        VirtualRepoDescriptor virtual1 = new VirtualRepoDescriptor();
        virtual1.setKey("virtual1");
        virtual1.setRepositories(getRepoList(remote2, local2));

        VirtualRepoDescriptor virtualToTest = new VirtualRepoDescriptor();
        virtualToTest.setKey("virtualToTest");
        virtualToTest.setRepositories(getRepoList(remote1, virtual1, local1));

        VirtualRepoResolver resolver = new VirtualRepoResolver(virtualToTest);

        List<LocalRepoDescriptor> localRepos = resolver.getLocalRepos();
        List<RemoteRepoDescriptor> remoteRepos = resolver.getRemoteRepos();
        List<RealRepoDescriptor> orderedRepos = resolver.getOrderedRepos();
        assertEquals(localRepos.size(), 2, "Expecting 2 local repositories");
        assertEquals(remoteRepos.size(), 2, "Expecting 2 remote repositories");
        assertEquals(orderedRepos.size(), 4, "Expecting 4 repositories");

        // excpect [local2, local1, remote1, remote2]
        assertSame(orderedRepos.get(0), local2);
        assertSame(orderedRepos.get(1), local1);
        assertSame(orderedRepos.get(2), remote1);
        assertSame(orderedRepos.get(3), remote2);
        assertFalse(resolver.hasCycle(), "This configuration doesn't contains a cycle");
    }

    public void sameRepoInTwoVirtualRepos() {
        VirtualRepoDescriptor virtual1 = new VirtualRepoDescriptor();
        virtual1.setKey("virtual1");
        virtual1.setRepositories(getRepoList(remote2, local2));

        VirtualRepoDescriptor virtual2 = new VirtualRepoDescriptor();
        virtual2.setKey("virtual2");
        virtual2.setRepositories(getRepoList(remote2, local2, local1));

        VirtualRepoDescriptor virtualToTest = new VirtualRepoDescriptor();
        virtualToTest.setKey("virtualToTest");
        virtualToTest.setRepositories(getRepoList(virtual1, virtual2));

        VirtualRepoResolver resolver = new VirtualRepoResolver(virtualToTest);

        List<RealRepoDescriptor> orderedRepos = resolver.getOrderedRepos();
        assertEquals(orderedRepos.size(), 3, "Expecting 3 repositories");

        // excpect [local2, local1, remote2]
        assertSame(orderedRepos.get(0), local2);
        assertSame(orderedRepos.get(1), local1);
        assertSame(orderedRepos.get(2), remote2);
    }

    public void virtualInVirtualWithCycles() {
        VirtualRepoDescriptor virtual1 = new VirtualRepoDescriptor();
        virtual1.setKey("virtual1");
        VirtualRepoDescriptor virtual2 = new VirtualRepoDescriptor();
        virtual2.setKey("virtual2");

        virtual1.setRepositories(getRepoList(local2, virtual1));
        virtual2.setRepositories(getRepoList(remote2, local2, virtual2));

        VirtualRepoDescriptor virtualToTest = new VirtualRepoDescriptor();
        virtualToTest.setKey("virtualToTest");
        virtualToTest.setRepositories(getRepoList(virtual1, virtual2));

        VirtualRepoResolver resolver = new VirtualRepoResolver(virtualToTest);

        List<RealRepoDescriptor> orderedRepos = resolver.getOrderedRepos();
        assertTrue(resolver.hasCycle(), "This configuration contains a cycle");
        assertEquals(orderedRepos.size(), 2, "Expecting 2 repositories");

        // excpect [local2, remote2]
        assertSame(orderedRepos.get(0), local2);
        assertSame(orderedRepos.get(1), remote2);
    }

    private List<RepoDescriptor> getRepoList(RepoDescriptor... repos) {
        return Arrays.asList(repos);
    }

}
