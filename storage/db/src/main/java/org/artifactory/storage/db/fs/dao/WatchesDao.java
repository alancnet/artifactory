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

package org.artifactory.storage.db.fs.dao;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.fs.entity.Watch;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A data access object for the watches table.
 *
 * @author Yossi Shaul
 */
@Repository
public class WatchesDao extends BaseDao {

    @Autowired
    public WatchesDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public int create(Watch watch) throws SQLException {
        return jdbcHelper.executeUpdate("INSERT INTO watches VALUES(?, ?, ?, ?)",
                watch.getWatchId(), watch.getNodeId(), watch.getUsername(), watch.getSince());
    }

    public boolean hasWatches(long nodeId) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT COUNT(1) FROM watches WHERE node_id = ?", nodeId);
            if (resultSet.next()) {
                int propsCount = resultSet.getInt(1);
                return propsCount > 0;
            }
            return false;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public List<Watch> getWatches(long nodeId) throws SQLException {
        ResultSet resultSet = null;
        List<Watch> results = Lists.newArrayList();
        try {
            // the child path must be the path+name of the parent
            resultSet = jdbcHelper.executeSelect("SELECT * FROM watches WHERE node_id = ?", nodeId);
            while (resultSet.next()) {
                results.add(watchFromResultSet(resultSet));
            }
            return results;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public List<Watch> getWatches() throws SQLException {
        ResultSet resultSet = null;
        List<Watch> results = Lists.newArrayList();
        try {
            // the child path must be the path+name of the parent
            resultSet = jdbcHelper.executeSelect("SELECT * FROM watches");
            while (resultSet.next()) {
                results.add(watchFromResultSet(resultSet));
            }
            return results;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public int deleteWatches(long nodeId) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM watches WHERE node_id = ?", nodeId);
    }

    public int deleteUserWatches(long nodeId, String username) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM watches WHERE node_id = ? and username = ?",
                nodeId, username);
    }

    public int deleteAllUserWatches(String username) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM watches WHERE username = ?", username);
    }

    private Watch watchFromResultSet(ResultSet resultSet) throws SQLException {
        long watchId = resultSet.getLong(1);
        long nodeId = resultSet.getLong(2);
        String username = resultSet.getString(3);
        long since = resultSet.getLong(4);
        return new Watch(watchId, nodeId, username, since);
    }
}
