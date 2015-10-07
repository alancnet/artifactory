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
import org.artifactory.storage.db.build.entity.BuildArtifact;
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
public class BuildArtifactsDao extends BaseDao {

    @Autowired
    public BuildArtifactsDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public int createBuildArtifacts(List<BuildArtifact> bas) throws SQLException {
        int res = 0;
        for (BuildArtifact ba : bas) {
            // TODO: Make it big single SQL
            res += createBuildArtifact(ba);
        }
        return res;
    }

    public int createBuildArtifact(BuildArtifact ba) throws SQLException {
        return jdbcHelper.executeUpdate("INSERT INTO build_artifacts VALUES(" +
                "?, ?," +
                "?, ?," +
                "?, ?)",
                ba.getArtifactId(), ba.getModuleId(),
                ba.getArtifactName(), ba.getArtifactType(),
                ba.getSha1(), ba.getMd5());
    }

    public int deleteBuildArtifacts(List<Long> moduleIds) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM build_artifacts WHERE module_id IN (#)", moduleIds);
    }

    public int deleteAllBuildArtifacts() throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM build_artifacts");
    }

    public List<BuildArtifact> findArtifactsForModules(List<Long> moduleIds) throws SQLException {
        ResultSet rs = null;
        List<BuildArtifact> artifacts = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT * FROM build_artifacts WHERE module_id IN (#)", moduleIds);
            artifacts = new ArrayList<>();
            while (rs.next()) {
                artifacts.add(resultSetToBuildArtifact(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        return artifacts;
    }

    public List<BuildArtifact> findArtifactsForModule(long moduleId) throws SQLException {
        ResultSet resultSet = null;
        List<BuildArtifact> artifacts = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM build_artifacts WHERE module_id = ?", moduleId);
            artifacts = new ArrayList<>();
            while (resultSet.next()) {
                artifacts.add(resultSetToBuildArtifact(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return artifacts;
    }

    public List<BuildArtifact> findArtifactsForChecksum(ChecksumType type, String checksum)
            throws SQLException {
        ResultSet resultSet = null;
        List<BuildArtifact> artifacts = null;
        try {
            resultSet = jdbcHelper.executeSelect(
                    "SELECT * FROM build_artifacts WHERE " + type.name() + " = ?", checksum);
            artifacts = new ArrayList<>();
            while (resultSet.next()) {
                artifacts.add(resultSetToBuildArtifact(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return artifacts;
    }

    private BuildArtifact resultSetToBuildArtifact(ResultSet resultSet) throws SQLException {
        return new BuildArtifact(resultSet.getLong(1), resultSet.getLong(2),
                resultSet.getString(3), resultSet.getString(4),
                resultSet.getString(5), resultSet.getString(6));
    }

}
