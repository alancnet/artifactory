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

package org.artifactory.maven;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoUtils;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.fs.ItemInfo;
import org.artifactory.maven.snapshot.BuildNumberSnapshotComparator;
import org.artifactory.maven.snapshot.SnapshotComparator;
import org.artifactory.maven.versioning.MavenMetadataVersionComparator;
import org.artifactory.maven.versioning.VersionNameMavenMetadataVersionComparator;
import org.artifactory.mime.MavenNaming;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.fs.tree.ItemNode;
import org.artifactory.storage.fs.tree.ItemNodeFilter;
import org.artifactory.storage.fs.tree.ItemTree;
import org.artifactory.util.RepoLayoutUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Calculates maven metadata recursively for folder in a local non-cache repository. Plugins metadata is calculated for
 * the whole repository.
 *
 * @author Yossi Shaul
 */
public class MavenMetadataCalculator extends AbstractMetadataCalculator {
    private static final Logger log = LoggerFactory.getLogger(MavenMetadataCalculator.class);
    private static Predicate<? super ItemNode> uniqueSnapshotFileOnly = new Predicate<ItemNode>() {
        @Override
        public boolean apply(@Nullable ItemNode pom) {
            if (pom != null) {
                return MavenNaming.isUniqueSnapshotFileName(pom.getName());
            }
            return false;
        }
    };
    private final RepoPath baseFolder;
    private final boolean recursive;

    /**
     * Creates new instance of maven metadata calculator.
     *
     * @param baseFolder Folder to calculate metadata for
     * @param recursive  True if the calculator should recursively calculate maven metadata for all sub folders
     */
    public MavenMetadataCalculator(RepoPath baseFolder, boolean recursive) {
        this.baseFolder = baseFolder;
        this.recursive = recursive;
    }

    public static MavenMetadataVersionComparator createVersionComparator() {
        String comparatorFqn = ConstantValues.mvnMetadataVersionsComparator.getString();
        if (StringUtils.isBlank(comparatorFqn)) {
            // return the default comparator
            return VersionNameMavenMetadataVersionComparator.get();
        }

        try {
            Class<?> comparatorClass = Class.forName(comparatorFqn);
            return (MavenMetadataVersionComparator) comparatorClass.newInstance();
        } catch (Exception e) {
            log.warn("Failed to create custom maven metadata version comparator '{}': {}", comparatorFqn,
                    e.getMessage());
            return VersionNameMavenMetadataVersionComparator.get();
        }
    }

    public static SnapshotComparator createSnapshotComparator() {
        SnapshotComparator comparator = BuildNumberSnapshotComparator.get();
        // Try to load costume comparator
        String comparatorFqn = ConstantValues.mvnMetadataSnapshotComparator.getString();
        if (!StringUtils.isBlank(comparatorFqn)) {
            try {
                Class comparatorClass = Class.forName(comparatorFqn);
                Method get = comparatorClass.getMethod("get");
                comparator = (SnapshotComparator) get.invoke(null);
                log.debug("Using costume snapshot comparator '{}' to calculate the latest snapshot", comparatorFqn);
            } catch (NoSuchMethodException e1) {
                log.warn(
                        "Failed to create custom maven metadata snapshot comparator, the comparator should contain" +
                                " static get method to avoid unnecessary object creation '{}': {}", comparatorFqn,
                        e1.getMessage());
            } catch (Exception e) {
                log.warn("Failed to create custom maven metadata snapshot comparator '{}': {}", comparatorFqn,
                        e.getMessage());
            }
        }
        return comparator;
    }

    /**
     * Starts calculation of the maven metadata from the base repo path
     *
     * @return Status of the metadata calculation
     */
    public BasicStatusHolder calculate() {
        log.debug("Calculating maven metadata recursively on '{}'", baseFolder);

        ItemTree itemTree = new ItemTree(baseFolder, new ItemNodeFilter() {
            @Override
            public boolean accepts(ItemInfo item) {
                if (item.isFolder()) {
                    return true;
                }
                String path = item.getRepoPath().getPath();
                return MavenNaming.isPom(path) || MavenNaming.isUniqueSnapshot(path);
            }
        });
        ItemNode rootNode = itemTree.getRootNode();
        if (rootNode != null) {
            calculateAndSet(rootNode);
            log.debug("Finished {} maven metadata calculation on '{}'",
                    (recursive ? "recursive" : "non recursive"), baseFolder);
        } else {
            log.debug("Root path for metadata calculation not found: {}", baseFolder);
        }
        return status;
    }

