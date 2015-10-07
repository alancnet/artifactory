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

package org.artifactory.repo.virtual.interceptor;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.artifactory.common.ConstantValues;
import org.artifactory.fs.RepoResource;
import org.artifactory.maven.MavenMetadataCalculator;
import org.artifactory.maven.snapshot.SnapshotComparator;
import org.artifactory.maven.versioning.MavenVersionComparator;
import org.artifactory.mime.MavenNaming;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author Noam Y. Tenne
 */
class MergeableMavenMetadata {
    private static final Logger log = LoggerFactory.getLogger(MergeableMavenMetadata.class);

    private Metadata metadata;
    private long lastModified;
    private boolean mergeSnapshotVersions;

    public MergeableMavenMetadata(InternalRequestContext context) {
        boolean v3MetadataEnabled = ConstantValues.mvnMetadataVersion3Enabled.getBoolean();
        mergeSnapshotVersions = v3MetadataEnabled && MavenNaming.isSnapshotMavenMetadata(context.getResourcePath()) &&
                context.clientSupportsM3SnapshotVersions();
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void merge(Metadata otherMetadata, RepoResource foundResource) {
        long otherLastModified = foundResource.getLastModified();
        if (metadata == null) {
            metadata = otherMetadata;
            lastModified = otherLastModified;
            if (!mergeSnapshotVersions) {
                Versioning versioning = metadata.getVersioning();
                if (versioning != null) {
                    versioning.setSnapshotVersions(null);
                }
            }
        } else {
            metadata.merge(otherMetadata);
            lastModified = Math.max(otherLastModified, lastModified);

            Versioning existingVersioning = metadata.getVersioning();
            if (existingVersioning != null) {
                List<String> versions = existingVersioning.getVersions();
                if (!CollectionUtils.isNullOrEmpty(versions)) {
                    try {
                        Collections.sort(versions, new MavenVersionComparator());
                    } catch (IllegalArgumentException e) {
                        // New Java 7 TimSort is pointing out the non transitive behavior
                        // of the Mercury version comparator => Doing fallback to natural string order
                        log.info(
                                "Hitting Mercury version comparator non transitive behavior message='" + e.getMessage() + "'");
                        if (log.isDebugEnabled()) {
                            log.debug("The lists of versions is: " + versions);
                        }
                        Collections.sort(versions);
                    }
                    // latest is simply the last (be it snapshot or release version)
                    String latestVersion = versions.get(versions.size() - 1);
                    existingVersioning.setLatest(latestVersion);

                    // release is the latest non snapshot version
                    for (String version : versions) {
                        if (!MavenNaming.isSnapshot(version)) {
                            existingVersioning.setRelease(version);
                        }
                    }
                }
                SnapshotComparator comparator = MavenMetadataCalculator.createSnapshotComparator();
                // if there's a unique snapshot version prefer the one with the bigger build number
                Snapshot snapshot = existingVersioning.getSnapshot();
                Versioning otherMetadataVersioning = otherMetadata.getVersioning();
                if (otherMetadataVersioning != null) {
                    Snapshot otherSnapshot = otherMetadataVersioning.getSnapshot();
                    if (snapshot != null && otherSnapshot != null) {
                        if (comparator.compare(otherSnapshot, snapshot) > 0) {
                            snapshot.setBuildNumber(otherSnapshot.getBuildNumber());
                            snapshot.setTimestamp(otherSnapshot.getTimestamp());
                        }
                    }
                    if (mergeSnapshotVersions) {
                        addSnapshotVersions(existingVersioning, otherMetadataVersioning);
                    }
                }
            }
        }
    }

    private void addSnapshotVersions(Versioning existingVersioning, Versioning otherMetadataVersioning) {
        //Maven metadata merge function does not include snapshot version (https://jira.codehaus.org/browse/MNG-5180)
        SnapshotComparator comparator = MavenMetadataCalculator.createSnapshotComparator();
        List<SnapshotVersion> otherSnapshotVersions = otherMetadataVersioning.getSnapshotVersions();
        if ((otherSnapshotVersions != null) && !otherSnapshotVersions.isEmpty()) {

            List<SnapshotVersion> existingSnapshotVersions = existingVersioning.getSnapshotVersions();
            if ((existingSnapshotVersions == null) || existingSnapshotVersions.isEmpty()) {
                existingVersioning.setSnapshotVersions(otherSnapshotVersions);
            } else {
                for (SnapshotVersion otherSnapshotVersion : otherSnapshotVersions) {
                    for (SnapshotVersion existingSnapshotVersion : existingSnapshotVersions) {
                        if (snapshotVersionClassifiersEqual(otherSnapshotVersion, existingSnapshotVersion) &&
                                snapshotVersionExtensionEqual(otherSnapshotVersion, existingSnapshotVersion)) {
                            if (comparator.compare(otherSnapshotVersion, existingSnapshotVersion) > 0) {
                                existingSnapshotVersion.setUpdated(otherSnapshotVersion.getUpdated());
                                existingSnapshotVersion.setVersion(otherSnapshotVersion.getVersion());
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean snapshotVersionClassifiersEqual(SnapshotVersion otherSnapshotVersion,
            SnapshotVersion existingSnapshotVersion) {
        String existingSnapshotClassifier = existingSnapshotVersion.getClassifier();
        String otherSnapshotClassifier = otherSnapshotVersion.getClassifier();
        return ((StringUtils.isBlank(otherSnapshotClassifier) && StringUtils.isBlank(existingSnapshotClassifier)) ||
                (StringUtils.isNotBlank(otherSnapshotClassifier) &&
                        otherSnapshotClassifier.equals(existingSnapshotClassifier)));
    }

    private boolean snapshotVersionExtensionEqual(SnapshotVersion otherSnapshotVersion,
            SnapshotVersion existingSnapshotVersion) {
        String existingSnapshotExtension = existingSnapshotVersion.getExtension();
        String otherSnapshotExtension = otherSnapshotVersion.getExtension();
        return ((StringUtils.isBlank(otherSnapshotExtension) && StringUtils.isBlank(existingSnapshotExtension)) ||
                (StringUtils.isNotBlank(otherSnapshotExtension) &&
                        otherSnapshotExtension.equals(existingSnapshotExtension)));
    }
}
