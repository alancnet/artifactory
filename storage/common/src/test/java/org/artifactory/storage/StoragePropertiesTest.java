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

package org.artifactory.storage;

import org.artifactory.storage.db.DbType;
import org.artifactory.util.ResourceUtils;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

/**
 * Unit tests for the {@link org.artifactory.storage.StorageProperties} class.
 *
 * @author Yossi Shaul
 */
@Test
public class StoragePropertiesTest {

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*Space.*")
    public void failUnsupportedDatabaseType() throws IOException {
        new StorageProperties(ResourceUtils.getResourceAsFile("/storage/unsupported.properties"));
    }

    public void minimalPropertiesFile() throws IOException {
        StorageProperties sp = new StorageProperties(
                ResourceUtils.getResourceAsFile("/storage/minimalstorage.properties"));

        assertEquals(sp.getDbType(), DbType.DERBY);
        assertEquals(sp.getConnectionUrl(), "jdbc:to:somewhere");
        assertEquals(sp.getDriverClass(), "some.driver");
        assertNull(sp.getUsername());
        assertNull(sp.getPassword());
        assertEquals(sp.getMaxActiveConnections(), StorageProperties.DEFAULT_MAX_ACTIVE_CONNECTIONS);
        assertEquals(sp.getMaxIdleConnections(), StorageProperties.DEFAULT_MAX_IDLE_CONNECTIONS);
        assertEquals(sp.getBinaryProviderCacheMaxSize(), 5368709120L);
    }

    public void minimalWithCacheSize() throws IOException {
        StorageProperties sp = new StorageProperties(
                ResourceUtils.getResourceAsFile("/storage/gigscachesize.properties"));

        assertEquals(sp.getDbType(), DbType.DERBY);
        assertEquals(sp.getConnectionUrl(), "jdbc:to:somewhere");
        assertEquals(sp.getDriverClass(), "some.driver");
        assertNull(sp.getUsername());
        assertNull(sp.getPassword());
        assertEquals(sp.getMaxActiveConnections(), StorageProperties.DEFAULT_MAX_ACTIVE_CONNECTIONS);
        assertEquals(sp.getMaxIdleConnections(), StorageProperties.DEFAULT_MAX_IDLE_CONNECTIONS);
        assertEquals(sp.getBinaryProviderCacheMaxSize(), 1073741824L); // 1g
    }

    public void valuesWithSpaces() throws IOException {
        StorageProperties sp = new StorageProperties(ResourceUtils.getResourceAsFile("/storage/trim.properties"));

        assertEquals(sp.getDbType(), DbType.DERBY);
        assertEquals(sp.getConnectionUrl(), "jdbc:to:removespaces");
        assertEquals(sp.getDriverClass(), "some.driver");
        assertEquals(sp.getProperty("binary.provider.filesystem.dir", ""), "a/b/c");
        assertEquals(sp.getProperty("empty", ""), "");
        assertEquals(sp.getProperty("emptySpaces", ""), "");
    }

    public void isDerby() throws IOException {
        StorageProperties sp = new StorageProperties(
                ResourceUtils.getResourceAsFile("/storage/minimalstorage.properties"));
        assertTrue(sp.isDerby());
        assertEquals(sp.getDbType(), DbType.DERBY);
    }

    public void isPostgres() throws IOException {
        StorageProperties sp = new StorageProperties(
                ResourceUtils.getResourceAsFile("/storage/storagepostgres.properties"));
        assertTrue(sp.isPostgres());
        assertEquals(sp.getDbType(), DbType.POSTGRESQL);
    }

}
