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

package org.artifactory.repo.snapshot;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoBuilder;
import org.artifactory.api.module.ModuleInfoUtils;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.maven.MavenModelUtils;
import org.artifactory.mime.MavenNaming;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.PathUtils;
import org.artifactory.util.RepoLayoutUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This snapshot version adapter changes non-unique snapshot versions to unique.
 *
 * @author Yossi Shaul
 */
public class UniqueSnapshotVersionAdapter extends SnapshotVersionAdapterBase {
    private static final Logger log = LoggerFactory.getLogger(UniqueSnapshotVersionAdapter.class);

    @Override
    protected String adapt(MavenSnapshotVersionAdapterContext context) {
        String path = context.getRepoPath().getPath();

        String fileName = PathUtils.getFileName(path);
        if (MavenNaming.isUniqueSnapshotFileName(fileName)) {
            log.debug("File '{}' has already a unique snapshot version. Returning the original path.", fileName);
            return path;
        }

        String pathBaseVersion = context.getModuleInfo().getBaseRevision();
        if (!fileName.contains(pathBaseVersion + "-" + MavenNaming.SNAPSHOT)) {
            log.debug("File '{}' doesn't contain the non-unique snapshot version {}. " +
                    "Returning the original path.", fileName, pathBaseVersion);
            return path;
        }

        // Replace 'SNAPSHOT' with version timestamp for unique snapshot repo
        return adjustNonUniqueSnapshotToUnique(context);
    }

    private String adjustNonUniqueSnapshotToUnique(MavenSnapshotVersionAdapterContext context) {
        long timestamp = context.getTimestamp();
        if (timestamp > 0) {
            return adjustUsingClientTimestamp(context);
        } else if (MavenNaming.isChecksum(context.getRepoPath().getPath())) {
            return adjustChecksum(context);
        } else {
            return adjustUsingServerData(context);
        }
    }

    private String adjustChecksum(MavenSnapshotVersionAdapterContext context) {
        // find latest unique file matching the checksum coordinates
        RepositoryService repoService = ContextHelper.get().getRepositoryService();
        RepoPath repoPath = context.getRepoPath();
        RepoPath parentRepoPath = repoPath.getParent();
        RepoDescriptor repoDescriptor = repoService.repoDescriptorByKey(parentRepoPath.getRepoKey());
        RepoLayout repoLayout = repoDescriptor.getRepoLayout();

        String latestMatching = null;

        String originalChecksumRequestPath = repoPath.getPath();
        String originalRequestPathWithNoChecksum = PathUtils.stripExtension(originalChecksumRequestPath);

        if (repoService.exists(parentRepoPath)) {
            List<String> children = repoService.getChildrenNames(parentRepoPath);
            for (String child : children) {
                if (MavenNaming.isUniqueSnapshotFileName(child)) {

                    ModuleInfo childModule =
                            repoService.getItemModuleInfo(InternalRepoPathFactory.create(parentRepoPath, child));
                    String fileRevisionIntegration = childModule.getFileIntegrationRevision();

                    //Try to construct a new non-unique path as a descriptor
                    String nonUniquePath = replaceIntegration(
                            ModuleInfoUtils.constructDescriptorPath(childModule, repoLayout, true),
                            fileRevisionIntegration);

                    //If the path as a descriptor doesn't match, perhaps it's an artifact path
                    if (!nonUniquePath.equals(originalRequestPathWithNoChecksum)) {
                        //Try to construct a new non-unique path as an artifact
                        nonUniquePath = replaceIntegration(
                                ModuleInfoUtils.constructArtifactPath(childModule, repoLayout),
                                fileRevisionIntegration);
                    }

                    if (nonUniquePath.equals(originalRequestPathWithNoChecksum)) {
                        if (latestMatching == null ||
                                MavenNaming.getUniqueSnapshotVersionBuildNumber(latestMatching) <
                                        MavenNaming.getUniqueSnapshotVersionBuildNumber(child)) {
                            latestMatching = child;
                        }
                    }
                }
            }
        }

        // if latest not found, return invalid path which will fail and return a message to the client
        String timestamp = latestMatching != null ?
                MavenNaming.getUniqueSnapshotVersionTimestamp(latestMatching) : System.currentTimeMillis() + "";
        int buildNumber = latestMatching != null ?
                MavenNaming.getUniqueSnapshotVersionBuildNumber(latestMatching) : 0;

        // use the timestamp and build number from it. if not found return something that will fail?
        return buildUniqueSnapshotFileName(buildNumber, timestamp, context.getModuleInfo());
    }

