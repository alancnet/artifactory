package org.artifactory.storage.db.properties.model;

import org.apache.commons.lang.StringUtils;

/**
 * Version and state of the database
 * <p/>
 * Date: 7/10/13 3:02 PM
 *
 * @author freds
 */
public class DbProperties {
    private final long installationDate;
    private final String artifactoryVersion;
    private final int artifactoryRevision;
    private final long artifactoryRelease;

    public DbProperties(long installationDate, String artifactoryVersion, int artifactoryRevision,
            long artifactoryRelease) {
        if (installationDate <= 0L) {
            throw new IllegalArgumentException("Installation date cannot be zero or negative!");
        }
        if (StringUtils.isBlank(artifactoryVersion)) {
            throw new IllegalArgumentException(
                    "Artifactory version and Artifactory running mode cannot be empty or null!");
        }
        this.installationDate = installationDate;
        this.artifactoryVersion = artifactoryVersion;
        this.artifactoryRevision = artifactoryRevision;
        this.artifactoryRelease = artifactoryRelease;
    }

    public long getInstallationDate() {
        return installationDate;
    }

    public String getArtifactoryVersion() {
        return artifactoryVersion;
    }

    public int getArtifactoryRevision() {
        return artifactoryRevision;
    }

    public long getArtifactoryRelease() {
        return artifactoryRelease;
    }
}
