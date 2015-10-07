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

package org.artifactory.storage.db.itest.spring;

import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.db.DbType;
import org.artifactory.storage.db.spring.ArtifactoryTomcatDataSource;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.util.querybuilder.*;
import org.artifactory.util.ResourceUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A Spring {@link org.springframework.context.annotation.Configuration} to initialized database beans.
 *
 * @author Yossi Shaul
 */
@Configuration
public class DbTestConfigFactory implements BeanFactoryAware {

    private BeanFactory beanFactory;

    @Bean(name = "dataSource")
    public DataSource createDataSource() {
        StorageProperties storageProperties = beanFactory.getBean("storageProperties", StorageProperties.class);
        /*ArtifactoryDataSource dataSource = new ArtifactoryDbcpDataSource(storageProperties);
        GenericObjectPool pool = (GenericObjectPool) ReflectionTestUtils.getField(dataSource, "_pool");
        PoolableConnectionFactory factory = (PoolableConnectionFactory) ReflectionTestUtils.getField(pool, "_factory");
        factory.setDefaultAutoCommit(true);*/
        ArtifactoryTomcatDataSource dataSource = new ArtifactoryTomcatDataSource(storageProperties);
        // the db tests has limited tx support (which is mostly controlled by the business logic layer) so we are using
        // auto commit
        dataSource.setDefaultAutoCommit(true);
        return dataSource;
    }

    /**
     * @return Auto-commit datasource for the unique ids generator.
     * @see org.artifactory.storage.db.spring.DbConfigFactory#createUniqueIdsDataSource()
     */
    @Bean(name = "uniqueIdsDataSource")
    public DataSource createUniqueIdsDataSource() {
        StorageProperties storageProperties = beanFactory.getBean("storageProperties", StorageProperties.class);

        /*GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        poolConfig.maxActive = 1;
        poolConfig.maxIdle = 1;
        ObjectPool connectionPool = new GenericObjectPool(null, poolConfig);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                storageProperties.getConnectionUrl(),
                storageProperties.getUsername(), storageProperties.getPassword());

        // default auto commit true!
        PoolableConnectionFactory pcf = new ArtifactoryPoolableConnectionFactory(connectionFactory,
                connectionPool, null, null, false, true);
        pcf.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        return new PoolingDataSource(connectionPool);*/
        return ArtifactoryTomcatDataSource.createUniqueIdDataSource(storageProperties);
    }

    @Bean(name = "storageProperties")
    public StorageProperties getDbProperties() throws IOException {
        File dbPropsFile;
        String generatedDbConfigLocation = getGeneratedDbConfigLocation();
        if (generatedDbConfigLocation != null) {
            dbPropsFile = new File(generatedDbConfigLocation);
        } else {
            String dbConfigName = getDbConfigName();
            dbPropsFile = ResourceUtils.getResourceAsFile("/db/" + dbConfigName + ".properties");
        }

        StorageProperties storageProperties = new StorageProperties(dbPropsFile);

        File workDir = new File("target", "dbtest").getAbsoluteFile();
        Files.createDirectories(workDir.toPath());

        // configure embedded derby
        if (storageProperties.getDbType().equals(DbType.DERBY)) {
            System.setProperty("derby.stream.error.file", new File(workDir, "derby.log").getAbsolutePath());
            String url = storageProperties.getConnectionUrl();
            File dbWorkDir = new File(workDir, DbType.DERBY.name()).getAbsoluteFile();
            url = url.replace("{db.home}", dbWorkDir.getAbsolutePath());
            storageProperties.setConnectionUrl(url);
        }

        return storageProperties;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public String getDbConfigName() {
        return System.getProperty("artifactory.db.config.name", "derby");
    }

    public String getGeneratedDbConfigLocation() {
        String generatedConfig = System.getProperty("artifactory.db.generated.config");
        // reset the value to make sure it's not propagated to other tests
        System.clearProperty("artifactory.db.generated.config");
        return generatedConfig;
    }


    /**
     * create  a query builder instance per db type
     *
     * @return query builder instance
     */
    @Bean(name = "queryBuilder", autowire = Autowire.BY_TYPE)
    public IQueryBuilder createSqlBuilder() throws SQLException {
        JdbcHelper jdbcHelper = beanFactory.getBean(JdbcHelper.class);
        StorageProperties storageProperties = beanFactory.getBean(StorageProperties.class);
        String productName = storageProperties.getDbType().toString();
        Connection connection = jdbcHelper.getDataSource().getConnection();
        connection.close();
        IQueryBuilder queryBuilder;
        switch (productName) {
            case "oracle":
                queryBuilder = new OracleQueryBuilder();
                break;
            case "mssql":
                queryBuilder = new SqlServerQueryBuilder();
                break;
            case "derby":
                queryBuilder = new DerbyQueryBuilder();
                break;
            case "postgresql":
                queryBuilder = new PostgresqlQueryBuilder();
                break;
            case "mysql":
                queryBuilder = new MysqlQueryBuilder();
                break;
            default:
                queryBuilder = new DerbyQueryBuilder();
                break;
        }
        return queryBuilder;
    }

}
