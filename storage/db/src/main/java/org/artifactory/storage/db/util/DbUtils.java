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

package org.artifactory.storage.db.util;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.storage.db.spring.ArtifactoryDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A utility class for common JDBC operations.
 *
 * @author Yossi Shaul
 */
public abstract class DbUtils {
    private static final Logger log = LoggerFactory.getLogger(DbUtils.class);
    public static final String INSERT_INTO = "INSERT INTO ";
    public static final String VALUES = " VALUES";

    /**
     * Closes the given resources. Exceptions are just logged.
     *
     * @param con  The {@link java.sql.Connection} to close
     * @param stmt The {@link java.sql.Statement} to close
     * @param rs   The {@link java.sql.ResultSet} to close
     */
    public static void close(@Nullable Connection con, @Nullable Statement stmt, @Nullable ResultSet rs) {
        try {
            close(rs);
        } finally {
            try {
                close(stmt);
            } finally {
                close(con);
            }
        }
    }

    /**
     * Closes the given resources. Exceptions are just logged.
     *
     * @param con  The {@link java.sql.Connection} to close
     * @param stmt The {@link java.sql.Statement} to close
     * @param rs   The {@link java.sql.ResultSet} to close
     */
    public static void close(@Nullable Connection con, @Nullable Statement stmt, @Nullable ResultSet rs,
            @Nullable DataSource ds) {
        try {
            close(rs);
        } finally {
            try {
                close(stmt);
            } finally {
                close(con, ds);
            }
        }
    }

    /**
     * Closes the given connection and just logs any exception.
     *
     * @param con The {@link java.sql.Connection} to close.
     */
    public static void close(@Nullable Connection con) {
        close(con, null);
    }

    /**
     * Closes the given connection and just logs any exception.
     *
     * @param con The {@link java.sql.Connection} to close.
     */
    public static void close(@Nullable Connection con, @Nullable DataSource ds) {
        if (con != null) {
            try {
                DataSourceUtils.doReleaseConnection(con, ds);
            } catch (SQLException e) {
                log.trace("Could not close JDBC connection", e);
            } catch (Exception e) {
                log.trace("Unexpected exception when closing JDBC connection", e);
            }
        }
    }

    /**
     * Closes the given statement and just logs any exception.
     *
     * @param stmt The {@link java.sql.Statement} to close.
     */
    public static void close(@Nullable Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.trace("Could not close JDBC statement", e);
            } catch (Exception e) {
                log.trace("Unexpected exception when closing JDBC statement", e);
            }
        }
    }

    /**
     * Closes the given result set and just logs any exception.
     *
     * @param rs The {@link java.sql.ResultSet} to close.
     */
    public static void close(@Nullable ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.trace("Could not close JDBC result set", e);
            } catch (Exception e) {
                log.trace("Unexpected exception when closing JDBC result set", e);
            }
        }
    }

    public static void executeSqlStream(Connection con, InputStream in) throws IOException, SQLException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
        Statement stmt = con.createStatement();
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isNotBlank(line) && !line.startsWith("--") && !line.startsWith("#")) {
                    sb.append(line);
                    if (line.endsWith(";")) {
                        String query = sb.substring(0, sb.length() - 1);
                        if (query.startsWith(INSERT_INTO)) {
                            String databaseProductName = con.getMetaData().getDatabaseProductName();
                            if ("Oracle".equals(databaseProductName)) {
                                query = transformInsertIntoForOracle(query);
                            }
                        }
                        log.debug("Executing query:\n{}", query);
                        try {
                            stmt.executeUpdate(query);
                            if (!con.getAutoCommit()) {
                                con.commit();
                            }
                        } catch (SQLException e) {
                            log.error("Failed to execute query: {}:\n{}", e.getMessage(), query);
                            throw e;
                        }
                        sb = new StringBuilder();
                    } else {
                        sb.append("\n");
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(reader);
            close(stmt);
        }
    }

    private static String transformInsertIntoForOracle(String query) {
        int values = query.indexOf(VALUES);
        if (values == -1) {
            throw new IllegalArgumentException("Query " + query + " does not the keyword " + VALUES);
        }
        String tableName = query.substring(INSERT_INTO.length(), values).trim();
        log.info("Doing insert all in Oracle for table " + tableName);
        StringBuilder sb = new StringBuilder("INSERT ALL ");
        boolean inValue = false;
        for (char c : query.toCharArray()) {
            if (c == '(') {
                inValue = true;
                sb.append("INTO ").append(tableName).append(VALUES).append(" (");
            } else if (c == ')') {
                inValue = false;
                sb.append(")\n");
            } else if (inValue) {
                sb.append(c);
            }
        }
        sb.append("SELECT * FROM dual");
        return sb.toString();
    }

    public static void closeDataSource(DataSource dataSource) {
        if (dataSource == null) {
            return;
        }
        if (dataSource instanceof ArtifactoryDataSource) {
            try {
                ((ArtifactoryDataSource) dataSource).close();
            } catch (Exception e) {
                String msg = "Error closing the data source " + dataSource + " due to:" + e.getMessage();
                if (log.isDebugEnabled()) {
                    log.error(msg, e);
                } else {
                    log.error(msg);
                }
            }
        }
    }

    public static boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        boolean schemaExists;
        if (metaData.storesLowerCaseIdentifiers()) {
            tableName = tableName.toLowerCase();
        } else if (metaData.storesUpperCaseIdentifiers()) {
            tableName = tableName.toUpperCase();
        }
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            schemaExists = rs.next();
        }
        return schemaExists;
    }
}
