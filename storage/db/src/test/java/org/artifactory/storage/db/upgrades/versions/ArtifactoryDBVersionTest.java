package org.artifactory.storage.db.upgrades.versions;

import org.apache.commons.lang.RandomStringUtils;
import org.artifactory.storage.db.DbType;
import org.artifactory.storage.db.itest.DbTestUtils;
import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.artifactory.storage.db.version.ArtifactoryDBVersion;
import org.artifactory.version.ArtifactoryVersion;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.artifactory.storage.db.version.ArtifactoryDBVersion.convert;
import static org.artifactory.storage.db.version.ArtifactoryDBVersion.v100;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Author: gidis
 */
public class ArtifactoryDBVersionTest extends UpgradeBaseTest {

    @BeforeClass
    @Override
    protected void springTestContextPrepareTestInstance() throws Exception {
        super.springTestContextPrepareTestInstance();

        rollBackTo300Version();
        convert(getFromVersion(v100), jdbcHelper, storageProperties.getDbType());
    }

    public void test300DBChanges() throws IOException, SQLException {
        // Now the DB is like in 3.0.x, should be missing the new tables of 3.1.x
        try (Connection connection = jdbcHelper.getDataSource().getConnection()) {
            assertFalse(DbTestUtils.isTableMissing(connection));
        }
    }

    public void test311DBChanges() throws IOException, SQLException {
        try (Connection connection = jdbcHelper.getDataSource().getConnection()) {
            assertEquals(DbTestUtils.getColumnSize(connection, "node_props", "prop_value"), 4000);
            if (storageProperties.getDbType() == DbType.MSSQL) {
                return; // RTFACT-5768
            }
            jdbcHelper.executeUpdate("INSERT INTO node_props VALUES(?, ?, ?, ?)",
                    15, 15, "longProp", RandomStringUtils.randomAscii(3999));
        }
    }

    private ArtifactoryVersion getFromVersion(ArtifactoryDBVersion version) {
        return version.getComparator().getFrom();
    }
}
