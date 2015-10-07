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
import com.google.common.collect.Sets;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ConstantValues;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.fs.entity.Node;
import org.artifactory.storage.db.fs.entity.NodePath;
import org.artifactory.storage.db.fs.util.NodeUtils;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.fs.repo.RepoStorageSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A data access object for node table access.
 *
 * @author Yossi Shaul
 */
@Repository
public class NodesDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(NodesDao.class);

    public static final String TABLE_NAME = "nodes";
    private static final String SELECT_NODE_QUERY = "SELECT * FROM nodes ";

    @Autowired
    public NodesDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    @Nullable
    public Node get(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        Node node = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM nodes " +
                    "WHERE repo = ? AND node_path = ? AND node_name = ?",
                    path.getRepo(), dotIfNullOrEmpty(path.getPath()), dotIfNullOrEmpty(path.getName()));
            if (resultSet.next()) {
                node = nodeFromResultSet(resultSet);
            }
            return node;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    @Nullable
    public Node get(long id) throws SQLException {
        ResultSet resultSet = null;
        Node node = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM nodes WHERE node_id = ?", id);
            if (resultSet.next()) {
                node = nodeFromResultSet(resultSet);
            }
            return node;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public long getNodeId(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT node_id FROM nodes " +
                    "WHERE repo = ? AND node_path = ? AND node_name = ?",
                    path.getRepo(), dotIfNullOrEmpty(path.getPath()), dotIfNullOrEmpty(path.getName()));
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return DbService.NO_DB_ID;
            }
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public long getFileNodeId(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT node_id FROM nodes " +
                    "WHERE node_type=1 AND repo = ? AND node_path = ? AND node_name = ?",
                    path.getRepo(), dotIfNullOrEmpty(path.getPath()), dotIfNullOrEmpty(path.getName()));
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return DbService.NO_DB_ID;
            }
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public String getNodeSha1(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        String sha1 = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT sha1_actual FROM nodes " +
                    "WHERE repo = ? AND node_path = ? AND node_name = ?",
                    path.getRepo(), dotIfNullOrEmpty(path.getPath()), dotIfNullOrEmpty(path.getName()));
            if (resultSet.next()) {
                sha1 = resultSet.getString(1);
            }
            return sha1;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public int create(Node node) throws SQLException {
        return jdbcHelper.executeUpdate("INSERT INTO nodes VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                node.getNodeId(), booleanAsByte(node.isFile()), node.getRepo(),
                dotIfNullOrEmpty(node.getPath()), dotIfNullOrEmpty(node.getName()),
                node.getDepth(), node.getCreated(), node.getCreatedBy(), node.getModified(), node.getModifiedBy(),
                node.getUpdated(), node.getLength(), node.getSha1Actual(), node.getSha1Original(), node.getMd5Actual(),
                node.getMd5Original());
    }

    public int update(Node node) throws SQLException {
        // node id and type are not updatable
        return jdbcHelper.executeUpdate("UPDATE nodes " +
                "SET repo = ?,  node_path = ?, node_name = ?, " +
                "depth = ?, created = ?, created_by = ?, " +
                "modified = ?, modified_by = ?, updated = ?, " +
                "bin_length = ?, sha1_actual = ?, sha1_original = ?, md5_actual = ?, md5_original = ? " +
                "WHERE node_id = ?",
                node.getRepo(), dotIfNullOrEmpty(node.getPath()), dotIfNullOrEmpty(node.getName()),
                node.getDepth(), node.getCreated(), node.getCreatedBy(),
                node.getModified(), node.getModifiedBy(), node.getUpdated(),
                node.getLength(), node.getSha1Actual(), node.getSha1Original(), node.getMd5Actual(),
                node.getMd5Original(), node.getNodeId());
    }

    public boolean exists(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT count(*) FROM nodes " +
                    "WHERE repo = ? AND node_path = ? AND node_name = ?",
                    path.getRepo(), dotIfNullOrEmpty(path.getPath()), dotIfNullOrEmpty(path.getName()));
            int count = 0;
            if (resultSet.next()) {
                count = resultSet.getInt(1);
                if (count > 1) {
                    if (log.isDebugEnabled()) {
                        StorageException bigWarning = new StorageException(
                                "Unexpected node count for absolute path: '" + path + "' - " + count);
                        log.warn(bigWarning.getMessage(), bigWarning);
                    } else {
                        log.warn("Unexpected node count for absolute path: '{}' - {}", path, count);
                    }
                }
            }
            return count > 0;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public boolean delete(long id) throws SQLException {
        int deleted = jdbcHelper.executeUpdate("DELETE FROM nodes WHERE node_id = ?", id);
        return deleted > 0;
    }

    public List<Node> getChildren(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        List<Node> results = Lists.newArrayList();
        try {
            // the child path must be the path+name of the parent
            String childPath = path.getPathName();
            resultSet = jdbcHelper.executeSelect(SELECT_NODE_QUERY +
                    "WHERE repo = ? AND node_path = ? AND depth = ?",
                    path.getRepo(), dotIfNullOrEmpty(childPath), path.getDepth() + 1);
            while (resultSet.next()) {
                results.add(nodeFromResultSet(resultSet));
            }
            return results;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public boolean hasChildren(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        try {
            // the child path must be the path+name of the parent
            String childPath = path.getPathName();
            resultSet = jdbcHelper.executeSelect("SELECT COUNT(1) FROM nodes " +
                    "WHERE repo = ? AND node_path like ? AND depth = ?",
                    path.getRepo(), dotIfNullOrEmpty(childPath), path.getDepth() + 1);
            if (resultSet.next()) {
                int childrenCount = resultSet.getInt(1);
                log.trace("Children count of '{}': {}", path, childrenCount);
                return childrenCount > 0;
            }
            return false;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public List<? extends Node> getAllNodes() throws SQLException {
        if (!ConstantValues.dev.getBoolean()) {
            return Lists.newArrayList();
        }

        ResultSet resultSet = null;
        List<Node> results = Lists.newArrayList();
        try {
            resultSet = jdbcHelper.executeSelect(SELECT_NODE_QUERY);
            while (resultSet.next()) {
                results.add(nodeFromResultSet(resultSet));
            }
            return results;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public List<? extends Node> getChildrenRecursively(String path) throws SQLException {
        ResultSet resultSet = null;
        List<Node> results = Lists.newArrayList();
        int childDepth = NodeUtils.getDepth(path);
        if (!path.endsWith("/")) {
            path += "/";
        }
        try {
            resultSet = jdbcHelper.executeSelect(SELECT_NODE_QUERY +
                    "WHERE path like '" + path + "%' AND depth >= " + childDepth);
            while (resultSet.next()) {
                results.add(nodeFromResultSet(resultSet));
            }
            return results;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public int getFilesCount() throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM nodes WHERE node_type = 1");
    }

    public int getFilesCount(String repoKey) throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM nodes WHERE node_type=1 and repo = ?", repoKey);
    }

    public int getFilesCount(NodePath nodePath) throws SQLException {
        ResultSet resultSet = null;
        int result = 0;
        try {
            resultSet = jdbcHelper.executeSelect(
                    "SELECT COUNT(*) FROM nodes WHERE node_type=1 and repo = ? and depth > ? and " +
                            "(node_path = ? or node_path like ?)",
                    nodePath.getRepo(), nodePath.getDepth(), nodePath.getPathName(), nodePath.getPathName() + "/%");
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return result;
    }

    public long getFilesTotalSize(String repoKey) throws SQLException {
        return jdbcHelper.executeSelectLong("SELECT SUM(bin_length) FROM nodes WHERE node_type=1 and repo = ?",
                repoKey);
    }

    public long getFilesTotalSize(NodePath nodePath) throws SQLException {
        return jdbcHelper.executeSelectLong(
                "SELECT SUM(bin_length) FROM nodes WHERE node_type=1 and repo = ? and depth > ? and " +
                        "(node_path = ? or node_path like ?)",
                nodePath.getRepo(), nodePath.getDepth(), nodePath.getPathName(), nodePath.getPathName() + "/%");
    }

    public int getNodesCount(String repoKey) throws SQLException {
        return jdbcHelper.executeSelectCount(
                "SELECT COUNT(*) FROM nodes WHERE (node_type=1 or node_type=0) and repo = ?", repoKey);
    }

    public int getNodesCount(NodePath nodePath) throws SQLException {
        return jdbcHelper.executeSelectCount(
                "SELECT COUNT(*) FROM nodes WHERE (node_type=1 or node_type=0) and repo = ? and depth > ? and " +
                        "(node_path = ? or node_path like ?)",
                nodePath.getRepo(), nodePath.getDepth(), nodePath.getPathName(), nodePath.getPathName() + "/%");
    }

    public List<Node> searchFileByName(String name) throws SQLException {
        ResultSet resultSet = null;
        List<Node> results = new ArrayList<>();
        try {
            resultSet = jdbcHelper.executeSelect(SELECT_NODE_QUERY +
                    "WHERE ftype = 1 and pname like '" + name + "'");
            while (resultSet.next()) {
                results.add(nodeFromResultSet(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return results;
    }

    public List<Node> searchGavc(String g, String a, String v, String c) {
        return null;
    }

    public List<Node> searchByChecksum(ChecksumType type, String checksum) throws SQLException {
        if (!type.isValid(checksum)) {
            throw new IllegalArgumentException(
                    "Cannot search for invalid " + type.name() + " checksum value '" + checksum + "'");
        }
        List<Node> results = Lists.newArrayList();
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT * FROM nodes " +
                    "WHERE " + type.name() + "_actual = ?", checksum);
            while (rs.next()) {
                results.add(nodeFromResultSet(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        return results;
    }

    public List<Node> searchBadChecksums(ChecksumType type) throws SQLException {
        List<Node> results = Lists.newArrayList();
        ResultSet rs = null;
        try {
            if (ChecksumType.sha1.equals(type)) {
                rs = jdbcHelper.executeSelect(
                        "SELECT * FROM nodes WHERE " +
                                "node_type = 1 and" +
                                "((sha1_original IS NULL) or " +
                                "(sha1_actual IS NULL) or " +
                                "(sha1_original != ? and sha1_original != sha1_actual))",
                        ChecksumInfo.TRUSTED_FILE_MARKER);
            } else {
                rs = jdbcHelper.executeSelect(
                        "SELECT * FROM nodes WHERE " +
                                "node_type = 1 and" +
                                "((md5_original IS NULL) or " +
                                "(md5_actual IS NULL) or " +
                                "(md5_original != ? and md5_original != md5_actual))",
                        ChecksumInfo.TRUSTED_FILE_MARKER);
            }
            while (rs.next()) {
                results.add(nodeFromResultSet(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        return results;
    }

    //TODO: [by YS] this is a just a temp naive search for maven plugin metadata
    public List<Node> searchFilesByProperty(String repo, String propKey, String propValue) throws SQLException {
        ResultSet resultSet = null;
        List<Node> results = new ArrayList<>();
        try {
            resultSet = jdbcHelper.executeSelect("SELECT n.* FROM nodes n " +
                    "JOIN node_props p ON n.node_id = p.node_id " +
                    "WHERE n.node_type = 1 and repo = ?  " +
                    "AND p.prop_key = ? and p.prop_value = ?",
                    repo, propKey, propValue);
            while (resultSet.next()) {
                results.add(nodeFromResultSet(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return results;
    }

    public List<Node> getOrphanNodes(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        List<Node> results = new ArrayList<>();
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM nodes n1" +
                    " WHERE n1.repo = ?" +
                    " AND n1.node_path like ?" +
                    " AND n1.node_name NOT IN" +
                    " (SELECT n2.node_name FROM nodes n2, nodes n3" +
                    " WHERE (n2.node_path like '%/%' AND n2.node_path like CONCAT('%/', n3.node_name))" +
                    " OR (n2.node_path not like '%/%' AND n2.node_path like CONCAT('%', n3.node_name)))",
                    path.getRepo(), emptyIfNullOrDot(path.getPath()) + "%");
            while (resultSet.next()) {
                results.add(nodeFromResultSet(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return results;
    }

    public Set<RepoStorageSummary> getRepositoriesStorageSummary() throws SQLException {
        ResultSet resultSet = null;
        Set<RepoStorageSummary> results = Sets.newHashSet();
        try {
            resultSet = jdbcHelper.executeSelect(
                    "SELECT repo, " +
                            "SUM(CASE WHEN node_type = 0 THEN 1 ELSE 0 END) as folders, " +
                            "SUM(CASE WHEN node_type = 1 THEN 1 ELSE 0 END) as files, " +
                            "SUM(bin_length) " +
                            "FROM nodes GROUP BY repo");
            while (resultSet.next()) {
                results.add(repoSummaryFromResultSet(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return results;
    }

    private Node nodeFromResultSet(ResultSet resultSet) throws SQLException {
        long nodeId = resultSet.getLong(1);
        boolean isFile = resultSet.getBoolean(2);
        String repoName = resultSet.getString(3);
        String path = emptyIfNullOrDot(resultSet.getString(4));
        String fileName = emptyIfNullOrDot(resultSet.getString(5));
        short depth = resultSet.getShort(6);
        long created = resultSet.getLong(7);
        String createdBy = resultSet.getString(8);
        long modified = resultSet.getLong(9);
        String modifiedBy = resultSet.getString(10);
        long updated = resultSet.getLong(11);
        long length = resultSet.getLong(12);
        String sha1Actual = resultSet.getString(13);
        String sha1Original = resultSet.getString(14);
        String md5Actual = resultSet.getString(15);
        String md5Original = resultSet.getString(16);
        Node node = new Node(nodeId, isFile, repoName, path, fileName, depth, created, createdBy,
                modified, modifiedBy, updated, length, sha1Actual, sha1Original, md5Actual, md5Original);
        return node;
    }

    private RepoStorageSummary repoSummaryFromResultSet(ResultSet rs) throws SQLException {
        // don't count the repo folder itself -> folderCount - 1
        return new RepoStorageSummary(rs.getString(1), rs.getLong(2) - 1, rs.getLong(3), rs.getLong(4));
    }
}
