package org.artifactory.storage.db.base.itest.dao;

import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.properties.dao.DbPropertiesDao;
import org.artifactory.storage.db.properties.model.DbProperties;
import org.artifactory.storage.db.properties.utils.VersionPropertiesUtils;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.CompoundVersionDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.*;

/**
 * Date: 7/10/13 3:31 PM
 *
 * @author freds
 */
public class DbPropertiesDaoTest extends DbBaseTest {
    @Autowired
    private DbPropertiesDao dbPropertiesDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/db-props.sql");
    }


    public void loadExistingProps() throws SQLException {
        DbProperties dbProperties = dbPropertiesDao.getLatestProperties();
        assertNotNull(dbProperties);
        assertEquals(dbProperties.getInstallationDate(), 1349000000000L);
        assertEquals(dbProperties.getArtifactoryVersion(), "5-t");
        assertEquals(dbProperties.getArtifactoryRevision(), 12000);
        assertEquals(dbProperties.getArtifactoryRelease(), 1300000000000L);
    }

    @Test(dependsOnMethods = {"loadExistingProps"})
    public void createNewLatestProps() throws SQLException {
        long now = System.currentTimeMillis() - 10000L;
        DbProperties dbTest = new DbProperties(now, "6-a", 1, 2L);
        dbPropertiesDao.createProperties(dbTest);
        DbProperties dbProperties = dbPropertiesDao.getLatestProperties();
        assertNotNull(dbProperties);
        assertEquals(dbProperties.getInstallationDate(), now);
        assertEquals(dbProperties.getArtifactoryVersion(), "6-a");
        assertEquals(dbProperties.getArtifactoryRevision(), 1);
        assertEquals(dbProperties.getArtifactoryRelease(), 2L);
    }

    @Test(dependsOnMethods = {"createNewLatestProps"})
    public void createNewDevModeProps() throws SQLException {
        long now = System.currentTimeMillis();
        DbProperties dbTest = new DbProperties(now, "7-dev", -1, -3L);
        dbPropertiesDao.createProperties(dbTest);
        DbProperties dbProperties = dbPropertiesDao.getLatestProperties();
        assertNotNull(dbProperties);
        assertEquals(dbProperties.getInstallationDate(), now);
        assertEquals(dbProperties.getArtifactoryVersion(), "7-dev");
        assertEquals(dbProperties.getArtifactoryRevision(), 0);
        assertEquals(dbProperties.getArtifactoryRelease(), 0L);
    }

    @Test(dependsOnMethods = {"createNewDevModeProps"})
    public void createNewLatestReleaseProps() throws SQLException, InterruptedException {
        long now = System.currentTimeMillis();
        ArtifactoryVersion current = ArtifactoryVersion.getCurrent();
        Thread.sleep(100);
        DbProperties fromVersion = VersionPropertiesUtils.createDbPropertiesFromVersion(new CompoundVersionDetails(
                current, current.getValue(), "" + current.getRevision(), now - 1000000L));
        dbPropertiesDao.createProperties(fromVersion);
        DbProperties dbProperties = dbPropertiesDao.getLatestProperties();
        assertNotNull(dbProperties);
        assertTrue(now <= fromVersion.getInstallationDate());
        assertEquals(dbProperties.getInstallationDate(), fromVersion.getInstallationDate());
        assertEquals(dbProperties.getArtifactoryVersion(), current.getValue());
        assertEquals(dbProperties.getArtifactoryRevision(), 0);
        assertEquals(dbProperties.getArtifactoryRelease(), now - 1000000L);
    }

}
