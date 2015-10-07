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
import org.artifactory.storage.db.fs.dao.NodeMetaInfoDao;
import org.artifactory.storage.db.fs.entity.NodeMetaInfo;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.*;

/**
 * Tests {@link org.artifactory.storage.db.fs.dao.NodeMetaInfoDao}.
 *
 * @author Yossi Shaul
 */
@Test
public class NodeMetaInfosDaoTest extends DbBaseTest {

    @Autowired
    private NodeMetaInfoDao metaDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void hasNodeMetadata() throws Exception {
        assertTrue(metaDao.hasNodeMetadata(5));
    }

    public void hasNodeMetadataNodeWithNoProps() throws Exception {
        assertFalse(metaDao.hasNodeMetadata(2));
    }

    public void getNodeMetadata() throws SQLException {
        NodeMetaInfo nodeMetadata = metaDao.getNodeMetadata(5);
        assertNotNull(nodeMetadata);
        assertEquals(nodeMetadata.getNodeId(), 5);
        assertEquals(nodeMetadata.getPropsModified(), 1340286103555L);
        assertEquals(nodeMetadata.getPropsModifiedBy(), "yossis");
    }

    public void getNodeMetadataNoSuchNode() throws SQLException {
        assertNull(metaDao.getNodeMetadata(748378));
    }

    public void getNodeMetadataNoMetadata() throws SQLException {
        assertNull(metaDao.getNodeMetadata(2));
    }

    public void insertMetadata() throws SQLException {
        long time = System.currentTimeMillis();
        NodeMetaInfo toInsert = new NodeMetaInfo(6, time, "me");
        metaDao.create(toInsert);

        NodeMetaInfo fromDb = metaDao.getNodeMetadata(toInsert.getNodeId());

        assertTrue(EqualsBuilder.reflectionEquals(toInsert, fromDb));
    }

    @Test(expectedExceptions = SQLException.class)
    public void insertMetadataNonExistentNode() throws SQLException {
        metaDao.create(new NodeMetaInfo(6, System.currentTimeMillis(), "me"));
    }

    public void updateMetadata() throws SQLException {
        NodeMetaInfo metaInfo = new NodeMetaInfo(4, System.currentTimeMillis(), "someone");
        metaDao.create(metaInfo);

        NodeMetaInfo toUpdate = new NodeMetaInfo(4, 12345678L, "someoneelse");
        metaDao.update(toUpdate);

        NodeMetaInfo updatedMetaInfo = metaDao.getNodeMetadata(4);
        assertNotNull(updatedMetaInfo);
        assertTrue(EqualsBuilder.reflectionEquals(toUpdate, updatedMetaInfo));
    }

    public void updateMetadataNotExist() throws SQLException {
        assertEquals(0, metaDao.update(new NodeMetaInfo(8989439, 12345678L, "someone")));
    }

    public void deleteMetadata() throws SQLException {
        assertTrue(metaDao.hasNodeMetadata(9));
        assertEquals(1, metaDao.deleteNodeMeta(9));
        assertFalse(metaDao.hasNodeMetadata(9));
    }

    public void deleteMetadataNotExist() throws SQLException {
        assertEquals(0, metaDao.deleteNodeMeta(11));
    }

}