    private void calculateAndSet(ItemNode treeNode) {
        ItemInfo itemInfo = treeNode.getItemInfo();
        if (!itemInfo.isFolder()) {
            // Nothing to do here for non folder tree node
            return;
        }

        RepoPath repoPath = itemInfo.getRepoPath();

        String nodePath = repoPath.getPath();
        boolean containsMetadataInfo;
        if (MavenNaming.isSnapshot(nodePath)) {
            // if this folder contains snapshots create snapshots maven.metadata
            log.trace("Detected snapshots container: {}", nodePath);
            containsMetadataInfo = createSnapshotsMetadata(repoPath, treeNode);
        } else {
            // if this folder contains "version folders" create versions maven metadata
            List<ItemNode> subFoldersContainingPoms = getSubFoldersContainingPoms(treeNode);
            if (!subFoldersContainingPoms.isEmpty()) {
                log.trace("Detected versions container: {}", repoPath.getId());
                createVersionsMetadata(repoPath, subFoldersContainingPoms);
                containsMetadataInfo = true;
            } else {
                containsMetadataInfo = false;
            }
        }

        if (!containsMetadataInfo) {
            // note: this will also remove plugins metadata. not sure it should
            removeMetadataIfExist(repoPath);
        }

        // Recursive call to calculate and set if recursive calc is on
        if (recursive && itemInfo.isFolder()) {
            List<ItemNode> children = treeNode.getChildren();
            if (children != null) {
                for (ItemNode child : children) {
                    calculateAndSet(child);
                }
            }
        }
    }

    private boolean createSnapshotsMetadata(RepoPath repoPath, ItemNode treeNode) {
        if (!folderContainsPoms(treeNode)) {
            return false;
        }
        List<ItemNode> folderItems = treeNode.getChildren();
        Iterable<ItemNode> poms = Iterables.filter(folderItems, new Predicate<ItemNode>() {
            @Override
            public boolean apply(@Nullable ItemNode input) {
                return (input != null) && MavenNaming.isPom(input.getItemInfo().getName());
            }
        });

        RepoPath firstPom = poms.iterator().next().getRepoPath();
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(firstPom);
        if (!artifactInfo.isValid()) {
            return true;
        }
        Metadata metadata = new Metadata();
        metadata.setGroupId(artifactInfo.getGroupId());
        metadata.setArtifactId(artifactInfo.getArtifactId());
        metadata.setVersion(artifactInfo.getVersion());
        Versioning versioning = new Versioning();
        metadata.setVersioning(versioning);
        versioning.setLastUpdatedTimestamp(new Date());
        Snapshot snapshot = new Snapshot();
        versioning.setSnapshot(snapshot);

        LocalRepoDescriptor localRepoDescriptor =
                getRepositoryService().localOrCachedRepoDescriptorByKey(repoPath.getRepoKey());
        SnapshotVersionBehavior snapshotBehavior = localRepoDescriptor.getSnapshotVersionBehavior();
        String latestUniquePom = getLatestUniqueSnapshotPomName(poms);
        if (snapshotBehavior.equals(SnapshotVersionBehavior.NONUNIQUE) ||
                (snapshotBehavior.equals(SnapshotVersionBehavior.DEPLOYER) && latestUniquePom == null)) {
            snapshot.setBuildNumber(1);
        } else if (snapshotBehavior.equals(SnapshotVersionBehavior.UNIQUE)) {
            // take the latest unique snapshot file file
            if (latestUniquePom != null) {
                snapshot.setBuildNumber(MavenNaming.getUniqueSnapshotVersionBuildNumber(latestUniquePom));
                snapshot.setTimestamp(MavenNaming.getUniqueSnapshotVersionTimestamp(latestUniquePom));
            }

            if (ConstantValues.mvnMetadataVersion3Enabled.getBoolean()) {
                List<SnapshotVersion> snapshotVersions = Lists.newArrayList(getFolderItemSnapshotVersions(folderItems));
                if (!snapshotVersions.isEmpty()) {
                    versioning.setSnapshotVersions(snapshotVersions);
                }
            }
        }
        saveMetadata(repoPath, metadata);
        return true;
    }

