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

package org.artifactory.repo.db.importexport;

import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.fs.WatcherInfo;
import org.artifactory.md.Properties;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.StoringRepo;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.schedule.TaskService;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.storage.spring.StorageContextHelper;
import org.artifactory.util.PathUtils;
import org.artifactory.util.RepoPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.artifactory.repo.db.importexport.ImportExportAccumulator.ProgressAccumulatorType.EXPORT;

/**
 * Controls the exporting of entire db repository.
 *
 * @author Yossi Shaul
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class DbRepoExportHandler extends DbExportBase {
    private static final Logger log = LoggerFactory.getLogger(DbRepoExportHandler.class);

    private final StoringRepo repo;

    public DbRepoExportHandler(StoringRepo repo, ExportSettings settings) {
        super(new ImportExportAccumulator(repo.getKey(), EXPORT));
        this.repo = repo;
        setExportSettings(settings);
    }

    public void export() {
        File fileSystemBaseDir = settings.getBaseDir();
        long nodeCount = repo.getRepositoryService().getNodesCount(RepoPathUtils.repoRootPath(repo.getKey()));
        String targetExportFolder = fileSystemBaseDir.getAbsolutePath();
        status.status(String.format("%s export started with %d nodes to: '%s'",
                repo.getKey(), nodeCount, targetExportFolder), log);
        try {
            FileUtils.forceMkdir(fileSystemBaseDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create export directory '" + targetExportFolder + "'.", e);
        }
        ItemInfo rootFolder = getFileService().loadItem(new RepoPathImpl(repo.getKey(), ""));
        exportRecursive(rootFolder);
        accumulator.finished();
        status.status(String.format("%s export finished with: %s Items exported (%s files and %s folders), " +
                "%s Item skipped: (%s files and %s folders).Duration: %s IPS: %s Target: '%s'",
                repo.getKey(), accumulator.getSuccessfulItemsCount(), accumulator.getSuccessfulFilesCount(),
                accumulator.getSuccessfulFoldersCount(), accumulator.getSkippedItemsCount(),
                accumulator.getSkippedFilesCount(), accumulator.getSkippedFoldersCount(),
                accumulator.getDurationString(), accumulator.getItemsPerSecond(), targetExportFolder), log);
    }

    private void exportRecursive(ItemInfo sourceItem) {
        TaskService taskService = InternalContextHelper.get().getTaskService();
        //Check if we need to break/pause
        boolean stop = taskService.pauseOrBreak();
        if (stop) {
            status.error("Export of " + repo.getKey() + " was stopped.", log);
            return;
        }

        try {
            if (sourceItem.isFolder()) {
                if (isStorableFolder(sourceItem.getName())) {
                    exportFolder((FolderInfo) sourceItem);
                }
            } else {
                if (isStorableFile(sourceItem.getName())) {
                    exportFile((FileInfo) sourceItem);
                }
            }
        } catch (Exception e) {
            //If a child export fails, we collect the error but not fail the whole export
            File exportDir = settings.getBaseDir();
            String msg = String.format("Export error: from: %s to: %s reason: %s", sourceItem.getRepoPath(),
                    exportDir != null ? exportDir.getPath() : "null", e.getMessage());
            if (sourceItem.isFolder()) {
                accumulator.accumulateSkippedFolder();
            } else {
                accumulator.accumulateSkippedFile();
            }
            status.error(msg, e, log);
        }
    }

    private void exportFolder(FolderInfo sourceFolder) throws IOException {
        File targetDir = new File(settings.getBaseDir(), sourceFolder.getRelPath());
        status.debug("Exporting directory '" + sourceFolder.getRepoPath() + "'...", log);
        // Insure that the source folder still exists.
        boolean sourceFolderExists = getFileService().exists(sourceFolder.getRepoPath());
        if (!sourceFolderExists) {
            log.info("Skipping folder export : '{}', the source folder doesn't exists.", sourceFolder.getRepoPath());
            return;
        }
        FileUtils.forceMkdir(targetDir);

        targetDir.setLastModified(sourceFolder.getLastModified());

        if (settings.isIncludeMetadata()) {
            exportMetadata(targetDir, sourceFolder);
        }

        accumulator.accumulateSuccessfulFolder();

        List<ItemInfo> children = getRepositoryService().getChildren(sourceFolder.getRepoPath());
        for (ItemInfo child : children) {
            exportRecursive(child);
        }

        if (settings.isIncremental()) {
            cleanupIncrementalBackupDirectory(sourceFolder, children, targetDir);
        }
    }

    //TORE: [by YS] this requires a nice refactoring
    private void cleanupIncrementalBackupDirectory(FolderInfo sourceFolder, List<ItemInfo> currentFolderChildren,
            File targetDir) {

        //Metadata File filter
        IOFileFilter metadataFilter = new MetadataFileFilter();

        //List all artifacts
        Collection<File> artifacts = Sets.newHashSet(
                targetDir.listFiles((FileFilter) new NotFileFilter(metadataFilter)));
        cleanArtifacts(currentFolderChildren, artifacts);

        //List all sub-target metadata
        Collection<File> subTargetMetadataFiles = FileUtils.listFiles(targetDir, metadataFilter,
                DirectoryFileFilter.INSTANCE);
        cleanMetadata(currentFolderChildren, subTargetMetadataFiles);

        //List all target metadata
        File targetDirMetadataContainerFolder = getMetadataContainerFolder(targetDir);
        Collection<File> targetMetadataFiles = FileUtils.listFiles(targetDirMetadataContainerFolder, metadataFilter,
                DirectoryFileFilter.INSTANCE);
        cleanTargetMetadata(sourceFolder, targetMetadataFiles);
    }

    /**
     * Locates the artifacts that were removed from the repo since last backup, but still remain in the backup folder
     * and clean them out.
     *
     * @param currentVfsFolderItems List of vfs items in the current vfs folder
     * @param artifacts             List of artifact files in the current target folder
     */
    private void cleanArtifacts(List<ItemInfo> currentVfsFolderItems, Collection<File> artifacts) {
        for (File artifact : artifacts) {
            if (artifact != null) {
                String ileName = artifact.getName();
                ItemInfo itemInfo = getItemByName(currentVfsFolderItems, ileName);
                if (itemInfo == null) {
                    if (artifact.isDirectory()) {
                        // If a directory does not exist in data store - we need to recursively handle all of his children as well
                        Collection<File> childArtifacts = Sets.newHashSet(
                                artifact.listFiles((FileFilter) new NotFileFilter(new MetadataFileFilter())));
                        cleanArtifacts(Collections.<ItemInfo>emptyList(), childArtifacts);
                    }
                    log.debug("Deleting {} from the incremental backup dir since it was " +
                            "deleted from the repository", artifact.getAbsolutePath());
                    boolean deleted = FileUtils.deleteQuietly(artifact);
                    if (!deleted) {
                        log.warn("Failed to delete {}", artifact.getAbsolutePath());
                    }
                    // now delete the metadata folder of the file/folder is it exists
                    File metadataFolder = getMetadataContainerFolder(artifact);
                    if (metadataFolder.exists()) {
                        deleted = FileUtils.deleteQuietly(metadataFolder);
                        if (!deleted) {
                            log.warn("Failed to delete metadata folder {}", metadataFolder.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    /**
     * Locates metadata that was removed from different artifacts since last backup, but still remain in the backup
     * folder and clean them out.
     *
     * @param currentVfsFolderItems List of vfs items in the current vfs folder
     * @param metadataFiles         List of metadata files in the current target's metadata folder
     */
    private void cleanMetadata(List<ItemInfo> currentVfsFolderItems, Collection<File> metadataFiles) {
        for (File metadataFile : metadataFiles) {
            if ((metadataFile != null) && (metadataFile.isFile())) {
                String metadataFolderPath = metadataFile.getParent();
                //Extract the metadata container name from the parent path
                String metadataContainerName = getMetadataContainerName(metadataFolderPath);
                //Extract the metadata name from the metadata file name
                String metadataName = PathUtils.stripExtension(metadataFile.getName());

                //If metadata and container names returned valid
                if ((metadataName != null) && (metadataContainerName != null)) {
                    ItemInfo itemInfo = getItemByName(currentVfsFolderItems, metadataContainerName);
                    if (itemInfo != null) {
                        //If the metadata container does not contain this metadata anymore
                        boolean hasMetadata = false;
                        try {
                            hasMetadata = hasMetadata(itemInfo.getRepoPath(), metadataName);
                        } catch (RepositoryRuntimeException e) {
                            String message = String.format("Unable to determine whether %s is annotated by metadata " +
                                    "of type %s. Metadata was not cleaned.", itemInfo.getRepoPath(), metadataName);
                            status.error(message, e, log);
                        }
                        if (!hasMetadata) {
                            boolean deleted = FileUtils.deleteQuietly(metadataFile);
                            if (!deleted) {
                                log.warn("Failed to delete {}", metadataFile.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean hasMetadata(RepoPath repoPath, String metadataName) {
        if (metadataName.equals(StatsInfo.ROOT)) {
            return getStatsService().hasStats(repoPath);
        } else if (metadataName.equals(Properties.ROOT)) {
            return getPropertiesService().hasProperties(repoPath);
        } else if (metadataName.equals(WatcherInfo.ROOT)) {
            return getWatchesService().hasWatches(repoPath);
        } else {
            // old generic metadata or unknown -> don't delete from the incremental backup
            return true;
        }
    }

    /**
     * Locates metadata that was removed from the current target since last backup, but still remain in the backup
     * folder and clean them out.
     *
     * @param currentFolderInfo   The folder info the cleanup is working with
     * @param targetMetadataFiles List of metadata files in the current target's metadata folder
     */
    private void cleanTargetMetadata(FolderInfo currentFolderInfo, Collection<File> targetMetadataFiles) {
        for (File metadataFile : targetMetadataFiles) {
            if ((metadataFile != null) && metadataFile.isFile()) {
                //Extract the metadata name from the metadata file name
                String metadataName = PathUtils.stripExtension(metadataFile.getName());
                boolean hasMetadata = false;
                try {
                    hasMetadata = hasMetadata(currentFolderInfo.getRepoPath(), metadataName);
                } catch (RepositoryRuntimeException e) {
                    // File may be deleted in the meantime, so this is just a warning
                    String message = String.format("Unable to determine whether %s is annotated by metadata of type " +
                            "%s. Metadata entry not present!", currentFolderInfo.getRepoPath(), metadataName);
                    status.warn(message, e, log);
                }
                //If the metadata container does not contain this metadata anymore
                if (!hasMetadata) {
                    boolean deleted = FileUtils.deleteQuietly(metadataFile);
                    if (!deleted) {
                        log.warn("Failed to delete {}", metadataFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    private ItemInfo getItemByName(List<ItemInfo> currentVfsFolderItems, String vfsFileName) {
        for (ItemInfo itemInfo : currentVfsFolderItems) {
            if (vfsFileName.equals(itemInfo.getName())) {
                return itemInfo;
            }
        }
        return null;
    }

    /**
     * Extracts the metadata container name from the metadata folder path
     *
     * @param metadataFolderPath Metadata folder path
     * @return Metadata container name extracted from metadata folder path
     */
    private String getMetadataContainerName(String metadataFolderPath) {
        //Get last index of slash
        int indexOfLastSlash = metadataFolderPath.lastIndexOf('/') + 1;
        //Get index of metadata folder suffix
        int indexOfFolderName = metadataFolderPath.indexOf(METADATA_FOLDER);
        if ((indexOfLastSlash == -1) || (indexOfFolderName == -1)) {
            return null;
        }
        return metadataFolderPath.substring(indexOfLastSlash, indexOfFolderName);
    }

    private class MetadataFileFilter extends AbstractFileFilter {
        @Override
        public boolean accept(File file) {
            //Accept only files within the metadata folder which are not part of the file info system
            boolean isArtifactoryFile = file.getName().contains(FileInfo.ROOT);
            boolean isArtifactoryFolder = file.getName().contains(FolderInfo.ROOT);
            return isFileInMetadataFolder(file) && !isArtifactoryFile && !isArtifactoryFolder;
        }

        /**
         * Indicates if the given file is located inside a metadata folder
         *
         * @param file File to query
         * @return True if the file is located in a metadata folder. False if not
         */
        private boolean isFileInMetadataFolder(File file) {
            return file.getAbsolutePath().contains(METADATA_FOLDER);
        }
    }

    private RepositoryService getRepositoryService() {
        return StorageContextHelper.get().getRepositoryService();
    }
}
