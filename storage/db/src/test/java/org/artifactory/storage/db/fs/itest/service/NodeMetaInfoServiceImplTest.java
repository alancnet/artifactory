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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.fs.service.NodeMetaInfoServiceImpl;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.fs.VfsException;
import org.artifactory.storage.fs.service.ItemMetaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.*;

/**
 * DB Tests the {@link org.artifactory.storage.db.fs.service.NodeMetaInfoServiceImpl}.
 *
 * @author Yossi Shaul
 */
@Test
public class NodeMetaInfoServiceImplTest extends DbBaseTest {

    @Autowired
    private NodeMetaInfoServiceImpl nodeMetaService;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-for-service.sql");
    }

    public void hasNodeMetadata() throws Exception {
        assertTrue(nodeMetaService.hasNodeMetadata(new RepoPathImpl("repo1", "org/yossis")));
    }

    public void hasNodeMetadataNodeWithNoProps() throws Exception {
        assertFalse(nodeMetaService.hasNodeMetadata(new RepoPathImpl("repo2", "")));
    }

    public void getNodeMetadata() throws SQLException {
        ItemMetaInfo metaInfo = nodeMetaService.getNodeMetaInfo(new RepoPathImpl("repo1", "org/yossis"));
        assertNotNull(metaInfo);
        assertEquals(metaInfo.getPropsModified(), 1340286203121L);
        assertEquals(metaInfo.getPropsModifiedBy(), "yoyo");
    }

    public void getNodeMetadataNoSuchNode() throws SQLException {
        assertNull(nodeMetaService.getNodeMetaInfo(new RepoPathImpl("repo7", "no/item/here")));
    }

    public void getNodeMetadataNoMetadata() throws SQLException {
        assertNull(nodeMetaService.getNodeMetaInfo(new RepoPathImpl("repo2", "")));
    }

    public void insertMetadata() throws SQLException {
        long time = System.currentTimeMillis();
        ItemMetaInfo toInsert = new ItemMetaInfo(time, "me");
        // insert new
        nodeMetaService.createOrUpdateNodeMetaInfo(6, toInsert);
        ItemMetaInfo fromDb = nodeMetaService.getNodeMetaInfo(new RepoPathImpl("repo1", "org"));
        assertTrue(EqualsBuilder.reflectionEquals(toInsert, fromDb));

        // update existing
        ItemMetaInfo toUpdate = new ItemMetaInfo(time + 1000, "another");
        nodeMetaService.createOrUpdateNodeMetaInfo(6, toUpdate);
        fromDb = nodeMetaService.getNodeMetaInfo(new RepoPathImpl("repo1", "org"));
        assertTrue(EqualsBuilder.reflectionEquals(toUpdate, fromDb));
    }

    @Test(expectedExceptions = VfsException.class)
    public void insertMetadataNonExistentNode() throws SQLException {
        nodeMetaService.createOrUpdateNodeMetaInfo(8989, new ItemMetaInfo(System.currentTimeMillis(), "me"));
    }

    public void deleteMetadata() throws SQLException {
        RepoPath pathWithMeta = new RepoPathImpl("repo1", "ant/ant/1.5/ant-1.5.jar");
        assertTrue(nodeMetaService.hasNodeMetadata(pathWithMeta));
        nodeMetaService.deleteMetaInfo(pathWithMeta);
        assertFalse(nodeMetaService.hasNodeMetadata(pathWithMeta));
    }

    public void deleteMetadataNotExist() throws SQLException {
        RepoPath pathWithNoMeta = new RepoPathImpl("repo2", "org/jfrog/test/test2.jar");
        assertFalse(nodeMetaService.hasNodeMetadata(pathWithNoMeta));
        nodeMetaService.deleteMetaInfo(pathWithNoMeta);
        assertFalse(nodeMetaService.hasNodeMetadata(pathWithNoMeta));
    }
}