    private Collection<SnapshotVersion> getFolderItemSnapshotVersions(Collection<ItemNode> folderItems) {
        List<SnapshotVersion> snapshotVersionsToReturn = Lists.newArrayList();

        Map<SnapshotVersionType, ModuleInfo> latestSnapshotVersions = Maps.newHashMap();

        for (ItemNode folderItem : folderItems) {
            String folderItemPath = folderItem.getItemInfo().getRelPath();
            if (MavenNaming.isUniqueSnapshot(folderItemPath)) {
                ModuleInfo folderItemModuleInfo;
                if (MavenNaming.isPom(folderItemPath)) {
                    folderItemModuleInfo = ModuleInfoUtils.moduleInfoFromDescriptorPath(folderItemPath,
                            RepoLayoutUtils.MAVEN_2_DEFAULT);
                } else {
                    folderItemModuleInfo = ModuleInfoUtils.moduleInfoFromArtifactPath(folderItemPath,
                            RepoLayoutUtils.MAVEN_2_DEFAULT);
                }
                if (!folderItemModuleInfo.isValid() || !folderItemModuleInfo.isIntegration()) {
                    continue;
                }
                SnapshotVersionType folderItemSnapshotVersionType = new SnapshotVersionType(
                        folderItemModuleInfo.getExt(), folderItemModuleInfo.getClassifier());
                if (latestSnapshotVersions.containsKey(folderItemSnapshotVersionType)) {
                    SnapshotComparator snapshotComparator = createSnapshotComparator();
                    ModuleInfo latestSnapshotVersion = latestSnapshotVersions.get(folderItemSnapshotVersionType);
                    if (snapshotComparator.compare(folderItemModuleInfo, latestSnapshotVersion) > 0) {
                        latestSnapshotVersions.put(folderItemSnapshotVersionType, folderItemModuleInfo);
                    }
                } else {
                    latestSnapshotVersions.put(folderItemSnapshotVersionType, folderItemModuleInfo);
                }
            }
        }

        for (ModuleInfo latestSnapshotVersion : latestSnapshotVersions.values()) {
            SnapshotVersion snapshotVersion = new SnapshotVersion();
            snapshotVersion.setClassifier(latestSnapshotVersion.getClassifier());
            snapshotVersion.setExtension(latestSnapshotVersion.getExt());

            String fileItegRev = latestSnapshotVersion.getFileIntegrationRevision();
            snapshotVersion.setVersion(latestSnapshotVersion.getBaseRevision() + "-" + fileItegRev);
            snapshotVersion.setUpdated(StringUtils.remove(StringUtils.substringBefore(fileItegRev, "-"), '.'));
            snapshotVersionsToReturn.add(snapshotVersion);
        }

        return snapshotVersionsToReturn;
    }

    private void createVersionsMetadata(RepoPath repoPath, List<ItemNode> versionNodes) {
        // get artifact info from the first pom

        RepoPath samplePomRepoPath = getFirstPom(versionNodes);
        if (samplePomRepoPath == null) {
            //Should never really be null, we've checked the list of version nodes for poms before passing it into here
            return;
        }
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(samplePomRepoPath);
        if (!artifactInfo.isValid()) {
            return;
        }
        Metadata metadata = new Metadata();
        metadata.setGroupId(artifactInfo.getGroupId());
        metadata.setArtifactId(artifactInfo.getArtifactId());
        metadata.setVersion(artifactInfo.getVersion());
        Versioning versioning = new Versioning();
        metadata.setVersioning(versioning);
        versioning.setLastUpdatedTimestamp(new Date());

        MavenMetadataVersionComparator comparator = createVersionComparator();
        TreeSet<ItemNode> sortedVersions = Sets.newTreeSet(comparator);
        sortedVersions.addAll(versionNodes);

        // add the versions to the versioning section
        for (ItemNode sortedVersion : sortedVersions) {
            versioning.addVersion(sortedVersion.getName());
        }

        // latest is simply the last (be it snapshot or release version)
        String latestVersion = sortedVersions.last().getName();
        versioning.setLatest(latestVersion);

        // release is the latest non snapshot version
        for (ItemNode sortedVersion : sortedVersions) {
            String versionNodeName = sortedVersion.getName();
            if (!MavenNaming.isSnapshot(versionNodeName)) {
                versioning.setRelease(versionNodeName);
            }
        }

        saveMetadata(repoPath, metadata);
    }

