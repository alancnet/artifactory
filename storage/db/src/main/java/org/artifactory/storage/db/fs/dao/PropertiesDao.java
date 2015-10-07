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
import org.apache.commons.lang.StringUtils;
import org.artifactory.storage.db.fs.entity.NodeProperty;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A data access object for the properties table.
 *
 * @author Yossi Shaul
 */
@Repository
public class PropertiesDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(PropertiesDao.class);
    private static final int PROP_VALUE_MAX_SIZE = 4000;

    @Autowired
    public PropertiesDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public boolean hasNodeProperties(long nodeId) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT COUNT(1) FROM node_props WHERE node_id = ?", nodeId);
            if (resultSet.next()) {
                int propsCount = resultSet.getInt(1);
                return propsCount > 0;
            }
            return false;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public List<NodeProperty> getNodeProperties(long nodeId) throws SQLException {
        ResultSet resultSet = null;
        List<NodeProperty> results = Lists.newArrayList();
        try {
            // the child path must be the path+name of the parent
            resultSet = jdbcHelper.executeSelect("SELECT * FROM node_props WHERE node_id = ?", nodeId);
            while (resultSet.next()) {
                results.add(propertyFromResultSet(resultSet));
            }
            return results;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public int deleteNodeProperties(long nodeId) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM node_props WHERE node_id = ?", nodeId);
    }

    public int create(NodeProperty property) throws SQLException {
        String propValue = nullIfEmpty(property.getPropValue());
        if (propValue != null && propValue.length() > PROP_VALUE_MAX_SIZE) {
            log.info("Trimming property value to 4000 characters '{}'", property.getPropKey());
            log.debug("Trimming property value to 4000 characters {}: {}", property.getPropKey(),
                    property.getPropValue());
            propValue = StringUtils.substring(propValue, 0, PROP_VALUE_MAX_SIZE);
        }
        return jdbcHelper.executeUpdate("INSERT INTO node_props VALUES(?, ?, ?, ?)",
                property.getPropId(), property.getNodeId(), property.getPropKey(), propValue);
    }

    private NodeProperty propertyFromResultSet(ResultSet resultSet) throws SQLException {
        long propId = resultSet.getLong(1);
        long nodeId = resultSet.getLong(2);
        String propKey = resultSet.getString(3);
        String propValue = emptyIfNull(resultSet.getString(4));
        return new NodeProperty(propId, nodeId, propKey, propValue);
    }
}