    private String replaceIntegration(String constructedPath, String artifactRevision) {
        return constructedPath.replace(artifactRevision, "SNAPSHOT");
    }

    private String adjustUsingServerData(MavenSnapshotVersionAdapterContext context) {
        //Get the latest build number from the metadata
        RepoPath repoPath = context.getRepoPath();
        String filePath = repoPath.getPath();
        int metadataBuildNumber = getLastBuildNumber(repoPath);
        int nextBuildNumber = metadataBuildNumber + 1;

        RepoPath parentPath = repoPath.getParent();

        // determine if the next build number should be the one read from the metadata
        ModuleInfo moduleInfo = context.getModuleInfo();
        String classifier = moduleInfo.getClassifier();
        boolean isPomChecksum = MavenNaming.isChecksum(filePath) && MavenNaming.isPom(
                PathUtils.stripExtension(filePath));
        if (metadataBuildNumber > 0 && (StringUtils.isNotBlank(classifier) || isPomChecksum)) {
            // pom checksums and artifacts with classifier are always deployed after the pom (which triggers the
            // maven-metadata.xml calculation) so use the metadata build number
            nextBuildNumber = metadataBuildNumber;
        }
        if (metadataBuildNumber > 0 && MavenNaming.isPom(filePath)) {
            // the metadata might already represent an existing main artifact (in case the
            // maven-metadata.xml was deployed after the main artifact and before the pom/classifier)
            // so first check if there's already a file with the next build number
            if (findSnapshotFileByBuildNumber(parentPath, metadataBuildNumber + 1) == null) {
                // no files deployed with the next build number so either this is a pom deployment (parent pom)
                // or this is a pom of a main artifact for which the maven-metadata.xml was deployed before this pom
                if (findSnapshotPomFile(parentPath, metadataBuildNumber) == null) {
                    // this is a pom attached to a main artifact deployed after maven-metadata.xml
                    nextBuildNumber = metadataBuildNumber;
                }
            }
        }

        String timestamp = getSnapshotTimestamp(parentPath, nextBuildNumber);
        if (timestamp == null) {
            // probably the first deployed file for this build, use now for the timestamp
            timestamp = MavenModelUtils.dateToUniqueSnapshotTimestamp(System.currentTimeMillis());
        }
        return buildUniqueSnapshotFileName(nextBuildNumber, timestamp, moduleInfo);
    }

    private String adjustUsingClientTimestamp(MavenSnapshotVersionAdapterContext context) {
        // artifact was uploaded with a timestamp, we use it for the unique snapshot timestamp and to locate build number
        long timestamp = context.getTimestamp();
        String uniqueTimestamp = MavenModelUtils.dateToUniqueSnapshotTimestamp(timestamp);
        RepoPath repoPath = context.getRepoPath();
        String existingArtifact = findSnapshotFileByTimestamp(repoPath.getParent(), uniqueTimestamp);
        int buildNumber;
        if (existingArtifact != null) {
            buildNumber = MavenNaming.getUniqueSnapshotVersionBuildNumber(existingArtifact);
        } else {
            buildNumber = getLastBuildNumber(repoPath) + 1;
        }

        return buildUniqueSnapshotFileName(buildNumber, uniqueTimestamp, context.getModuleInfo());
    }

    private String buildUniqueSnapshotFileName(int nextBuildNumber, String timestamp, ModuleInfo moduleInfo) {
        // replace the SNAPSHOT string with timestamp-buildNumber
        ModuleInfo transformedModuleInfo = new ModuleInfoBuilder(moduleInfo).fileIntegrationRevision(
                timestamp + "-" + nextBuildNumber).build();
        return ModuleInfoUtils.constructArtifactPath(transformedModuleInfo, RepoLayoutUtils.MAVEN_2_DEFAULT);
    }

    /**
     * @return The last build number for snapshot version. 0 if maven-metadata not found for the path.
     */
    private int getLastBuildNumber(RepoPath repoPath) {
        int buildNumber = 0;
        try {
            // get the parent path which should contains the maven-metadata
            RepoPath parentRepoPath = repoPath.getParent();
            RepositoryService repoService = ContextHelper.get().getRepositoryService();
            RepoPathImpl mavenMetadataPath = new RepoPathImpl(parentRepoPath, MavenNaming.MAVEN_METADATA_NAME);
            if (repoService.exists(mavenMetadataPath)) {
                String mavenMetadataStr = repoService.getStringContent(mavenMetadataPath);
                Metadata metadata = MavenModelUtils.toMavenMetadata(mavenMetadataStr);
                Versioning versioning = metadata.getVersioning();
                if (versioning != null) {
                    Snapshot snapshot = versioning.getSnapshot();
                    if (snapshot != null) {
                        buildNumber = snapshot.getBuildNumber();
                    }
                }
            } else {
                // ok probably not found. just log
                log.debug("No maven metadata found for {}.", repoPath);
            }
        } catch (Exception e) {
            log.error("Cannot obtain build number from metadata.", e);
        }
        return buildNumber;
    }

