package org.artifactory.storage.db.security.dao;

import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A dao for the user_props table.
 * This table contains any extra data or properties connected to users that may
 * be required by external authentication methods.
 *
 * @author Travis Foster
 */
@Repository
public class UserPropertiesDao extends BaseDao {

    @Autowired
    public UserPropertiesDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public long getUserIdByProperty(String key, String val) throws SQLException {
        ResultSet rs = null;
        try {
            String sel = "SELECT user_id FROM user_props ";
            sel += "WHERE prop_key = ? AND prop_value = ?";
            rs = jdbcHelper.executeSelect(sel, key, val);
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } finally {
            DbUtils.close(rs);
        }
    }

    public boolean addUserProperty(long uid, String key, String val) throws SQLException {
        DataSource dataSource = null;
        Connection conn = null;
        PreparedStatement update = null;
        PreparedStatement insert = null;
        String upd = "UPDATE user_props SET prop_value=? WHERE user_id=? AND prop_key=?";
        String ins = "INSERT INTO user_props (user_id, prop_key, prop_value) VALUES (?, ?, ?)";
        try {
            dataSource = jdbcHelper.getDataSource();
            conn = dataSource.getConnection();
            update = conn.prepareStatement(upd);
            insert = conn.prepareStatement(ins);
            update.setString(1, val);
            update.setLong(2, uid);
            update.setString(3, key);
            int updated = update.executeUpdate();
            if (updated == 0) {
                insert.setLong(1, uid);
                insert.setString(2, key);
                insert.setString(3, val);
                int inserted = insert.executeUpdate();
                return inserted == 1;
            } else return updated == 1;
        } finally {
            DbUtils.close(insert);
            DbUtils.close(conn, update, null, dataSource);
        }
    }

    public String getUserProperty(String username, String key) throws SQLException {
        ResultSet rs = null;
        try {
            String sel = "SELECT d.prop_value FROM users u INNER JOIN user_props d USING (user_id) ";
            sel += "WHERE u.username = ? AND d.prop_key = ?";
            rs = jdbcHelper.executeSelect(sel, username, key);
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        } finally {
            DbUtils.close(rs);
        }
    }

    public boolean deleteProperty(long uid, String key) throws SQLException {
        String del = "DELETE FROM user_props WHERE user_id = ? AND prop_key = ?";
        return jdbcHelper.executeUpdate(del, uid, key) == 1;
    }
}