    private RepoPath getFirstPom(List<ItemNode> versionNodes) {
        for (ItemNode versionNode : versionNodes) {
            List<ItemNode> children = versionNode.getChildren();
            for (ItemNode treeNode : children) {
                if (MavenNaming.isPom(treeNode.getName())) {
                    return treeNode.getRepoPath();
                }
            }
        }

        return null;
    }

    private String getLatestUniqueSnapshotPomName(Iterable<ItemNode> poms) {
        // Get Default Comparator
        Comparator<ItemNode> comparator = createSnapshotComparator();
        ArrayList<ItemNode> list = Lists.newArrayList(poms);
        list = Lists.newArrayList(Iterables.filter(list, uniqueSnapshotFileOnly));
        Collections.sort(list, comparator);
        String name = list.size() > 0 ? list.get(list.size() - 1).getName() : null;
        return name;
    }

    private List<ItemNode> getSubFoldersContainingPoms(ItemNode treeNode) {
        List<ItemNode> result = Lists.newArrayList();

        if (treeNode.isFolder()) {
            List<ItemNode> children = treeNode.getChildren();
            for (ItemNode child : children) {
                if (folderContainsPoms(child)) {
                    result.add(child);
                }
            }
        }

        return result;
    }

    private boolean folderContainsPoms(ItemNode treeNode) {
        if (!treeNode.isFolder()) {
            return false;
        }

        List<ItemNode> children = treeNode.getChildren();
        for (ItemNode child : children) {
            if (!child.isFolder() && MavenNaming.isPom(child.getName())) {
                return true;
            }
        }

        return false;
    }

    private void removeMetadataIfExist(RepoPath repoPath) {
        try {
            RepoPathImpl mavenMetadataPath = new RepoPathImpl(repoPath, MavenNaming.MAVEN_METADATA_NAME);
            if (getRepositoryService().exists(mavenMetadataPath)) {
                boolean delete = true;
                String metadataStr = getRepositoryService().getStringContent(mavenMetadataPath);
                try {
                    Metadata metadata = MavenModelUtils.toMavenMetadata(metadataStr);
                    if (isSnapshotMavenMetadata(metadata) && !MavenNaming.isSnapshot(repoPath.getPath())) {
                        // RTFACT-6242 - don't delete user deployed maven-metadata (maven 2 bug)
                        delete = false;
                    }
                } catch (IOException e) {
                    // ignore -> delete
                }
                if (delete) {
                    log.debug("Deleting {}", mavenMetadataPath);
                    getRepositoryService().undeploy(mavenMetadataPath, false, false);
                }
            }
        } catch (Exception e) {
            status.error("Error while removing maven metadata from " + repoPath + ".", e, log);
        }
    }

    private boolean isSnapshotMavenMetadata(Metadata metadata) {
        Versioning versioning = metadata.getVersioning();
        if (versioning == null) {
            return false;
        }
        List<SnapshotVersion> snapshots = versioning.getSnapshotVersions();
        return snapshots != null && !snapshots.isEmpty();
    }

    private static class SnapshotVersionType {

        private String extension;
        private String classifier;

        private SnapshotVersionType(String extension, String classifier) {
            this.extension = extension;
            this.classifier = classifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SnapshotVersionType)) {
                return false;
            }

            SnapshotVersionType that = (SnapshotVersionType) o;

            if (classifier != null ? !classifier.equals(that.classifier) : that.classifier != null) {
                return false;
            }
            if (extension != null ? !extension.equals(that.extension) : that.extension != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = extension != null ? extension.hashCode() : 0;
            result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
            return result;
        }
    }
}
