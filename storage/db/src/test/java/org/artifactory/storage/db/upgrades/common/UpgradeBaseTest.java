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

package org.artifactory.storage.db.upgrades.common;

import ch.qos.logback.classic.util.ContextInitializer;
import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.db.DbType;
import org.artifactory.storage.db.itest.DbTestUtils;
import org.artifactory.storage.db.spring.ArtifactoryDataSource;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import static org.testng.Assert.assertEquals;

/**
 * Date: 8/5/13 9:58 AM
 *
 * @author freds
 */
@Test(groups = "dbtest-upgrade")
@ContextConfiguration(locations = {"classpath:spring/db-upgrade-test-context.xml"})
public abstract class UpgradeBaseTest extends AbstractTestNGSpringContextTests {
    @Autowired
    protected JdbcHelper jdbcHelper;

    @Autowired
    @Qualifier("storageProperties")
    protected StorageProperties storageProperties;

    private ArtifactoryHomeBoundTest artifactoryHomeBoundTest;

    static {
        // use the itest logback config
        URL url = UpgradeBaseTest.class.getClassLoader().getResource("logback-dbtest.xml");
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, url.getPath());
    }

    protected static InputStream getPreviousDbSchemaSql(String previousVersion, DbType dbType) {
        String dbConfigDir = dbType.toString();
        return ResourceUtils.getResource(
                "/upgrades/" + previousVersion + "/ddl/" + dbConfigDir + "/" + dbConfigDir + ".sql");
    }

    protected static InputStream getPreviousImportSql(String previousVersion, String dataFileName) {
        return ResourceUtils.getResource(
                "/upgrades/" + previousVersion + "/data/" + dataFileName + ".sql");
    }

    protected static InputStream getDbSchemaUpgradeSql(String forVersion, DbType dbType) {
        String dbConfigDir = dbType.toString();
        return ResourceUtils.getResource(
                "/conversion/" + dbConfigDir + "/" + dbConfigDir + "_" + forVersion + ".sql");
    }

    @BeforeClass
    @Override
    protected void springTestContextPrepareTestInstance() throws Exception {
        artifactoryHomeBoundTest = createArtifactoryHomeTest();
        artifactoryHomeBoundTest.bindArtifactoryHome();

        super.springTestContextPrepareTestInstance();
    }

    protected ArtifactoryHomeBoundTest createArtifactoryHomeTest() throws IOException {
        return new ArtifactoryHomeBoundTest();
    }

    @AfterMethod
    protected void verifyDbResourcesReleased() throws IOException, SQLException {
        // make sure there are no active connections
        ArtifactoryDataSource ds = (ArtifactoryDataSource) jdbcHelper.getDataSource();
        assertEquals(ds.getActiveConnectionsCount(), 0, "Found " + ds.getActiveConnectionsCount() +
                " active connections after test ended");
        artifactoryHomeBoundTest.unbindArtifactoryHome();
    }

    @BeforeMethod
    public void bindArtifactoryHome() {
        artifactoryHomeBoundTest.bindArtifactoryHome();
    }

    @AfterMethod
    public void unbindArtifactoryHome() {
        artifactoryHomeBoundTest.unbindArtifactoryHome();
    }

    public void rollBackTo300Version() throws SQLException, IOException {
        try (Connection connection = jdbcHelper.getDataSource().getConnection()) {
            // Making DB looks like v300 with data
            DbTestUtils.dropAllExistingTables(connection);
            DbUtils.executeSqlStream(connection, getPreviousDbSchemaSql("v300", storageProperties.getDbType()));
            DbUtils.executeSqlStream(connection, getPreviousImportSql("v300", "user-group"));
            DbUtils.executeSqlStream(connection, getPreviousImportSql("v300", "acls"));
            DbUtils.executeSqlStream(connection, getPreviousImportSql("v300", "builds"));
            DbUtils.executeSqlStream(connection, getPreviousImportSql("v300", "nodes"));
        }
    }
    protected void importSql(String resourcePath) {
        InputStream resource = ResourceUtils.getResource(resourcePath);
        Connection con = null;
        try {
            con = jdbcHelper.getDataSource().getConnection();
            DbUtils.executeSqlStream(con, resource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            DbUtils.close(con);
        }
    }
}
