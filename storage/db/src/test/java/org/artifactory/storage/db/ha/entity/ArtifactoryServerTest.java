package org.artifactory.storage.db.ha.entity;

import org.apache.commons.codec.digest.DigestUtils;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Date: 7/10/13 3:17 PM
 *
 * @author freds
 */
@Test
public class ArtifactoryServerTest {

    public static final String SERVER_ID = DigestUtils.shaHex("test");
    private static final String LICENSE_HASH = "aaffsdgdfgdffgdgddfgdfgdfgdfgdfgsvfsawui0";

    public void basicArtifactoryServer() {
        long lastHeartbeat = System.currentTimeMillis();
        ArtifactoryServer test = new ArtifactoryServer(
                SERVER_ID, 1000000L, "127.0.0.1:8080",
                5700, ArtifactoryServerState.RUNNING, ArtifactoryServerRole.PRIMARY,
                lastHeartbeat,
                "3.0.1-test", 2, 3L, ArtifactoryRunningMode.OSS, LICENSE_HASH);
        assertEquals(test.getServerId(), SERVER_ID);
        assertEquals(test.getStartTime(), 1000000L);
        assertEquals(test.getContextUrl(), "127.0.0.1:8080");
        assertEquals(test.getServerState(), ArtifactoryServerState.RUNNING);
        assertEquals(test.getServerRole(), ArtifactoryServerRole.PRIMARY);
        assertEquals(test.getLastHeartbeat(), lastHeartbeat);
        assertEquals(test.getArtifactoryVersion(), "3.0.1-test");
        assertEquals(test.getArtifactoryRevision(), 2);
        assertEquals(test.getArtifactoryRelease(), 3L);
        assertEquals(test.getArtifactoryRunningMode(), ArtifactoryRunningMode.OSS);
    }

    public void maxNullArtifactoryServer() {
        ArtifactoryServer test = new ArtifactoryServer(
                SERVER_ID, 1000000L, null,
                5700, ArtifactoryServerState.OFFLINE, ArtifactoryServerRole.STANDALONE,
                1L, "2-t", 0, 0L, ArtifactoryRunningMode.HA, LICENSE_HASH);
        assertEquals(test.getServerId(), SERVER_ID);
        assertEquals(test.getStartTime(), 1000000L);
        assertNull(test.getContextUrl());
        assertEquals(test.getServerState(), ArtifactoryServerState.OFFLINE);
        assertEquals(test.getServerRole(), ArtifactoryServerRole.STANDALONE);
        assertEquals(test.getLastHeartbeat(), 1L);
        assertEquals(test.getArtifactoryVersion(), "2-t");
        assertEquals(test.getArtifactoryRevision(), 0);
        assertEquals(test.getArtifactoryRelease(), 0L);
        assertEquals(test.getArtifactoryRunningMode(), ArtifactoryRunningMode.HA);
    }

    public void maxNegArtifactoryServer() {
        ArtifactoryServer test = new ArtifactoryServer(
                SERVER_ID, 1000000L, null,
                5700, ArtifactoryServerState.UNKNOWN, ArtifactoryServerRole.COPY,
                1L, "3-t", -2, -3L, ArtifactoryRunningMode.OSS, LICENSE_HASH);
        assertEquals(test.getServerId(), SERVER_ID);
        assertEquals(test.getStartTime(), 1000000L);
        assertNull(test.getContextUrl());
        assertEquals(test.getServerState(), ArtifactoryServerState.UNKNOWN);
        assertEquals(test.getServerRole(), ArtifactoryServerRole.COPY);
        assertEquals(test.getLastHeartbeat(), 1L);
        assertEquals(test.getArtifactoryVersion(), "3-t");
        assertEquals(test.getArtifactoryRevision(), -2);
        assertEquals(test.getArtifactoryRelease(), -3L);
        assertEquals(test.getArtifactoryRunningMode(), ArtifactoryRunningMode.OSS);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*serverId.*cannot.*null.*")
    public void nullServerIdArtifactoryServer() {
        new ArtifactoryServer(
                null, 1000000L, "127.0.0.1:8080",
                5700, ArtifactoryServerState.RUNNING, ArtifactoryServerRole.PRIMARY,
                2L, "3.0.1-test", 2, 3L, ArtifactoryRunningMode.OSS, LICENSE_HASH);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*serverId.*cannot.*empty.*")
    public void emptyServerIdArtifactoryServer() {
        new ArtifactoryServer(
                " ", 1000000L, "127.0.0.1:8080",
                5700, ArtifactoryServerState.RUNNING, ArtifactoryServerRole.PRIMARY,
                2L, "3.0.1-test", 2, 3L, ArtifactoryRunningMode.OSS, LICENSE_HASH);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*start time.*cannot.*zero.*")
    public void zeroStartTimeArtifactoryServer() {
        new ArtifactoryServer(
                SERVER_ID, 0L, "127.0.0.1:8080",
                5700, ArtifactoryServerState.RUNNING, ArtifactoryServerRole.PRIMARY,
                2L, "3.0.1-test", 2, 3L, ArtifactoryRunningMode.OSS, LICENSE_HASH);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*start time.*cannot.*negative.*")
    public void negStartTimeArtifactoryServer() {
        new ArtifactoryServer(
                SERVER_ID, -3L, "127.0.0.1:8080",
                5700, ArtifactoryServerState.RUNNING, ArtifactoryServerRole.PRIMARY,
                2L, "3.0.1-test", 2, 3L, ArtifactoryRunningMode.OSS, LICENSE_HASH);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*server state.*cannot.*null.*")
    public void nullServerStateArtifactoryServer() {
        new ArtifactoryServer(
                SERVER_ID, 1000000L, "127.0.0.1:8080",
                5700, null, ArtifactoryServerRole.PRIMARY,
                2L, "3.0.1-test", 2, 3L, ArtifactoryRunningMode.OSS, LICENSE_HASH);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*role.*cannot.*null.*")
    public void nullServerRoleArtifactoryServer() {
        new ArtifactoryServer(
                SERVER_ID, 1000000L, "127.0.0.1:8080",
                5700, ArtifactoryServerState.UNKNOWN, null,
                2L, "3.0.1-test", 2, 3L, ArtifactoryRunningMode.OSS, LICENSE_HASH);
    }

}
