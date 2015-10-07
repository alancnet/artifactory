package org.artifactory.storage.db.properties.dao;

import org.artifactory.storage.db.properties.model.DbProperties;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Date: 7/10/13 3:08 PM
 *
 * @author freds
 */
@Repository
public class DbPropertiesDao extends BaseDao {
    public static final String TABLE_NAME = "db_properties";

    @Autowired
    public DbPropertiesDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    @Nullable
    public DbProperties getLatestProperties() throws SQLException {
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT * FROM " + TABLE_NAME +
                    " WHERE installation_date = (SELECT MAX(installation_date) FROM " + TABLE_NAME + ")");
            if (rs.next()) {
                return new DbProperties(rs.getLong(1), rs.getString(2),
                        zeroIfNull(rs.getInt(3)), zeroIfNull(rs.getLong(4)));
            }
        } finally {
            DbUtils.close(rs);
        }
        return null;
    }

    public boolean createProperties(DbProperties dbProperties) throws SQLException {
        int updateCount = jdbcHelper.executeUpdate("INSERT INTO " + TABLE_NAME +
                " (installation_date, artifactory_version, artifactory_revision, artifactory_release)" +
                " VALUES(?, ?, ?, ?)",
                dbProperties.getInstallationDate(), dbProperties.getArtifactoryVersion(),
                nullIfZeroOrNeg(dbProperties.getArtifactoryRevision()),
                nullIfZeroOrNeg(dbProperties.getArtifactoryRelease()));
        return updateCount == 1;
    }

    public boolean isDbPropertiesTableExists() throws SQLException {
        try (Connection con = jdbcHelper.getDataSource().getConnection()) {
            DatabaseMetaData metaData = con.getMetaData();
            return DbUtils.tableExists(metaData, DbPropertiesDao.TABLE_NAME);
        }
    }
}
