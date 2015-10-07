/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.storage.db.itest;

import org.artifactory.storage.db.DbType;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A utility class for integration tests to clean and setup the database
 *
 * @author Yossi Shaul
 */
public class DbTestUtils {
    private static final Logger log = LoggerFactory.getLogger(DbTestUtils.class);

    /**
     * A list of all the tables in the database
     */
    public static String[] tables = new String[]{
            "db_properties", "artifactory_servers",
            "stats_remote", "stats", "watches", "node_props", "node_meta_infos", "nodes",
            "indexed_archives_entries", "archive_names", "archive_paths", "indexed_archives",
            "binary_blobs", "binaries",
            "aces", "acls", "users_groups", "groups", "user_props", "users",
            "permission_target_repos", "permission_targets",
            "configs", "tasks",
            "module_props", "build_props", "build_jsons", "build_promotions",
            "build_dependencies", "build_artifacts", "build_modules", "builds",
            "unique_ids"
    };

    public static void refreshOrRecreateSchema(Connection con, DbType dbType) throws IOException, SQLException {
        // to improve test speed, re-create the schema only if there's a missing table
        boolean recreateSchema = isTableMissing(con);
        if (recreateSchema) {
            log.info("Recreating test database schema for database: {}", dbType);
            dropAllExistingTables(con);
            createSchema(con, dbType);
        } else {
            log.info("Deleting database tables data from database: {}", dbType);
            deleteFromAllTables(con);
        }
    }

    public static void dropAllExistingTables(Connection con) throws SQLException {
        for (String table : tables) {
            if (tableExists(table, con)) {
                try (Statement statement = con.createStatement()) {
                    statement.execute("DROP TABLE " + table);
                }
            }
        }
    }

    private static void deleteFromAllTables(Connection con) throws SQLException {
        for (String table : tables) {
            try (Statement statement = con.createStatement()) {
                statement.execute("DELETE FROM " + table);
            }
        }
    }

    public static boolean isTableMissing(Connection con) throws SQLException, IOException {
        for (String table : tables) {
            if (!tableExists(table, con)) {
                return true;
            }
        }
        return false;
    }

    public static int getColumnSize(Connection con, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        if (metaData.storesLowerCaseIdentifiers()) {
            tableName = tableName.toLowerCase();
            columnName = columnName.toLowerCase();
        } else if (metaData.storesUpperCaseIdentifiers()) {
            tableName = tableName.toUpperCase();
            columnName = columnName.toUpperCase();
        }

        try(Statement statement = con.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * from " + tableName);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                if (resultSetMetaData.getColumnName(i).equals(columnName)) {
                    return resultSetMetaData.getColumnDisplaySize(i);
                }
            }
        }

        return -1;
    }

    private static void createSchema(Connection con, DbType dbType) throws SQLException, IOException {
        // read ddl from file and execute
        DbUtils.executeSqlStream(con, getDbSchemaSql(dbType));
    }

    private static boolean tableExists(String tableName, Connection con) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        if (metaData.storesLowerCaseIdentifiers()) {
            tableName = tableName.toLowerCase();
        } else if (metaData.storesUpperCaseIdentifiers()) {
            tableName = tableName.toUpperCase();
        }
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            boolean tableExists = rs.next();
            return tableExists;
        }
    }

    private static InputStream getDbSchemaSql(DbType dbType) {
        String dbConfigDir = dbType.toString();
        return ResourceUtils.getResource("/" + dbConfigDir + "/" + dbConfigDir + ".sql");
    }
}
