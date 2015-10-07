package org.artifactory.storage.db.mbean;

/**
 * MBean wrapper for {@link org.artifactory.storage.db.spring.ArtifactoryDataSource}
 *
 * @author mamo
 */
public interface ManagedDataSourceMBean {

    int getMaxActive();

    int getMaxIdle();

    long getMaxWait();

    int getMinIdle();

    int getActiveConnectionsCount();

    int getIdleConnectionsCount();

    String getUrl();

    long getSelectQueriesCount();

    long getUpdateQueriesCount();

}
