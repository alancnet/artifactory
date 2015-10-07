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

package org.artifactory.storage.db.fs.itest.service;

import org.artifactory.md.Properties;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.fs.service.PropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.*;

/**
 * @author Yossi Shaul
 */
@Test
public class PropertiesServiceImplTest extends DbBaseTest {

    @Autowired
    private PropertiesService propertiesService;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-for-service.sql");
    }

    public void getPropertiesNodeWithProperties() {
        Properties props = propertiesService.loadProperties(5);

        assertEquals(props.keySet().size(), 2);
        String value = props.getFirst("build.name");
        assertEquals(value, "ant");
    }

    public void getPropertiesNodeWithMultiValueProperties() {
        Properties props = propertiesService.loadProperties(7);

        assertEquals(props.keySet().size(), 1, "One unique key");
        Set<String> values = props.get("yossis");
        assertNotNull(values);
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));
    }

    public void getPropertiesNodeWithNone() {
        assertEquals(propertiesService.loadProperties(2).size(), 0);
    }

    public void getPropertiesNodeNotExist() {
        assertEquals(propertiesService.loadProperties(78849).size(), 0);
    }

    public void setProperties() {
        assertEquals(propertiesService.loadProperties(6).size(), 0);
        PropertiesImpl properties = new PropertiesImpl();
        properties.put("key1", "1");
        properties.put("key1", "2");
        properties.put("key2", "2");

        propertiesService.setProperties(6, properties);

        assertEquals(propertiesService.loadProperties(6), properties);
    }

    @Test(dependsOnMethods = "setProperties")
    public void deleteProperties() {
        int count = propertiesService.deleteProperties(6);
        assertEquals(count, 3);

        assertEquals(propertiesService.loadProperties(6).size(), 0);
    }

    public void hasPropertiesPathWithProperties() {
        assertTrue(propertiesService.hasProperties(new RepoPathImpl("repo1", "ant/ant/1.5/ant-1.5.jar")));
    }

    public void hasPropertiesPathWithNoProperties() {
        assertFalse(propertiesService.hasProperties(new RepoPathImpl("repo2", "org")));
    }
}