    /**
     * @param snapshotDirectoryPath Path to a repository snapshot directory (eg, /a/path/1.0-SNAPSHOT)
     * @param buildNumber           The file with build number to search for
     * @return The path of the first unique snapshot file with the input build number.
     */
    private String findSnapshotFileByBuildNumber(RepoPath snapshotDirectoryPath, int buildNumber) {
        return findSnapshotFile(snapshotDirectoryPath, buildNumber, null, null);
    }

    /**
     * @param snapshotDirectoryPath Path to a repository snapshot directory (eg, /a/path/1.0-SNAPSHOT)
     * @param timestamp             The file with timestamp to search for
     * @return The path of the first unique snapshot file with the input timestamp.
     */
    private String findSnapshotFileByTimestamp(RepoPath snapshotDirectoryPath, String timestamp) {
        return findSnapshotFile(snapshotDirectoryPath, 0, timestamp, null);
    }

    /**
     * @param snapshotDirectoryPath Path to a repository snapshot directory (eg, /a/path/1.0-SNAPSHOT)
     * @param buildNumber           The file with build number to search for
     * @return The path of the unique snapshot pom file with the input build number.
     */
    private String findSnapshotPomFile(RepoPath snapshotDirectoryPath, int buildNumber) {
        return findSnapshotFile(snapshotDirectoryPath, buildNumber, null, "pom");
    }

    /**
     * @param snapshotDirectoryPath Path to a repository snapshot directory (eg, /a/path/1.0-SNAPSHOT)
     * @param buildNumber           The file with build number to search for or 0 if doesn't matter
     * @param timestamp             The file with timestamp to search for or null if doesn't matter
     * @param fileExtension         The file type to search for. Use null for any type
     * @return The path of the first unique snapshot file with the input build number.
     */
    private String findSnapshotFile(RepoPath snapshotDirectoryPath, int buildNumber, String timestamp,
            String fileExtension) {
        log.debug("Searching for unique snapshot file in {} with build number {} and timestamp {}",
                snapshotDirectoryPath, buildNumber, timestamp);
        RepositoryService repoService = ContextHelper.get().getRepositoryService();
        if (repoService.exists(snapshotDirectoryPath)) {
            List<String> children = repoService.getChildrenNames(snapshotDirectoryPath);
            for (String child : children) {
                if (MavenNaming.isUniqueSnapshotFileName(child)) {
                    // now match against all the conditions
                    boolean buildNumberMatches = buildNumber == 0 || buildNumber == MavenNaming
                            .getUniqueSnapshotVersionBuildNumber(child);
                    boolean timestampMatches = timestamp == null || timestamp
                            .equals(MavenNaming.getUniqueSnapshotVersionTimestamp(child));
                    boolean typeMatches = fileExtension == null || fileExtension.equals(PathUtils.getExtension(child));
                    if (buildNumberMatches && timestampMatches && typeMatches) {
                        // passed all the search requirements...
                        log.debug("Found unique snapshot: {}", child);
                        return child;
                    }
                }
            }
        }
        log.debug("Unique snapshot file not found in {} for build number {}", snapshotDirectoryPath, buildNumber);
        return null;
    }

    /**
     * @param snapshotDirectoryPath Path to a repository snapshot directory (eg, /a/path/1.0-SNAPSHOT)
     * @param buildNumber           The file with build number to search for
     * @return The timestamp of the unique snapshot file with the input build number.
     */
    private String getSnapshotTimestamp(RepoPath snapshotDirectoryPath, int buildNumber) {
        String snapshotFile = findSnapshotFileByBuildNumber(snapshotDirectoryPath, buildNumber);
        if (snapshotFile != null) {
            int childBuildNumber = MavenNaming.getUniqueSnapshotVersionBuildNumber(snapshotFile);
            if (childBuildNumber == buildNumber) {
                String timestamp = MavenNaming.getUniqueSnapshotVersionTimestamp(snapshotFile);
                log.debug("Extracted timestamp {} from {}", timestamp, snapshotFile);
                return timestamp;
            }
        }
        log.debug("Snapshot timestamp not found in {} for build number {}", snapshotDirectoryPath, buildNumber);
        return null;
    }

}
