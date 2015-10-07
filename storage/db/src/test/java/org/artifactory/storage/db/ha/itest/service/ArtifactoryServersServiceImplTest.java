package org.artifactory.storage.db.ha.itest.service;

import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;
import org.artifactory.storage.db.servers.service.ArtifactoryServersServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Integration tests for {@link org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService}.
 *
 * @author Yossi Shaul
 */
@Test
public class ArtifactoryServersServiceImplTest extends DbBaseTest {

    @Autowired
    private ArtifactoryServersServiceImpl serversService;

    @BeforeClass
    public void setup() {
        importSql("/sql/ha.sql");
    }

    public void testAllServers() {
        List<ArtifactoryServer> allArtifactoryServers = serversService.getAllArtifactoryServers();
        assertEquals(allArtifactoryServers.size(), 3);
        for (int i = 1; i < 4; i++) {
            String serverId = "" + i + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0";
            ArtifactoryServer artifactoryServer = serversService.getArtifactoryServer(serverId);
            assertNotNull(artifactoryServer, "Server with ID " + serverId + " not found!");
            assertEquals(artifactoryServer.getStartTime(), 1234567890 + i);
            assertEquals(artifactoryServer.getContextUrl(), "10.0.0." + i + ":" + i);
            assertEquals(artifactoryServer.getLastHeartbeat(), 1234567890L + (long) i);
            assertEquals(artifactoryServer.getArtifactoryVersion(), "3.1.0_0" + i);
            assertEquals(artifactoryServer.getArtifactoryRevision(), 8844550 + i);
            assertEquals(artifactoryServer.getArtifactoryRelease(), 1230L + (long) i);
            assertEquals(artifactoryServer.getLicenseKeyHash(), "0123456789012345678901234567890123456789" + i);
            switch (i) {
                case 1:
                    assertEquals(artifactoryServer.getServerState(), ArtifactoryServerState.RUNNING);
                    assertEquals(artifactoryServer.getServerRole(), ArtifactoryServerRole.PRIMARY);
                    assertEquals(artifactoryServer.getArtifactoryRunningMode(), ArtifactoryRunningMode.HA);
                    break;
                case 2:
                    assertEquals(artifactoryServer.getServerState(), ArtifactoryServerState.STARTING);
                    assertEquals(artifactoryServer.getServerRole(), ArtifactoryServerRole.MEMBER);
                    assertEquals(artifactoryServer.getArtifactoryRunningMode(), ArtifactoryRunningMode.OSS);
                    break;
                case 3:
                    assertEquals(artifactoryServer.getServerState(), ArtifactoryServerState.STOPPING);
                    assertEquals(artifactoryServer.getServerRole(), ArtifactoryServerRole.COPY);
                    assertEquals(artifactoryServer.getArtifactoryRunningMode(), ArtifactoryRunningMode.PRO);
                    break;
                default:
                    fail("ID " + i + " does not exists!");
            }
        }
    }
}
