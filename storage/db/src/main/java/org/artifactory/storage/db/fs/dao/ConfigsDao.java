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

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.util.blob.BlobWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A data access object for configs table access.
 *
 * @author Yossi Shaul
 */
@Repository
public class ConfigsDao extends BaseDao {

    @Autowired
    public ConfigsDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public int createConfig(String name, String data) throws SQLException, UnsupportedEncodingException {
        BlobWrapper blobWrapper = new BlobWrapper(data);
        return createConfig(name, blobWrapper);
    }

    public int createConfig(String name, BlobWrapper blobWrapper) throws SQLException {
        return jdbcHelper.executeUpdate("INSERT INTO configs VALUES(?, ?)", name, blobWrapper);
    }

    public boolean hasConfig(String name) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT COUNT(1) FROM configs WHERE config_name = ?", name);
            if (resultSet.next()) {
                int propsCount = resultSet.getInt(1);
                return propsCount > 0;
            }
            return false;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public InputStream loadStreamConfig(String name) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT data FROM configs WHERE config_name = ?", name);
            if (resultSet.next()) {
                InputStream binaryStream = null;
                try {
                    binaryStream = resultSet.getBinaryStream(1);
                    return IOUtils.toBufferedInputStream(binaryStream);
                } finally {
                    IOUtils.closeQuietly(binaryStream);
                }
            }
            return null;
        } catch (IOException e) {
            throw new SQLException("Failed to read config '" + name + "' due to: " + e.getMessage(), e);
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public String loadConfig(String name) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT data FROM configs WHERE config_name = ?", name);
            if (resultSet.next()) {
                InputStream binaryStream = resultSet.getBinaryStream(1);
                return IOUtils.toString(binaryStream, Charsets.UTF_8.name());
            }
            return null;
        } catch (IOException e) {
            throw new SQLException("Failed to read config '" + name + "' due to: " + e.getMessage(), e);
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public int updateConfig(String name, String data) throws UnsupportedEncodingException, SQLException {
        BlobWrapper blobWrapper = new BlobWrapper(data);
        return updateConfig(name, blobWrapper);
    }

    public int updateConfig(String name, BlobWrapper blobWrapper) throws SQLException {
        return jdbcHelper.executeUpdate("UPDATE configs SET data = ? WHERE config_name = ?",
                blobWrapper, name);
    }

    public int deleteConfig(String name) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM configs WHERE config_name = ?", name);
    }
}
