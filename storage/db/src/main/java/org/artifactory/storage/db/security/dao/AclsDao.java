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

import org.artifactory.storage.db.security.entity.Ace;
import org.artifactory.storage.db.security.entity.Acl;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Date: 9/3/12
 * Time: 1:42 PM
 *
 * @author freds
 */
@Repository
public class AclsDao extends BaseDao {

    @Autowired
    public AclsDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public int createAcl(Acl acl) throws SQLException {
        int res = jdbcHelper.executeUpdate("INSERT INTO acls VALUES(" +
                " ?, ?," +
                " ?, ?)",
                acl.getAclId(), acl.getPermTargetId(),
                acl.getLastModified(), acl.getLastModifiedBy());
        for (Ace ace : acl.getAces()) {
            res += insertAce(ace);
        }
        return res;
    }

    public int updateAcl(Acl acl) throws SQLException {
        int res = jdbcHelper.executeUpdate("UPDATE acls SET" +
                " perm_target_id = ?, modified = ?, modified_by = ?" +
                " WHERE acl_id = ?",
                acl.getPermTargetId(), acl.getLastModified(), acl.getLastModifiedBy(),
                acl.getAclId());
        jdbcHelper.executeUpdate("DELETE FROM aces WHERE acl_id = ?", acl.getAclId());
        for (Ace ace : acl.getAces()) {
            res += insertAce(ace);
        }
        return res;
    }

    private int insertAce(Ace ace) throws SQLException {
        return jdbcHelper.executeUpdate("INSERT INTO aces VALUES(?, ?, ?, ?, ?)",
                ace.getAceId(), ace.getAclId(), ace.getMask(),
                nullIfZero(ace.getUserId()), nullIfZero(ace.getGroupId()));
    }

    public int deleteAcl(long aclId) throws SQLException {
        int res = jdbcHelper.executeUpdate("DELETE FROM aces WHERE acl_id = ?", aclId);
        res += jdbcHelper.executeUpdate("DELETE FROM acls WHERE acl_id = ?", aclId);
        return res;
    }

    public Acl findAclByPermissionTargetId(long permTargetId) throws SQLException {
        ResultSet resultSet = null;
        Acl acl = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM acls WHERE perm_target_id = ?", permTargetId);
            if (resultSet.next()) {
                acl = resultSetToAcl(resultSet);
            }
        } finally {
            DbUtils.close(resultSet);
        }
        if (acl != null) {
            acl.setAces(findAces(acl.getAclId()));
        }
        return acl;
    }

    public Acl findAcl(long aclId) throws SQLException {
        ResultSet resultSet = null;
        Acl acl = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM acls WHERE acl_id = ?", aclId);
            if (resultSet.next()) {
                acl = resultSetToAcl(resultSet);
            }
        } finally {
            DbUtils.close(resultSet);
        }
        if (acl != null) {
            acl.setAces(findAces(aclId));
        }
        return acl;
    }

    private Acl resultSetToAcl(ResultSet resultSet) throws SQLException {
        Acl acl;
        acl = new Acl(resultSet.getLong(1), resultSet.getLong(2), resultSet.getLong(3), resultSet.getString(4));
        return acl;
    }

    private Set<Ace> findAces(long aclId) throws SQLException {
        Set<Ace> aces = new HashSet<>(3);
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM aces WHERE acl_id = ?", aclId);
            while (resultSet.next()) {
                aces.add(aceFromResultSet(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return aces;
    }

    public Collection<Acl> getAllAcls() throws SQLException {
        ResultSet resultSet = null;
        Map<Long, Acl> aclsMap = new HashMap<>();
        Map<Long, Set<Ace>> acesMap = new HashMap<>();
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM acls");
            while (resultSet.next()) {
                Acl acl = resultSetToAcl(resultSet);
                aclsMap.put(acl.getAclId(), acl);
                acesMap.put(acl.getAclId(), new HashSet<Ace>(3));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM aces");
            while (resultSet.next()) {
                Ace ace = aceFromResultSet(resultSet);
                Set<Ace> aces = acesMap.get(ace.getAclId());
                aces.add(ace);
            }
        } finally {
            DbUtils.close(resultSet);
        }
        for (Acl acl : aclsMap.values()) {
            acl.setAces(acesMap.get(acl.getAclId()));
        }
        return aclsMap.values();
    }

    private Ace aceFromResultSet(ResultSet resultSet) throws SQLException {
        return new Ace(resultSet.getLong(1), resultSet.getLong(2), resultSet.getInt(3),
                zeroIfNull(resultSet.getLong(4)), zeroIfNull(resultSet.getLong(5)));
    }

    public boolean userHasAce(long userId) throws SQLException {
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT COUNT(*) FROM aces WHERE user_id = ?", userId);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            DbUtils.close(rs);
        }
    }

    public boolean groupHasAce(long groupId) throws SQLException {
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT COUNT(*) FROM aces WHERE group_id = ?", groupId);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            DbUtils.close(rs);
        }
    }

    public int deleteAceForGroup(long groupId) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM aces WHERE group_id = ?", groupId);
    }

    public int deleteAceForUser(long userId) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM aces WHERE user_id = ?", userId);
    }

    public int deleteAllAcls() throws SQLException {
        int res = jdbcHelper.executeUpdate("DELETE FROM aces");
        res += jdbcHelper.executeUpdate("DELETE FROM acls");
        return res;
    }
}
