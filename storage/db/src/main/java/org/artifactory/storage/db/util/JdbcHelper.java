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

import org.apache.commons.lang.StringUtils;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.util.blob.BlobWrapper;
import org.artifactory.util.PerfTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A helper class to execute jdbc queries.
 *
 * @author Yossi Shaul
 */
@Service
public class JdbcHelper {
    private static final Logger log = LoggerFactory.getLogger(JdbcHelper.class);

    private final DataSource dataSource;
    private final AtomicLong selectQueriesCounter = new AtomicLong();
    private final AtomicLong updateQueriesCounter = new AtomicLong();

    @Autowired
    public JdbcHelper(@Qualifier("dataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @return A transaction aware connection
     */
    private Connection getConnection() throws SQLException {
        return DataSourceUtils.doGetConnection(dataSource);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @Nonnull
    public ResultSet executeSelect(String query, Object... params) throws SQLException {
        selectQueriesCounter.incrementAndGet();
        debugSql(query, params);

        PerfTimer timer = null;
        if (log.isDebugEnabled()) {
            timer = new PerfTimer();
        }
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            if (params == null || params.length == 0) {
                stmt = con.createStatement();
                rs = stmt.executeQuery(query);
            } else {
                PreparedStatement pstmt = con.prepareStatement(parseInListQuery(query, params));
                stmt = pstmt;
                setParamsToStmt(pstmt, params);
                rs = pstmt.executeQuery();
            }
            if (timer != null && log.isDebugEnabled()) {
                timer.stop();
                log.debug("Query returned in {} : '{}'", timer, resolveQuery(query, params));
            }
            return ResultSetWrapper.newInstance(con, stmt, rs, dataSource);
        } catch (Exception e) {
            DbUtils.close(con, stmt, rs, dataSource);
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new SQLException("Unexpected exception: " + e.getMessage(), e);
            }
        }
    }

    public int executeUpdate(String query, Object... params) throws SQLException {
        updateQueriesCounter.incrementAndGet();
        debugSql(query, params);

        PerfTimer timer = null;
        if (log.isDebugEnabled()) {
            timer = new PerfTimer();
        }
        Connection con = null;
        Statement stmt = null;
        int results;
        try {
            con = getConnection();
            if (params == null || params.length == 0) {
                stmt = con.createStatement();
                results = stmt.executeUpdate(query);
            } else {
                PreparedStatement pstmt = con.prepareStatement(parseInListQuery(query, params));
                stmt = pstmt;
                setParamsToStmt(pstmt, params);
                results = pstmt.executeUpdate();
            }
            if (timer != null && log.isDebugEnabled()) {
                timer.stop();
                log.debug("Query returned with {} results in {} : '{}'",
                        results, timer, resolveQuery(query, params));
            }
            return results;
        } finally {
            DbUtils.close(con, stmt, null, dataSource);
        }
    }

    public int executeSelectCount(String query, Object... params) throws SQLException {
        try (ResultSet resultSet = executeSelect(query, params)) {
            int count = 0;
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
            return count;
        }
    }

    /**
     * Helper method to select long value. This method expects long value returned as the first column.
     * It ignores multiple results.
     *
     * @param query  The select query to execute
     * @param params The query parameters
     * @return Long value if a result was found or {@link org.artifactory.storage.db.DbService#NO_DB_ID} if not found
     */
    public long executeSelectLong(String query, Object... params) throws SQLException {
        try (ResultSet resultSet = executeSelect(query, params)) {
            long result = DbService.NO_DB_ID;
            if (resultSet.next()) {
                result = resultSet.getLong(1);
            }
            return result;
        }
    }

    private String parseInListQuery(String sql, Object... params) {
        int idx = sql.indexOf("(#)");
        if (idx != -1) {
            List<Integer> iterableSizes = new ArrayList<>(1);
            for (Object param : params) {
                if (param instanceof Collection) {
                    int size = ((Collection) param).size();
                    if (size > 0) {
                        iterableSizes.add(size);
                    }
                }
            }
            if (iterableSizes.isEmpty()) {
                throw new IllegalArgumentException("Could not find collection in parameters needed for query " + sql);
            }

            StringBuilder builder = new StringBuilder(sql.substring(0, idx + 1));
            for (int i = 0; i < iterableSizes.get(0); i++) {
                if (i != 0) {
                    builder.append(',');
                }
                builder.append('?');
            }
            builder.append(sql.substring(idx + 2));
            return builder.toString();
        }
        return sql;
    }

    private void setParamsToStmt(PreparedStatement pstmt, Object[] params) throws SQLException {
        int i = 1;
        for (Object param : params) {
            if (param instanceof Iterable) {
                for (Object p : (Iterable) param) {
                    pstmt.setObject(i++, p);
                }
            } else if (param instanceof BlobWrapper) {
                BlobWrapper blobWrapper = (BlobWrapper) param;
                if (blobWrapper.getLength() < 0) {
                    pstmt.setBinaryStream(i++, blobWrapper.getInputStream());
                } else {
                    pstmt.setBinaryStream(i++, blobWrapper.getInputStream(), blobWrapper.getLength());
                }
            } else {
                pstmt.setObject(i++, param);
            }
        }
    }

    private static void debugSql(String sql, Object[] params) {
        if (log.isDebugEnabled()) {
            String resolved = resolveQuery(sql, params);
            log.debug("Executing SQL: '" + resolved + "'.");
        }
    }

    public static String resolveQuery(String sql, Object[] params) {
        int expectedParamsCount = StringUtils.countMatches(sql, "?");
        int paramsLength = params == null ? 0 : params.length;
        if (expectedParamsCount != paramsLength) {
            log.warn("Unexpected parameters count. Expected {} but got {}", expectedParamsCount, paramsLength);
        }

        if (params == null || params.length == 0) {
            return sql;
        } else if (expectedParamsCount == paramsLength) {
            // replace placeholders in the query with matching parameter values
            // this method doesn't attempt to escape characters
            Object[] printParams = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                Object current = params[i];
                if (current == null) {
                    current = "NULL";
                } else if (current instanceof String) {
                    current = "'" + params[i] + "'";
                } else if (current instanceof BlobWrapper) {
                    current = "BLOB(length: " + ((BlobWrapper) params[i]).getLength() + ")";
                }
                printParams[i] = current;
            }
            sql = sql.replaceAll("\\?", "%s");
            sql = String.format(sql, printParams);
        } else {    // params not null but there's a difference between actual and expected
            StringBuilder builder = new StringBuilder();
            builder.append("Executing SQL: '").append(sql).append("'");
            if (params.length > 0) {
                builder.append(" with params: ");
                for (int i = 0; i < params.length; i++) {
                    builder.append("'");
                    builder.append(params[i]);
                    builder.append("'");
                    if (i < params.length - 1) {
                        builder.append(", ");
                    }
                }
            }
            sql = builder.toString();
        }
        return sql;
    }

    public void destroy() {
        DbUtils.closeDataSource(dataSource);
    }

    public long getSelectQueriesCount() {
        return selectQueriesCounter.get();
    }

    public long getUpdateQueriesCount() {
        return updateQueriesCounter.get();
    }
}
