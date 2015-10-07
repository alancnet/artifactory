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

package org.artifactory.storage.db.build.dao;

import org.artifactory.checksum.ChecksumType;
import org.artifactory.storage.db.build.entity.BuildDependency;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 10/30/12
 * Time: 12:44 PM
 *
 * @author freds
 */
@Repository
public class BuildDependenciesDao extends BaseDao {

    @Autowired
    public BuildDependenciesDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public int createBuildDependencies(List<BuildDependency> bds) throws SQLException {
        int res = 0;
        for (BuildDependency bd : bds) {
            // TODO: Make it big single SQL
            res += createBuildDependency(bd);
        }
        return res;
    }

    public int createBuildDependency(BuildDependency bd) throws SQLException {
        return jdbcHelper.executeUpdate("INSERT INTO build_dependencies VALUES(" +
                "?, ?," +
                "?, ?, ?," +
                "?, ?)",
                bd.getDependencyId(), bd.getModuleId(),
                bd.getDependencyNameId(), bd.getDependencyScopes(), bd.getDependencyType(),
                bd.getSha1(), bd.getMd5());
    }

    public int deleteBuildDependencies(List<Long> moduleIds) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM build_dependencies WHERE module_id IN (#)", moduleIds);
    }

    public int deleteAllBuildDependencies() throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM build_dependencies");
    }

    public List<BuildDependency> findDependenciesForModules(List<Long> moduleIds) throws SQLException {
        ResultSet resultSet = null;
        List<BuildDependency> dependencies = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM build_dependencies WHERE module_id IN (#)", moduleIds);
            dependencies = new ArrayList<>();
            while (resultSet.next()) {
                dependencies.add(resultSetToBuildDependency(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return dependencies;
    }

    public List<BuildDependency> findDependenciesForModule(long moduleId) throws SQLException {
        ResultSet resultSet = null;
        List<BuildDependency> dependencies = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM build_dependencies WHERE module_id = ?", moduleId);
            dependencies = new ArrayList<>();
            while (resultSet.next()) {
                dependencies.add(resultSetToBuildDependency(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return dependencies;
    }

    public List<BuildDependency> findDependenciesForChecksum(ChecksumType type, String checksum)
            throws SQLException {
        ResultSet resultSet = null;
        List<BuildDependency> dependencies = null;
        try {
            resultSet = jdbcHelper.executeSelect(
                    "SELECT * FROM build_dependencies WHERE " + type.name() + " = ?", checksum);
            dependencies = new ArrayList<>();
            while (resultSet.next()) {
                dependencies.add(resultSetToBuildDependency(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return dependencies;
    }

    private BuildDependency resultSetToBuildDependency(ResultSet resultSet) throws SQLException {
        return new BuildDependency(resultSet.getLong(1), resultSet.getLong(2),
                resultSet.getString(3), resultSet.getString(4), resultSet.getString(5),
                resultSet.getString(6), resultSet.getString(7));
    }
}
