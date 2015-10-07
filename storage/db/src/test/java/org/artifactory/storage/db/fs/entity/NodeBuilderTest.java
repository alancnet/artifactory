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
 * Tests {@link org.artifactory.storage.db.fs.entity.NodeBuilder}.
 *
 * @author Yossi Shaul
 */
@Test
public class NodeBuilderTest {

    public void buildDirectory() throws Exception {
        Date created = new Date(System.currentTimeMillis() - 30000);
        Date modified = new Date(System.currentTimeMillis() - 20000);
        Date updated = new Date(System.currentTimeMillis() - 10000);
        NodeBuilder b = new NodeBuilder().nodeId(2).file(false).repo("repo").path("path/to/there").name("name")
                .created(created).createdBy("yossis").modified(modified).modifiedBy("modifier").updated(
                        updated);

        Node n = b.build();

        assertEquals(n.getNodeId(), 2);
        assertFalse(n.isFile());
        assertEquals(n.getRepo(), "repo");
        assertEquals(n.getPath(), "path/to/there");
        assertEquals(n.getName(), "name");
        assertEquals(n.getDepth(), 4);
        assertEquals(n.getCreated(), created.getTime());
        assertEquals(n.getCreatedBy(), "yossis");
        assertEquals(n.getModified(), modified.getTime());
        assertEquals(n.getModifiedBy(), "modifier");
        assertEquals(n.getUpdated(), updated.getTime());
        assertNull(n.getSha1Actual());
        assertNull(n.getSha1Original());
        assertNull(n.getMd5Actual());
        assertNull(n.getMd5Original());
    }

    public void buildFile() throws Exception {
        Date created = new Date(System.currentTimeMillis() - 30000);
        Date modified = new Date(System.currentTimeMillis() - 20000);
        Date updated = new Date(System.currentTimeMillis() - 10000);
        NodeBuilder b = new NodeBuilder();
        b.nodeId(999).file(false).repo("repo").path("path/file").name("filename").created(created)
                .createdBy("yossis").modified(modified).modifiedBy("modifier").updated(updated).length(333)
                .sha1Actual("aaa").sha1Original("bbb").md5Actual("ccc").md5Original("ddd");

        Node n = b.build();

        assertEquals(n.getNodeId(), 999);
        assertFalse(n.isFile());
        assertEquals(n.getRepo(), "repo");
        assertEquals(n.getPath(), "path/file");
        assertEquals(n.getName(), "filename");
        assertEquals(n.getDepth(), 3);
        assertEquals(n.getCreated(), created.getTime());
        assertEquals(n.getCreatedBy(), "yossis");
        assertEquals(n.getModified(), modified.getTime());
        assertEquals(n.getModifiedBy(), "modifier");
        assertEquals(n.getUpdated(), updated.getTime());
        assertEquals(n.getLength(), 333);
        assertEquals(n.getSha1Actual(), "aaa");
        assertEquals(n.getSha1Original(), "bbb");
        assertEquals(n.getMd5Actual(), "ccc");
        assertEquals(n.getMd5Original(), "ddd");
    }
}
