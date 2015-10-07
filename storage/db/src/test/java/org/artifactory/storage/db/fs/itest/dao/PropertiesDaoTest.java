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

package org.artifactory.storage.db.fs.itest.dao;

import org.apache.commons.lang.RandomStringUtils;
import org.artifactory.storage.db.DbType;
import org.artifactory.storage.db.fs.dao.PropertiesDao;
import org.artifactory.storage.db.fs.entity.NodeProperty;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;

import java.sql.SQLException;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.*;

/**
 * Tests {@link org.artifactory.storage.db.fs.dao.PropertiesDao}.
 *
 * @author Yossi Shaul
 */
public class PropertiesDaoTest extends DbBaseTest {

    @Autowired
    private PropertiesDao propsDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void hasPropertiesNodeWithProperties() throws SQLException {
        boolean result = propsDao.hasNodeProperties(5);
        assertTrue(result, "Node expected to hold properties");
    }

    public void hasPropertiesNodeWithoutProperties() throws SQLException {
        boolean result = propsDao.hasNodeProperties(1);
        assertFalse(result, "Node is not expected to hold properties");
    }

    public void hasPropertiesNodeNotExist() throws SQLException {
        boolean result = propsDao.hasNodeProperties(5478939);
        assertFalse(result, "Node that doesn't exist is not expected to hold properties");
    }

    public void getPropertiesNodeWithProperties() throws SQLException {
        List<NodeProperty> result = propsDao.getNodeProperties(5);
        assertNotNull(result);
        assertEquals(result.size(), 2);
        for (NodeProperty property : result) {
            assertEquals(property.getNodeId(), 5, "All results should be with the same node id");
        }

        NodeProperty buildName = getById(1, result);
        assertEquals(buildName.getPropId(), 1);
        assertEquals(buildName.getPropKey(), "build.name");
        assertEquals(buildName.getPropValue(), "ant");
    }

    public void getPropertiesNodeWithEmptyProperties() throws SQLException {
        List<NodeProperty> result = propsDao.getNodeProperties(14);
        assertNotNull(result);
        assertEquals(result.size(), 2);
        for (NodeProperty property : result) {
            assertEquals(property.getNodeId(), 14, "All results should be with the same node id");
        }

        NodeProperty emptyVal = getById(6, result);
        assertEquals(emptyVal.getPropId(), 6);
        assertEquals(emptyVal.getPropKey(), "empty.val");
        assertEquals(emptyVal.getPropValue(), "");

        NodeProperty nullVal = getById(7, result);
        assertEquals(nullVal.getPropId(), 7);
        assertEquals(nullVal.getPropKey(), "null.val");
        assertEquals(emptyVal.getPropValue(), "");
    }

    public void getPropertiesNodeWithoutProperties() throws SQLException {
        List<NodeProperty> result = propsDao.getNodeProperties(1);
        assertEquals(result.size(), 0);
    }

    public void getPropertiesNodeNotExist() throws SQLException {
        List<NodeProperty> result = propsDao.getNodeProperties(98958459);
        assertEquals(result.size(), 0);
    }

    public void insertProperty() throws SQLException {
        int createCount = propsDao.create(new NodeProperty(11, 9, "key1", "value1"));
        assertEquals(createCount, 1);
        createCount = propsDao.create(new NodeProperty(12, 9, "key2", "value2"));
        assertEquals(createCount, 1);

        List<NodeProperty> properties = propsDao.getNodeProperties(9);
        assertEquals(properties.size(), 2);
    }

    public void deletePropertiesNodeWithProperties() throws SQLException {
        // first check the properties exist
        assertEquals(propsDao.getNodeProperties(9).size(), 3);

        int deletedCount = propsDao.deleteNodeProperties(9);
        assertEquals(deletedCount, 3);
        assertEquals(propsDao.getNodeProperties(9).size(), 0);
    }

    public void deletePropertiesNodeWithNoProperties() throws SQLException {
        assertEquals(propsDao.deleteNodeProperties(1), 0);
    }

    public void deletePropertiesNonExistentNode() throws SQLException {
        assertEquals(propsDao.deleteNodeProperties(6778678), 0);
    }

    public void trimLongPropertyValue() throws SQLException {
        if (storageProperties.getDbType() == DbType.MSSQL) {
            return; // RTFACT-5768
        }
        String longValue = RandomStringUtils.randomAscii(4020);
        propsDao.create(new NodeProperty(876, 15, "trimeme", longValue));
        List<NodeProperty> nodeProperties = propsDao.getNodeProperties(15);
        assertThat(nodeProperties.size()).isEqualTo(1);
        String trimmedValue = nodeProperties.get(0).getPropValue();
        assertThat(trimmedValue).hasSize(4000);
        assertThat(longValue).startsWith(trimmedValue);
    }

    private NodeProperty getById(long propId, List<NodeProperty> properties) {
        for (NodeProperty property : properties) {
            if (property.getPropId() == propId) {
                return property;
            }
        }
        return null;
    }
}
