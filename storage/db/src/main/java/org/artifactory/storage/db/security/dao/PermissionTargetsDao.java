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

package org.artifactory.storage.db.security.dao;

import org.artifactory.storage.db.security.entity.PermissionTarget;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Date: 9/3/12
 * Time: 1:12 PM
 *
 * @author freds
 */
@Repository
public class PermissionTargetsDao extends BaseDao {

    @Autowired
    public PermissionTargetsDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public int createPermissionTarget(PermissionTarget permTarget) throws SQLException {
        int res = jdbcHelper.executeUpdate("INSERT INTO permission_targets VALUES(" +
                " ?, ?," +
                " ?, ?)",
                permTarget.getPermTargetId(), permTarget.getName(),
                permTarget.getIncludesPattern(), permTarget.getExcludesPattern());
        for (String repoKey : permTarget.getRepoKeys()) {
            res += jdbcHelper.executeUpdate("INSERT INTO permission_target_repos VALUES(?,?)",
                    permTarget.getPermTargetId(), repoKey);
        }
        return res;
    }

    public int updatePermissionTarget(PermissionTarget permTarget) throws SQLException {
        int res = jdbcHelper.executeUpdate("UPDATE permission_targets SET" +
                " perm_target_name = ?, includes = ?, excludes = ?" +
                " WHERE perm_target_id = ?",
                permTarget.getName(), permTarget.getIncludesPattern(), permTarget.getExcludesPattern(),
                permTarget.getPermTargetId());
        jdbcHelper.executeUpdate("DELETE FROM permission_target_repos WHERE perm_target_id = ?",
                permTarget.getPermTargetId());
        for (String repoKey : permTarget.getRepoKeys()) {
            res += jdbcHelper.executeUpdate("INSERT INTO permission_target_repos VALUES(?,?)",
                    permTarget.getPermTargetId(), repoKey);
        }
        return res;
    }

    public int deletePermissionTarget(long permTargetId) throws SQLException {
        int res = jdbcHelper.executeUpdate("DELETE FROM permission_target_repos WHERE perm_target_id = ?",
                permTargetId);
        res += jdbcHelper.executeUpdate("DELETE FROM permission_targets WHERE perm_target_id = ?", permTargetId);
        return res;
    }

    public PermissionTarget findPermissionTarget(String permTargetName) throws SQLException {
        ResultSet resultSet = null;
        PermissionTarget permTarget = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM permission_targets WHERE perm_target_name = ?",
                    permTargetName);
            if (resultSet.next()) {
                permTarget = resultSetToPermissionTarget(resultSet);
            }
        } finally {
            DbUtils.close(resultSet);
        }
        if (permTarget != null) {
            permTarget.setRepoKeys(findRepoKeysForTarget(permTarget.getPermTargetId()));
        }
        return permTarget;
    }

    public PermissionTarget findPermissionTarget(long permTargetId) throws SQLException {
        ResultSet resultSet = null;
        PermissionTarget permTarget = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM permission_targets WHERE perm_target_id = ?",
                    permTargetId);
            if (resultSet.next()) {
                permTarget = resultSetToPermissionTarget(resultSet);
            }
        } finally {
            DbUtils.close(resultSet);
        }
        if (permTarget != null) {
            permTarget.setRepoKeys(findRepoKeysForTarget(permTargetId));
        }
        return permTarget;
    }

    private HashSet<String> findRepoKeysForTarget(long permTargetId) throws SQLException {
        HashSet<String> repoKeys;
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect(
                    "SELECT repo_key FROM permission_target_repos WHERE perm_target_id = ?",
                    permTargetId);
            repoKeys = new HashSet<>(3);
            while (resultSet.next()) {
                repoKeys.add(resultSet.getString(1));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return repoKeys;
    }

    private PermissionTarget resultSetToPermissionTarget(ResultSet resultSet) throws SQLException {
        PermissionTarget permTarget;
        permTarget = new PermissionTarget(
                resultSet.getLong(1), resultSet.getString(2),
                resultSet.getString(3), resultSet.getString(4));
        return permTarget;
    }

    public int deleteAllPermissionTargets() throws SQLException {
        int res = jdbcHelper.executeUpdate("DELETE FROM permission_target_repos");
        res += jdbcHelper.executeUpdate("DELETE FROM permission_targets");
        return res;
    }
}
