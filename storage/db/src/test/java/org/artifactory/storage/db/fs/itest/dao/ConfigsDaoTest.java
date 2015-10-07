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

import org.apache.commons.io.IOUtils;
import org.artifactory.storage.db.fs.dao.ConfigsDao;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.util.blob.BlobWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import static org.testng.Assert.*;

/**
 * Tests the {@link org.artifactory.storage.db.fs.dao.ConfigsDao}.
 *
 * @author Yossi Shaul
 */
public class ConfigsDaoTest extends DbBaseTest {

    @Autowired
    private ConfigsDao configsDao;

    public void createConfig() throws SQLException, UnsupportedEncodingException {
        int insertCount = configsDao.createConfig("test", "data");
        assertEquals(insertCount, 1);
    }

    @Test(dependsOnMethods = "createConfig")
    public void hasConfig() throws SQLException {
        assertTrue(configsDao.hasConfig("test"));
    }

    @Test(dependsOnMethods = "createConfig")
    public void loadConfig() throws SQLException {
        String data = configsDao.loadConfig("test");
        assertNotNull(data);
        assertEquals(data, "data");
    }

    @Test(dependsOnMethods = "loadConfig")
    public void updateConfig() throws SQLException, UnsupportedEncodingException {
        int updateCount = configsDao.updateConfig("test", "newdata");
        assertEquals(updateCount, 1);
        String result = configsDao.loadConfig("test");
        assertNotNull(result);
        assertEquals(result, "newdata");
    }

    private byte[] getByteTest() {
        byte[] bytes = new byte[1024];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        return bytes;
    }

    public void createStreamConfig() throws SQLException {
        byte[] byteTest = getByteTest();
        int insertCount = configsDao.createConfig("test:stream",
                new BlobWrapper(new ByteArrayInputStream(byteTest), byteTest.length));
        assertEquals(insertCount, 1);
    }

    @Test(dependsOnMethods = "createStreamConfig")
    public void hasStreamConfig() throws SQLException {
        assertTrue(configsDao.hasConfig("test:stream"));
    }

    @Test(dependsOnMethods = "createStreamConfig")
    public void loadStreamConfig() throws SQLException, IOException {
        byte[] data = IOUtils.toByteArray(configsDao.loadStreamConfig("test:stream"));
        assertNotNull(data);
        assertEquals(data, getByteTest());
    }

    @Test(dependsOnMethods = "loadStreamConfig")
    public void updateStreamConfig() throws SQLException, IOException {
        byte[] byteTest = getByteTest();
        byteTest[0] = 2;
        int updateCount = configsDao.updateConfig("test:stream",
                new BlobWrapper(new ByteArrayInputStream(byteTest), byteTest.length));
        assertEquals(updateCount, 1);
        byte[] result = IOUtils.toByteArray(configsDao.loadStreamConfig("test:stream"));
        assertNotNull(result);
        assertEquals(result, byteTest);
    }

    @Test(dependsOnMethods = {"updateStreamConfig", "updateConfig", "hasConfig", "hasStreamConfig"})
    public void testDelete() throws SQLException, IOException {
        assertEquals(configsDao.deleteConfig("test:stream"), 1);
        assertEquals(configsDao.deleteConfig("test"), 1);
    }

    public void hasConfigNoConfig() throws SQLException {
        assertFalse(configsDao.hasConfig("nosuchconfig"));
    }

    public void loadConfigNoConfig() throws SQLException {
        assertNull(configsDao.loadConfig("nosuchconfig"));
    }

    public void updateConfigNoConfig() throws SQLException, UnsupportedEncodingException {
        int updateCount = configsDao.updateConfig("nosuchconfig", "newdata");
        assertEquals(updateCount, 0);
    }

}
