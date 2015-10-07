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

package org.artifactory.addon.replication;

import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.testng.annotations.Test;

import java.io.StringWriter;

import static org.testng.Assert.*;

/**
 * @author Noam Y. Tenne
 */
@Test
public class RemoteReplicationSettingsBuilderTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNoRepoPath() throws Exception {
        new RemoteReplicationSettingsBuilder(null).build();
    }

    @Test
    public void testDefaultValues() throws Exception {
        RepoPath repoPath = InternalRepoPathFactory.create("moo", "mieow");
        StringWriter stringWriter = new StringWriter();
        RemoteReplicationSettings settings = new RemoteReplicationSettingsBuilder(repoPath).responseWriter(stringWriter)
                .build();

        assertEquals(settings.getRepoPath(), repoPath, "Unexpected repo path.");
        assertFalse(settings.isProgress(), "Unexpected default progress display state.");
        assertEquals(settings.getMark(), 0, "Unexpected default mark.");
        assertFalse(settings.isDeleteExisting(), "Unexpected default delete existing state.");
        assertFalse(settings.isIncludeProperties(), "Unexpected default property inclusion state.");
        assertEquals(settings.getOverwrite(), ReplicationAddon.Overwrite.force,
                "Unexpected default overwrite switch state.");
        assertEquals(settings.getResponseWriter(), stringWriter, "Unexpected response writer.");
    }

    @Test
    public void testSetters() throws Exception {
        RepoPath repoPath = InternalRepoPathFactory.create("moo", "mieow");
        StringWriter stringWriter = new StringWriter();
        RemoteReplicationSettings settings = new RemoteReplicationSettingsBuilder(repoPath).progress(true).mark(15)
                .responseWriter(stringWriter).deleteExisting(true).includeProperties(true)
                .overwrite(ReplicationAddon.Overwrite.never).build();

        assertEquals(settings.getRepoPath(), repoPath, "Unexpected repo path.");
        assertTrue(settings.isProgress(), "Unexpected progress display state.");
        assertEquals(settings.getMark(), 15, "Unexpected mark.");
        assertTrue(settings.isDeleteExisting(), "Unexpected delete existing state.");
        assertTrue(settings.isIncludeProperties(), "Unexpected property inclusion state.");
        assertEquals(settings.getOverwrite(), ReplicationAddon.Overwrite.never,
                "Unexpected overwrite switch state.");
        assertEquals(settings.getResponseWriter(), stringWriter, "Unexpected response writer.");
    }
}
