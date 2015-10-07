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

package org.artifactory.storage.db.fs.dao;

import org.artifactory.storage.db.fs.entity.NodeMetaInfo;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A data access table for the node meta infos table.
 *
 * @author Yossi Shaul
 */
@Repository
public class NodeMetaInfoDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(NodeMetaInfoDao.class);

    @Autowired
    public NodeMetaInfoDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public boolean hasNodeMetadata(long nodeId) throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(1) FROM node_meta_infos WHERE node_id = ?", nodeId) > 0;
    }

    @Nullable
    public NodeMetaInfo getNodeMetadata(long nodeId) throws SQLException {
        ResultSet resultSet = null;
        try {
            // the child path must be the path+name of the parent
            resultSet = jdbcHelper.executeSelect("SELECT * FROM node_meta_infos WHERE node_id = ?", nodeId);
            if (resultSet.next()) {
                return metaInfosFromResultSet(resultSet);
            }
            return null;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public int deleteNodeMeta(long nodeId) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM node_meta_infos WHERE node_id = ?", nodeId);
    }

    public int create(NodeMetaInfo metaInfo) throws SQLException {
        return jdbcHelper.executeUpdate("INSERT INTO node_meta_infos VALUES(?, ?, ?)",
                metaInfo.getNodeId(), metaInfo.getPropsModified(), metaInfo.getPropsModifiedBy());
    }

    public int update(NodeMetaInfo metaInfo) throws SQLException {
        return jdbcHelper.executeUpdate("UPDATE node_meta_infos SET props_modified = ?, props_modified_by = ? " +
                "WHERE node_id = ?", metaInfo.getPropsModified(), metaInfo.getPropsModifiedBy(), metaInfo.getNodeId());
    }

    private NodeMetaInfo metaInfosFromResultSet(ResultSet resultSet) throws SQLException {
        long nodeId = resultSet.getLong(1);
        long propModified = resultSet.getLong(2);
        String propModifiedBy = resultSet.getString(3);
        return new NodeMetaInfo(nodeId, propModified, propModifiedBy);
    }

}
