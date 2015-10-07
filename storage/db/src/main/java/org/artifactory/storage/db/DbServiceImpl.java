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

package org.artifactory.storage.db;

import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.jdbc.pool.jmx.ConnectionPool;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.mbean.ManagedDataSource;
import org.artifactory.storage.db.properties.model.DbProperties;
import org.artifactory.storage.db.properties.service.ArtifactoryDbPropertiesService;
import org.artifactory.storage.db.spring.ArtifactoryDataSource;
import org.artifactory.storage.db.spring.ArtifactoryTomcatDataSource;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.IdGenerator;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.version.ArtifactoryDBVersion;
import org.artifactory.util.ResourceUtils;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * @author Yossi Shaul
 */
@Repository
@Reloadable(beanClass = DbService.class)
public class DbServiceImpl implements DbService {
    private static final Logger log = LoggerFactory.getLogger(DbServiceImpl.class);

    private static final double MYSQL_MIN_VERSION = 5.5;

    @Autowired
    private JdbcHelper jdbcHelper;

    @Autowired
    @Qualifier("storageProperties")
    private StorageProperties storageProperties;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private ArtifactoryDbPropertiesService dbPropertiesService;

    @PostConstruct
    private void initDb() throws Exception {
        printConnectionInfo();

        // check if db tables exist and initialize if not
        if (!isSchemaExist()) {
            try (Connection con = jdbcHelper.getDataSource().getConnection()) {

                // if using mySQL, check version compatibility
                if (storageProperties.getDbType() == DbType.MYSQL) {
                    checkMySqlMinVersion();
                }

                // read ddl from file and execute
                log.info("***Creating database schema***");
                DbUtils.executeSqlStream(con, getDbSchemaSql());
                updateDbProperties();


            }
        }

        // initialize id generator
        initializeIdGenerator();
    }

    private void updateDbProperties() {
        // Update DBProperties
        long installTime = System.currentTimeMillis();
        CompoundVersionDetails versionDetails = ArtifactoryHome.get().readRunningArtifactoryVersion();
        String versionStr = versionDetails.getVersion().getValue();
        long timestamp = versionDetails.getTimestamp();
        int revisionInt = versionDetails.getRevisionInt();
        dbPropertiesService.updateDbProperties(new DbProperties(installTime, versionStr, revisionInt, timestamp));
    }

    @Override
    public void init() {
        registerDataSourceMBean();
    }

    @Override
    public DbType getDatabaseType() {
        return storageProperties.getDbType();
    }

    @Override
    public long nextId() {
        return idGenerator.nextId();
    }

    @Override
    public void compressDerbyDb(BasicStatusHolder statusHolder) {
        DerbyUtils.compress(statusHolder);
    }

    @Override
    public <T> T invokeInTransaction(String transactionName, Callable<T> execute) {
        if (StringUtils.isNotBlank(transactionName)) {
            TransactionSynchronizationManager.setCurrentTransactionName(transactionName);
        }
        try {
            return execute.call();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    //used via reflection by DbBaseTest
    public void initializeIdGenerator() throws SQLException {
        idGenerator.initializeIdGenerator();
    }

    private InputStream getDbSchemaSql() throws IOException {
        String dbTypeName = storageProperties.getDbType().toString();
        String resourcePath = "/" + dbTypeName + "/" + dbTypeName + ".sql";
        InputStream resource = ResourceUtils.getResource(resourcePath);
        if (resource == null) {
            throw new IOException("Database DDL resource not found at: '" + resourcePath + "'");
        }
        return resource;
    }

    private boolean isSchemaExist() throws SQLException {
        log.debug("Checking for database schema existence");
        try (Connection con = jdbcHelper.getDataSource().getConnection()) {
            DatabaseMetaData metaData = con.getMetaData();
            return tableExists(metaData, NodesDao.TABLE_NAME);
        }
    }

    public static boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        return DbUtils.tableExists(metaData, tableName);
    }

    private void printConnectionInfo() throws SQLException {
        Connection connection = jdbcHelper.getDataSource().getConnection();
        try {
            DatabaseMetaData meta = connection.getMetaData();
            log.info("Database: {} {}. Driver: {} {}", meta.getDatabaseProductName(), meta.getDatabaseProductVersion(),
                    meta.getDriverName(), meta.getDriverVersion());
            log.info("Connection URL: {}", meta.getURL());
        } catch (SQLException e) {
            log.warn("Can not retrieve database and driver name / version", e);
        } finally {
            DbUtils.close(connection);
        }
    }

    private void registerDataSourceMBean() {
        DataSource dataSource = jdbcHelper.getDataSource();
        if (dataSource instanceof ArtifactoryDataSource) {
            ContextHelper.get().beanForType(MBeanRegistrationService.class).register(
                    new ManagedDataSource((ArtifactoryDataSource) dataSource, jdbcHelper), "Storage", "Data Source");
        }
        if (dataSource instanceof ArtifactoryTomcatDataSource) {
            // register the Tomcat JDBC pool JMX if enabled
            ArtifactoryTomcatDataSource tomcatDataSource = (ArtifactoryTomcatDataSource) dataSource;
            if (tomcatDataSource.isJmxEnabled()) {
                ConnectionPool jmxPool = tomcatDataSource.getPool().getJmxPool();
                ContextHelper.get().beanForType(MBeanRegistrationService.class)
                        .register(jmxPool, "Storage", "Connection Pool");

            }
        }
    }

    private boolean checkMySqlMinVersion() {
        log.debug("Checking MySQL version compatibility");
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT VERSION();");
            if (rs.next()) {
                String versionString = rs.getString(1);
                int i = StringUtils.ordinalIndexOf(versionString, ".", 2);
                if (i == -1) {
                    i = versionString.length();
                }
                Double mysqlVersion = Double.valueOf(versionString.substring(0, i));
                if (mysqlVersion >= MYSQL_MIN_VERSION) {
                    return true;
                } else {
                    log.error("Unsupported MySQL version found [" + versionString + "]. " +
                            "Minimum version required is " + MYSQL_MIN_VERSION + ". " +
                            "Please follow the requirements on the wiki page.");
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Could not determine MySQL version due to an exception", e);
        } finally {
            DbUtils.close(rs);
        }
        log.error("Could not determine MySQL version. Minimum version should be " + MYSQL_MIN_VERSION + " and above.");
        return false;
    }


    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        ArtifactoryDBVersion.convert(source.getVersion(), jdbcHelper, storageProperties.getDbType());
        updateDbProperties();
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
    }

    @Override
    public void destroy() {
        jdbcHelper.destroy();
    }

}
