package org.artifactory.storage.db.base.entity;

import org.artifactory.storage.db.properties.model.DbProperties;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Date: 7/10/13 3:17 PM
 *
 * @author freds
 */
@Test
public class DbPropertiesTest {
    public void basicDbProperties() {
        DbProperties test = new DbProperties(1L, "3.0.1-test", 2, 3L);
        assertEquals(test.getInstallationDate(), 1L);
        assertEquals(test.getArtifactoryVersion(), "3.0.1-test");
        assertEquals(test.getArtifactoryRevision(), 2);
        assertEquals(test.getArtifactoryRelease(), 3L);
    }

    public void maxNullDbProperties() {
        DbProperties test = new DbProperties(2L, "2-t", 0, 0L);
        assertEquals(test.getInstallationDate(), 2L);
        assertEquals(test.getArtifactoryVersion(), "2-t");
        assertEquals(test.getArtifactoryRevision(), 0);
        assertEquals(test.getArtifactoryRelease(), 0L);
    }

    public void maxNegDbProperties() {
        DbProperties test = new DbProperties(3L, "3-t", -2, -3L);
        assertEquals(test.getInstallationDate(), 3L);
        assertEquals(test.getArtifactoryVersion(), "3-t");
        assertEquals(test.getArtifactoryRevision(), -2);
        assertEquals(test.getArtifactoryRelease(), -3L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*Installation date.*cannot.*zero.*")
    public void nullInstallDateDbProperties() {
        new DbProperties(0L, "3.0.1-test", 2, 3L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*Installation date.*cannot.*negative.*")
    public void negInstallDateDbProperties() {
        new DbProperties(-1L, "3.0.1-test", 2, 3L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*version.*cannot.*null.*")
    public void nullArtVersionDbProperties() {
        new DbProperties(1L, null, 2, 3L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*version.*cannot.*empty.*")
    public void emptyArtVersionDbProperties() {
        new DbProperties(1L, " ", 2, 3L);
    }

}
