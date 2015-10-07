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

package org.artifactory.storage.db.servers.dao;

import com.google.common.collect.Lists;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A data access object for artifactory servers table access.
 *
 * @author Yossi Shaul
 */
@Repository
public class ArtifactoryServersDao extends BaseDao {

    @Autowired
    public ArtifactoryServersDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public int createArtifactoryServer(ArtifactoryServer s) throws SQLException {
        return jdbcHelper.executeUpdate(
                "INSERT INTO artifactory_servers VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                s.getServerId(), s.getStartTime(), s.getContextUrl(), s.getMembershipPort(),
                enumToString(s.getServerState()), enumToString(s.getServerRole()),
                s.getLastHeartbeat(), s.getArtifactoryVersion(),
                s.getArtifactoryRevision(), s.getArtifactoryRelease(),
                enumToString(s.getArtifactoryRunningMode()), s.getLicenseKeyHash());
    }

    public boolean hasArtifactoryServer(String serverId) throws SQLException {
        return getArtifactoryServer(serverId) != null;
    }

    @Nullable
    public ArtifactoryServer getArtifactoryServer(String serverId) throws SQLException {
        ResultSet resultSet = null;
        ArtifactoryServer server = null;
        try {
            resultSet = jdbcHelper.executeSelect(
                    "SELECT * FROM artifactory_servers WHERE server_id = ?", serverId);
            if (resultSet.next()) {
                server = serverFromResultSet(resultSet);
            }
            return server;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public List<ArtifactoryServer> getAllArtifactoryServers() throws SQLException {
        ResultSet resultSet = null;
        List<ArtifactoryServer> servers = Lists.newArrayList();
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM artifactory_servers");
            while (resultSet.next()) {
                servers.add(serverFromResultSet(resultSet));
            }
            return servers;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    /**
     * Update existing {@link ArtifactoryServer}
     * <p>Does not update the {@code membership_port}
     * */
    public int updateArtifactoryServer(ArtifactoryServer s) throws SQLException {
        return jdbcHelper.executeUpdate("UPDATE artifactory_servers " +
                "SET start_time = ?, context_url = ?, " +
                "server_state = ?, server_role = ?, " +
                "last_heartbeat = ?, artifactory_version = ?, " +
                "artifactory_revision = ?, artifactory_release = ?, " +
                "artifactory_running_mode = ?, license_hash=? WHERE server_id = ?",
                s.getStartTime(), s.getContextUrl(),
                enumToString(s.getServerState()), enumToString(s.getServerRole()),
                s.getLastHeartbeat(), s.getArtifactoryVersion(),
                s.getArtifactoryRevision(), s.getArtifactoryRelease(),
                enumToString(s.getArtifactoryRunningMode()), s.getLicenseKeyHash(), s.getServerId());
    }

    public int updateArtifactoryServerHeartbeat(String serverId, long lastHeartbeat) throws SQLException {
        return jdbcHelper.executeUpdate("UPDATE artifactory_servers SET last_heartbeat = ? WHERE server_id = ?",
                lastHeartbeat, serverId);
    }

    public int updateArtifactoryServerState(String serverId, ArtifactoryServerState serverState, long heartbeat)
            throws SQLException {
        return jdbcHelper.executeUpdate(
                "UPDATE artifactory_servers SET server_state = ?, last_heartbeat = ? WHERE server_id = ?",
                enumToString(serverState), heartbeat, serverId);
    }

    public int updateArtifactoryServerRole(String serverId, ArtifactoryServerRole serverRole) throws SQLException {
        return jdbcHelper.executeUpdate("UPDATE artifactory_servers SET server_role = ? WHERE server_id = ?",
                enumToString(serverRole), serverId);
    }

    public int updateArtifactoryMembershipPort(String serverId, int membershipPort) throws SQLException {
        return jdbcHelper.executeUpdate("UPDATE artifactory_servers SET membership_port = ? WHERE server_id = ?",
                membershipPort, serverId);
    }

    public boolean removeServer(String serverId) throws SQLException {
        int deleted = jdbcHelper.executeUpdate("DELETE FROM artifactory_servers WHERE server_id = ?", serverId);
        return deleted > 0;
    }

    private ArtifactoryServer serverFromResultSet(ResultSet rs) throws SQLException {
        return new ArtifactoryServer(rs.getString(1), rs.getLong(2), rs.getString(3), rs.getInt(4),
                ArtifactoryServerState.fromString(rs.getString(5)), ArtifactoryServerRole.fromString(rs.getString(6)),
                rs.getLong(7), rs.getString(8), rs.getInt(9), rs.getLong(10),
                ArtifactoryRunningMode.fromString(rs.getString(11)), rs.getString(12));
    }
}
