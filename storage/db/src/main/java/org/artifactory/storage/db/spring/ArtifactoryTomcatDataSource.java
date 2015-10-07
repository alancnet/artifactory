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

package org.artifactory.storage.db.spring;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.artifactory.storage.StorageProperties;

import java.sql.Connection;
import java.util.concurrent.TimeUnit;

/**
 * A pooling data source based on tomcat-jdbc library.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryTomcatDataSource extends DataSource implements ArtifactoryDataSource {

    public ArtifactoryTomcatDataSource(StorageProperties s) {
        // see org.apache.tomcat.jdbc.pool.DataSourceFactory.parsePoolProperties()
        PoolProperties p = new PoolProperties();
        p.setUrl(s.getConnectionUrl());
        p.setDriverClassName(s.getDriverClass());
        p.setUsername(s.getUsername());
        p.setPassword(s.getPassword());

        p.setDefaultAutoCommit(false);
        p.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        p.setInitialSize(s.getIntProperty("initialSize", 1));
        p.setMaxAge(s.getIntProperty("maxAge", 0));
        p.setMaxActive(s.getMaxActiveConnections());
        p.setMaxWait(s.getIntProperty("maxWait", (int) TimeUnit.SECONDS.toMillis(120)));
        p.setMaxIdle(s.getMaxIdleConnections());
        p.setMinIdle(s.getIntProperty("minIdle", 1));
        p.setMinEvictableIdleTimeMillis(
                s.getIntProperty("minEvictableIdleTimeMillis", 300000));
        p.setTimeBetweenEvictionRunsMillis(
                s.getIntProperty("timeBetweenEvictionRunsMillis", 30000));
        p.setInitSQL(s.getProperty("initSQL", null));

        // validation query for all kind of tests (connect, borrow etc.)
        p.setValidationQuery(s.getProperty("validationQuery", getDefaultValidationQuery(s)));
        p.setValidationQueryTimeout(s.getIntProperty("validationQueryTimeout", 30));
        p.setValidationInterval(s.getLongProperty("validationInterval", 30000));
        p.setTestOnBorrow(s.getBooleanProperty("testOnBorrow", true));
        p.setTestWhileIdle(s.getBooleanProperty("testWhileIdle", false));
        p.setTestOnReturn(s.getBooleanProperty("testOnReturn", false));
        p.setTestOnConnect(s.getBooleanProperty("testOnConnect", false));

        p.setRemoveAbandoned(s.getBooleanProperty("removeAbandoned", false));
        p.setRemoveAbandonedTimeout(s.getIntProperty("removeAbandonedTimeout", 600));
        p.setSuspectTimeout(s.getIntProperty("suspectTimeout", 600));
        p.setLogAbandoned(s.getBooleanProperty("logAbandoned", false));
        p.setLogValidationErrors(s.getBooleanProperty("logValidationErrors", false));

        p.setJmxEnabled(s.getBooleanProperty("jmxEnabled", true));

        // only applicable if auto commit is false. has high performance penalty and only protects bugs in the code
        p.setRollbackOnReturn(s.getBooleanProperty("rollbackOnReturn", false));
        p.setCommitOnReturn(s.getBooleanProperty("commitOnReturn", false));

        p.setIgnoreExceptionOnPreLoad(s.getBooleanProperty("ignoreExceptionOnPreLoad", false));

        //p.setJdbcInterceptors(s.getProperty("jdbcInterceptors", "ConnectionState;StatementFinalizer"));
        p.setJdbcInterceptors(s.getProperty("jdbcInterceptors", null));

        p.setDefaultCatalog(s.getProperty("defaultCatalog", null));

        setPoolProperties(p);
    }

    @Override
    public int getActiveConnectionsCount() {
        return super.getActive();
    }

    @Override
    public int getIdleConnectionsCount() {
        return super.getIdle();
    }

    @Override
    public void close() {
        close(true);    // close all connections, including active ones
    }

    public static DataSource createUniqueIdDataSource(StorageProperties s) {
        // see org.apache.tomcat.jdbc.pool.DataSourceFactory.parsePoolProperties()
        PoolProperties p = new PoolProperties();
        p.setUrl(s.getConnectionUrl());
        p.setDriverClassName(s.getDriverClass());
        p.setUsername(s.getUsername());
        p.setPassword(s.getPassword());

        // auto commit is true for the unique id generator
        p.setDefaultAutoCommit(true);
        p.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        // only one connection is required for the id generator
        p.setInitialSize(1);
        p.setMinIdle(1);
        p.setMaxIdle(1);
        p.setMaxActive(1);
        p.setMaxAge(s.getIntProperty("maxAge", 0));
        p.setMaxWait(s.getIntProperty("maxWait", (int) TimeUnit.SECONDS.toMillis(120)));
        p.setMinEvictableIdleTimeMillis(
                s.getIntProperty("minEvictableIdleTimeMillis", 300000));
        p.setTimeBetweenEvictionRunsMillis(
                s.getIntProperty("timeBetweenEvictionRunsMillis", 30000));
        p.setInitSQL(s.getProperty("initSQL", null));

        // validation query for all kind of tests (connect, borrow etc.)
        p.setValidationQuery(s.getProperty("validationQuery", getDefaultValidationQuery(s)));
        p.setValidationQueryTimeout(s.getIntProperty("validationQueryTimeout", 30));
        p.setValidationInterval(s.getLongProperty("validationInterval", 30000));
        p.setTestOnBorrow(s.getBooleanProperty("testOnBorrow", true));
        p.setTestWhileIdle(s.getBooleanProperty("testWhileIdle", false));
        p.setTestOnReturn(s.getBooleanProperty("testOnReturn", false));
        p.setTestOnConnect(s.getBooleanProperty("testOnConnect", false));

        p.setRemoveAbandoned(s.getBooleanProperty("removeAbandoned", false));
        p.setRemoveAbandonedTimeout(s.getIntProperty("removeAbandonedTimeout", 600));
        p.setSuspectTimeout(s.getIntProperty("suspectTimeout", 600));
        p.setLogAbandoned(s.getBooleanProperty("logAbandoned", false));
        p.setLogValidationErrors(s.getBooleanProperty("logValidationErrors", false));

        p.setJmxEnabled(false);

        p.setIgnoreExceptionOnPreLoad(s.getBooleanProperty("ignoreExceptionOnPreLoad", false));

        //p.setJdbcInterceptors(s.getProperty("jdbcInterceptors", "ConnectionState;StatementFinalizer"));
        p.setJdbcInterceptors(s.getProperty("jdbcInterceptors", null));

        p.setDefaultCatalog(s.getProperty("defaultCatalog", null));

        return new DataSource(p);
    }

    private static String getDefaultValidationQuery(StorageProperties s) {
        switch (s.getDbType()) {
            case DERBY:
                return "values(1)";
            case MYSQL:
                return "/* ping */"; // special MySQL lightweight ping query
            case ORACLE:
                return "SELECT 1 FROM DUAL";
            default:
                return "SELECT 1";
        }
    }
}
