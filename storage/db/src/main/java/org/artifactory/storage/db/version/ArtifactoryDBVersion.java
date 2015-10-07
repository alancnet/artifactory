package org.artifactory.storage.db.version;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.DbType;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.version.converter.DBConverter;
import org.artifactory.storage.db.version.converter.DBSqlConverter;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.VersionComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Artifactory DB version
 */
public enum ArtifactoryDBVersion {
    v100(ArtifactoryVersion.v300, ArtifactoryVersion.v304),
    v101(ArtifactoryVersion.v310, ArtifactoryVersion.v310, new DBSqlConverter("v310")),
    v102(ArtifactoryVersion.v311, ArtifactoryVersion.v402, new DBSqlConverter("v311")),
    v103(ArtifactoryVersion.v410, ArtifactoryVersion.getCurrent(), new DBSqlConverter("v410"));
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryDBVersion.class);


    private final VersionComparator comparator;
    private final DBConverter[] converters;

    ArtifactoryDBVersion(ArtifactoryVersion from, ArtifactoryVersion until, DBConverter... converters) {
        this.comparator = new VersionComparator(from, until);
        this.converters = converters;
    }

    public static ArtifactoryDBVersion getLast() {
        ArtifactoryDBVersion[] versions = ArtifactoryDBVersion.values();
        return versions[versions.length - 1];
    }

    public static void convert(ArtifactoryVersion from, JdbcHelper jdbcHelper, DbType dbType) {
        // All converters of versions above me needs to be executed in sequence
        List<DBConverter> converters = Lists.newArrayList();
        for (ArtifactoryDBVersion version : ArtifactoryDBVersion.values()) {
            if (version.comparator.isAfter(from) && !version.comparator.supports(from)) {
                for (DBConverter dbConverter : version.getConverters()) {
                    converters.add(dbConverter);
                }
            }
        }

        if (converters.isEmpty()) {
            log.debug("No database converters found between version {} and {}", from, ArtifactoryVersion.getCurrent());
        } else {
            log.info("Starting database conversion from {} to {}", from, ArtifactoryVersion.getCurrent());
            for (DBConverter converter : converters) {
                converter.convert(jdbcHelper, dbType);
            }
            log.info("Finished database conversion from {} to {}", from, ArtifactoryVersion.getCurrent());
        }
    }

    public DBConverter[] getConverters() {
        return converters;
    }

    public VersionComparator getComparator() {
        return comparator;
    }
}
