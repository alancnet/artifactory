package org.artifactory.storage.db.version.converter;

import org.artifactory.storage.db.DbType;
import org.artifactory.storage.db.util.JdbcHelper;

/**
 *
 */
public interface DBConverter {
    void convert(JdbcHelper jdbcHelper, DbType dbType);
}
