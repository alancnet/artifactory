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

package org.artifactory.storage.db.fs.entity;

import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.*;

/**
 * Tests {@link org.artifactory.storage.db.fs.entity.Node}.
 *
 * @author Yossi Shaul
 */
@Test
public class NodeTest {

    public void directoryConstructor() {
        Date created = new Date(System.currentTimeMillis() - 30000);
        Date modified = new Date(System.currentTimeMillis() - 20000);
        Date updated = new Date(System.currentTimeMillis() - 10000);
        Node n = new Node(2, false, "repo", "path/to/there", "name", (short) 3, created.getTime(), "yossis",
                modified.getTime(), "modifier", updated.getTime(), 0, null, null, null, null);

        assertEquals(n.getNodeId(), 2);
        assertFalse(n.isFile());
        assertEquals(n.getRepo(), "repo");
        assertEquals(n.getPath(), "path/to/there");
        assertEquals(n.getName(), "name");
        assertEquals(n.getDepth(), 3);
        assertEquals(n.getCreated(), created.getTime());
        assertEquals(n.getCreatedBy(), "yossis");
        assertEquals(n.getModified(), modified.getTime());
        assertEquals(n.getModifiedBy(), "modifier");
        assertEquals(n.getUpdated(), updated.getTime());
        assertEquals(n.getLength(), 0);
        assertNull(n.getSha1Actual());
        assertNull(n.getSha1Original());
        assertNull(n.getMd5Actual());
        assertNull(n.getMd5Original());
    }

    public void fileConstructor() {
        Date created = new Date(System.currentTimeMillis() - 30000);
        Date modified = new Date(System.currentTimeMillis() - 20000);
        Date updated = new Date(System.currentTimeMillis() - 10000);
        Node n = new Node(3, true, "repo2", "path/to", "file", (short) 2, created.getTime(), "yossis",
                modified.getTime(), "modifier", updated.getTime(),
                22, "shasha", "ahsahs", "md5md5", "5dm5dm");

        assertEquals(n.getNodeId(), 3);
        assertTrue(n.isFile());
        assertEquals(n.getRepo(), "repo2");
        assertEquals(n.getPath(), "path/to");
        assertEquals(n.getName(), "file");
        assertEquals(n.getDepth(), 2);
        assertEquals(n.getCreated(), created.getTime());
        assertEquals(n.getCreatedBy(), "yossis");
        assertEquals(n.getModified(), modified.getTime());
        assertEquals(n.getModifiedBy(), "modifier");
        assertEquals(n.getUpdated(), updated.getTime());
        assertEquals(n.getLength(), 22);
        assertEquals(n.getSha1Actual(), "shasha");
        assertEquals(n.getSha1Original(), "ahsahs");
        assertEquals(n.getMd5Actual(), "md5md5");
        assertEquals(n.getMd5Original(), "5dm5dm");
    }

}
