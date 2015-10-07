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

package org.artifactory.storage.db.ha.itest.dao;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.servers.dao.ArtifactoryServersDao;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;
import org.fest.assertions.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.*;

/**
 * Tests the {@link org.artifactory.storage.db.servers.dao.ArtifactoryServersDao}.
 *
 * @author Yossi Shaul
 */
@Test
public class ArtifactoryServersDaoTest extends DbBaseTest {

    private static final String LICENSE_HASH = DigestUtils.shaHex("server") + "0";
    private static final String SERVER_ID = DigestUtils.shaHex("id") + "0";
    @Autowired
    private ArtifactoryServersDao dao;

    private final ArtifactoryServer sampleServer = new ArtifactoryServer(SERVER_ID, 1000000L,
            "127.0.0.1", 5700, ArtifactoryServerState.RUNNING, ArtifactoryServerRole.STANDALONE,
            System.currentTimeMillis(),
            "3.0.1-test", 2, 3L, ArtifactoryRunningMode.OSS, LICENSE_HASH);

    @BeforeClass
    public void createServer() throws SQLException {
        dao.createArtifactoryServer(sampleServer);
    }

    @Test()
    public void hasServer() throws SQLException {
        assertTrue(dao.hasArtifactoryServer(SERVER_ID));
    }

    public void hasServerNonExistingServer() throws SQLException {
        assertFalse(dao.hasArtifactoryServer(DigestUtils.shaHex("nosuchserver")));
    }

    public void loadServer() throws SQLException {
        ArtifactoryServer insertedServer = new ArtifactoryServer(DigestUtils.shaHex("create'n'load"), 1000000L,
                "152.45.32.56", 5700, ArtifactoryServerState.RUNNING, ArtifactoryServerRole.STANDALONE,
                System.currentTimeMillis(), "6.1", 2, 3L, ArtifactoryRunningMode.OSS, LICENSE_HASH);

        dao.createArtifactoryServer(insertedServer);

        ArtifactoryServer loadedServer = dao.getArtifactoryServer(DigestUtils.shaHex("create'n'load"));

        assertTrue(EqualsBuilder.reflectionEquals(insertedServer, loadedServer), "Orig and copy differ");
    }

    @Test()
    public void loadAllServers() throws SQLException {
        Assertions.assertThat(dao.getAllArtifactoryServers()).isNotEmpty().contains(sampleServer);
    }

    public void updateServer() throws SQLException {
        ArtifactoryServer server = new ArtifactoryServer(DigestUtils.shaHex("toupdate"), 1000000L,
                "152.45.32.56", 5700, ArtifactoryServerState.RUNNING, ArtifactoryServerRole.PRIMARY,
                System.currentTimeMillis(), "6.1", 2, 3L, ArtifactoryRunningMode.OSS, LICENSE_HASH);
        dao.createArtifactoryServer(server);

        ArtifactoryServer updatedServer = new ArtifactoryServer(DigestUtils.shaHex("toupdate"), 11000000L,
                "152.45.32.57", 5700, ArtifactoryServerState.STOPPED, ArtifactoryServerRole.STANDALONE,
                System.currentTimeMillis(), "6.2", 3, 2L, ArtifactoryRunningMode.OSS, LICENSE_HASH);

        int updateCount = dao.updateArtifactoryServer(updatedServer);
        assertEquals(updateCount, 1);

        ArtifactoryServer serverFromDb = dao.getArtifactoryServer(server.getServerId());
        assertTrue(EqualsBuilder.reflectionEquals(updatedServer, serverFromDb));
    }
}
