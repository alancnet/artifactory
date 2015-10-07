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

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.backup.FileExportInfoImpl;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.common.Info;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.fs.WatcherInfo;
import org.artifactory.fs.WatchersInfo;
import org.artifactory.md.MetadataDefinition;
import org.artifactory.md.Properties;
import org.artifactory.md.PropertiesInfo;
import org.artifactory.md.XmlMetadataProvider;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.FileExportEvent;
import org.artifactory.sapi.common.FileExportInfo;
import org.artifactory.storage.fs.VfsItemNotFoundException;
import org.artifactory.storage.fs.service.ItemMetaInfo;
import org.artifactory.storage.fs.service.NodeMetaInfoService;
import org.artifactory.storage.fs.service.PropertiesService;
import org.artifactory.storage.fs.service.StatsService;
import org.artifactory.storage.service.StatsServiceImpl;
import org.artifactory.storage.spring.StorageContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * Base class for exporting repository content.
 *
 * @author Yossi Shaul
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class DbExportBase extends DbRepoImportExportBase {
    private static final Logger log = LoggerFactory.getLogger(DbExportBase.class);

    protected ExportSettings settings;
    protected MutableStatusHolder status;
    protected final ImportExportAccumulator accumulator;

    protected DbExportBase(ImportExportAccumulator accumulator) {
        this.accumulator = accumulator;
    }


    protected void setExportSettings(ExportSettings settings) {
        this.settings = settings;
        this.status = settings.getStatusHolder();
    }

    protected void exportFile(FileInfo sourceFile) {
        status.debug("Exporting file '" + sourceFile.getRepoPath() + "'...", log);

        File targetBase;
        if (settings.isCreateArchive()) {
            targetBase = settings.getBaseDir();
        } else {
            targetBase = settings.getBaseDir();
        }
        File targetFile = new File(targetBase, sourceFile.getRelPath());
        try {
            // Insure that the source file still exists.
            boolean sourceFileExists = getFileService().exists(sourceFile.getRepoPath());
            if (!sourceFileExists) {
                log.info("Skipping file export : '{}', the source file doesn't exists.", sourceFile.getRepoPath());
                return;
            }
            //Invoke the callback if exists
            settings.executeCallbacks(
                    new FileExportInfoImpl(sourceFile, targetFile, FileExportInfo.FileExportStatus.PENDING),
                    FileExportEvent.BEFORE_FILE_EXPORT);

            File parentFile = targetFile.getParentFile();
            if (!parentFile.exists()) {
                FileUtils.forceMkdir(parentFile);
            }

            boolean fileContentExported = false;
            // Export file only if not "incremental export" and the file to export is newer than the target file.
            boolean skipFileContentExport = isSkipFileContentExport(sourceFile, targetFile);
            if (!skipFileContentExport) {
                fileContentExported = exportFileContent(sourceFile, targetFile);
            }

            settings.executeCallbacks(new FileExportInfoImpl(sourceFile, targetFile,
                    fileContentExported ? FileExportInfo.FileExportStatus.ADDED :
                            FileExportInfo.FileExportStatus.SKIPPED),
                    FileExportEvent.AFTER_FILE_EXPORT);

            if (settings.isIncludeMetadata()) {
                exportMetadata(targetFile, sourceFile);
            }
            if (settings.isM2Compatible()) {
                writeChecksums(targetFile, sourceFile);
            }
            accumulator.accumulateSuccessfulFile();
            //If a file export fails, we collect the error but not fail the whole export
        } catch (FileNotFoundException e) {
            status.error("Failed to export '" + targetFile.getAbsolutePath() + "' since it is non-accessible.",
                    e, log);
            accumulator.accumulateSkippedFile();
        } catch (Exception e) {
            status.error("Failed to export '" + targetFile.getAbsolutePath() + "' due to:" + e.getMessage(),
                    e, log);
            accumulator.accumulateSkippedFile();
        }
    }

    private boolean isSkipFileContentExport(FileInfo sourceFile, File targetFile) {
        if (settings.isExcludeContent()) {
            return true;
        }
        if (settings.isIncremental() && targetFile.exists()) {
            // incremental export - only export the file if it is newer
            log.trace("Source file last modified {} vs target file last modified {}", sourceFile.getLastModified(),
                    targetFile.lastModified());
            if (sourceFile.getLastModified() - targetFile.lastModified() < TimeUnit.MILLISECONDS.toMicros(1)) {
                log.debug("Skipping not modified file {}", sourceFile.getRepoPath());
                return true;
            }
        }
        return false;
    }

    private boolean exportFileContent(FileInfo sourceFile, File targetFile) throws IOException {

        log.debug("Exporting file content to {}", targetFile.getAbsolutePath());
        OutputStream os = null;
        InputStream is = null;
        try {
            // get the stream directly from the datastore (no fs item locks)
            is = getBinaryStore().getBinary(sourceFile.getSha1());
            os = new BufferedOutputStream(new FileOutputStream(targetFile));
            IOUtils.copy(is, os);
        } catch (VfsItemNotFoundException e) {
            // since we work with an unlocked items there's a small chance the binary doesn't exist anymore
            status.warn("Binary not found for item '" + sourceFile.getRepoPath() + "'"
                    + " with sha1 '" + sourceFile.getSha1() + "'", log);
            return false;
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }

        targetFile.setLastModified(sourceFile.getLastModified());
        return true;
    }

    private void writeChecksums(File targetFile, FileInfo sourceFile) throws IOException {
        // Write the checksum files next to the file which they belong to.
        for (ChecksumInfo checksumInfo : sourceFile.getChecksumsInfo().getChecksums()) {
            File checksumFile = new File(targetFile + checksumInfo.getType().ext());
            FileUtils.writeStringToFile(checksumFile, checksumInfo.getActual(), "utf-8");
            checksumFile.setLastModified(sourceFile.getLastModified());
        }
    }

    protected void exportMetadata(File targetFile, ItemInfo sourceItem) {
        try {
            File metadataFolder = getMetadataContainerFolder(targetFile);
            FileUtils.forceMkdir(metadataFolder);

            exportItemInfo(sourceItem, metadataFolder);

            exportProperties(sourceItem, metadataFolder);

            exportWatches(sourceItem, metadataFolder);

            if (!sourceItem.isFolder()) {
                exportStats(((FileInfo) sourceItem), metadataFolder);
            }

        } catch (Exception e) {
            status.error("Failed to export metadata for '" + sourceItem.getRepoPath() + "'.", e, log);
        }
    }


    private void exportItemInfo(ItemInfo itemInfo, File metadataFolder) {
        String metadataName = itemInfo.isFolder() ? FolderInfo.ROOT : FileInfo.ROOT;
        MetadataDefinition definition = getMetadataDefinitionService()
                .getMetadataDefinition(metadataName, false);
        File targetMetadataFile = new File(metadataFolder, definition.getMetadataName() + ".xml");
        long lastModified = itemInfo.getLastModified();
        writeMetadataToFile(definition, targetMetadataFile, itemInfo, lastModified);
    }

    private void exportProperties(ItemInfo itemInfo, File metadataFolder) {
        MetadataDefinition definition = getMetadataDefinitionService()
                .getMetadataDefinition(PropertiesInfo.ROOT, false);
        File targetMetadataFile = new File(metadataFolder, definition.getMetadataName() + ".xml");
        Properties properties = getPropertiesService().getProperties(itemInfo.getRepoPath());
        if (properties.isEmpty()) {
            // remove existing properties if incremental
            if (settings.isIncremental()) {
                FileUtils.deleteQuietly(targetMetadataFile);
            }
        } else {
            ItemMetaInfo nodeMetaInfo = ContextHelper.get().beanForType(NodeMetaInfoService.class).getNodeMetaInfo(
                    itemInfo.getRepoPath());
            long lastModified = nodeMetaInfo != null ? nodeMetaInfo.getPropsModified() : itemInfo.getLastModified();
            writeMetadataToFile(definition, targetMetadataFile, properties, lastModified);
        }
    }

    private void exportWatches(ItemInfo itemInfo, File metadataFolder) {
        MetadataDefinition definition = getMetadataDefinitionService()
                .getMetadataDefinition(WatchersInfo.ROOT, false);
        File targetMetadataFile = new File(metadataFolder, definition.getMetadataName() + ".xml");
        WatchersInfo watches = getWatchesService().getWatches(itemInfo.getRepoPath());
        if (watches.isEmpty()) {
            // remove existing watches if incremental
            if (settings.isIncremental()) {
                FileUtils.deleteQuietly(targetMetadataFile);
            }
        } else {
            long lastModified = itemInfo.getLastModified();
            for (WatcherInfo watcherInfo : watches.getWatchers()) {
                lastModified = Math.max(lastModified, watcherInfo.getWatchingSinceTime());
            }
            writeMetadataToFile(definition, targetMetadataFile, watches, lastModified);
        }
    }

    private void exportStats(FileInfo fileInfo, File metadataFolder) {
        MetadataDefinition definition = getMetadataDefinitionService()
                .getMetadataDefinition(StatsInfo.ROOT, false);
        File targetMetadataFile = new File(metadataFolder, definition.getMetadataName() + ".xml");
        StatsInfo stats = getStatsService().getStats(fileInfo.getRepoPath());
        if (stats == null) {
            // remove existing stats if incremental
            if (settings.isIncremental()) {
                FileUtils.deleteQuietly(targetMetadataFile);
            }
        } else {
            long lastModified = stats.getLastDownloaded();
            writeMetadataToFile(definition, targetMetadataFile, stats, lastModified);
        }
    }

    private void writeMetadataToFile(MetadataDefinition definition, File targetMetadataFile, Info metadata,
            long lastModified) {
        if (shouldExportMetadata(targetMetadataFile, lastModified)) {
            XmlMetadataProvider xmlProvider = definition.getXmlProvider();
            String xmlData = xmlProvider.toXml(metadata);
            writeFile(targetMetadataFile, xmlData, lastModified);
        }
    }

    private boolean shouldExportMetadata(File targetMetadataFile, long lastModified) {
        if (!settings.isIncremental()) {
            return true;
        } else {
            return !targetMetadataFile.exists() || lastModified > targetMetadataFile.lastModified();
        }
    }

    protected void writeFile(File metadataFile, String xmlData, long lastModified) {
        if (StringUtils.isBlank(xmlData)) {
            return;
        }
        try {
            FileUtils.writeStringToFile(metadataFile, xmlData, Charsets.UTF_8.name());
            metadataFile.setLastModified(lastModified);
        } catch (Exception e) {
            status.error("Failed to export metadata of '" +
                    metadataFile.getPath() + "' + to '" + metadataFile.getAbsolutePath(), e, log);
            FileUtils.deleteQuietly(metadataFile);
        }
    }

    protected PropertiesService getPropertiesService() {
        return ContextHelper.get().beanForType(PropertiesService.class);
    }

    protected StatsService getStatsService() {
        return StorageContextHelper.get().beanForType(StatsServiceImpl.class);
    }
}
