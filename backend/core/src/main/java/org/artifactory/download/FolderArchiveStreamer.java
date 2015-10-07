package org.artifactory.download;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.artifactory.api.archive.ArchiveType;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.download.FolderDownloadService;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.common.ConstantValues;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.schedule.CachedThreadPoolTaskExecutor;
import org.artifactory.schedule.DummyExecutorService;
import org.artifactory.security.AccessLogger;
import org.artifactory.storage.fs.service.StatsService;
import org.artifactory.storage.fs.tree.ItemNode;
import org.artifactory.storage.fs.tree.ItemTree;
import org.artifactory.storage.fs.tree.TreeBrowsingCriteriaBuilder;
import org.artifactory.traffic.TrafficService;
import org.artifactory.traffic.entry.DownloadEntry;
import org.artifactory.util.ArchiveUtils;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.PathUtils;
import org.iostreams.streams.in.OutputToInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Traverses the tree under the requested path recursively and writes each file into the stream serially.
 * The stream itself is an {@link ArchiveOutputStream} based on the selected {@link ArchiveType}
 *
 * @author Dan Feldman
 * @author Yossi Shaul
 */
public class FolderArchiveStreamer {
    private static final Logger log = LoggerFactory.getLogger(FolderArchiveStreamer.class);

    private final ArchiveType archiveType;
    private final BasicStatusHolder status;
    private long maxDownloadSizeInMB;
    private long maxDownloadSizeInBytes;
    private long maxFiles;
    private RepoPath rootFolder;
    private ArchiveOutputStream archiveOutputStream = null;
    private long filesCount;
    private long totalSizeInBytes;

    public FolderArchiveStreamer(RepoPath pathToDownload, ArchiveType archiveType, int maxDownloadSizeMb,
            long maxFiles, BasicStatusHolder status) {
        this.rootFolder = pathToDownload;
        this.archiveType = archiveType;
        this.status = status;
        //rounds towards zero but the overflow is negligible
        this.maxDownloadSizeInBytes = (long) StorageUnit.MB.toBytes(maxDownloadSizeMb);
        this.maxFiles = maxFiles;
        this.maxDownloadSizeInMB = maxDownloadSizeMb;
    }

    public InputStream go() {
        CachedThreadPoolTaskExecutor executor = ContextHelper.get().beanForType(CachedThreadPoolTaskExecutor.class);
        return new OutputToInputStream(new DummyExecutorService(executor)) {
            @Override
            protected void write(OutputStream sink) throws IOException {
                try {
                    long start = System.currentTimeMillis();
                    archiveOutputStream = ArchiveUtils.createArchiveOutputStream(sink, archiveType);
                    ItemTree tree = new ItemTree(rootFolder, new TreeBrowsingCriteriaBuilder()
                            .applyRepoIncludeExclude().applySecurity().cacheChildren(false).build());
                    ItemNode rootNode = tree.getRootNode();
                    writeRecursive(rootNode);
                    archiveOutputStream.finish();
                    log.trace("folder download finished successfully, took {} ms", System.currentTimeMillis() - start);
                } catch (Exception e) {
                    status.error("Error executing folder download: " + e.getMessage(), log);
                    log.debug("Caught exception while executing folder download: ", e);
                } finally {
                    archiveOutputStream.flush();
                    IOUtils.closeQuietly(archiveOutputStream);
                    IOUtils.closeQuietly(sink);
                    ContextHelper.get().beanForType(FolderDownloadService.class).releaseDownloadSlot();
                }
            }
        };
    }

    private void writeRecursive(ItemNode currentNode) throws IOException {
        if (limitsReached()) {
            return;
        }
        if (currentNode.isFolder()) {
            for (ItemNode child : currentNode.getChildren()) {
                if (!limitsReached()) {
                    writeRecursive(child);
                }
            }
        } else {
            FileInfo fileInfo = (FileInfo) currentNode.getItemInfo();
            totalSizeInBytes += fileInfo.getSize();
            filesCount++;
            if (!limitsReached()) {
                writeArtifactToStream(fileInfo.getRepoPath(), fileInfo.getSize());
            }
        }
    }

    private boolean limitsReached() {
        if(filesCount > maxFiles) {
            status.debug("Stopping folder download tree traversal, max file limit reached. current file count: " +
                    filesCount + " limit is: " + maxFiles, log);
            return true;
        } else if(totalSizeInBytes > maxDownloadSizeInBytes) {
            status.debug("Stopping folder download tree traversal, max size limit reached. current size count: " +
                    StorageUnit.toReadableString(totalSizeInBytes) + " limit is: " + maxDownloadSizeInMB, log);
            return true;
        }
        return false;
    }

    /**
     * Returns the size of downloaded file for the loop stop calculation
     */
    private void writeArtifactToStream(RepoPath fileRepoPath, long size) throws IOException {
        long start = System.currentTimeMillis();
        String relativePath = PathUtils.getRelativePath(rootFolder.getPath(), fileRepoPath.getPath());
        ArchiveEntry archiveEntry = ArchiveUtils.createArchiveEntry(relativePath, archiveType, size);
        ResourceStreamHandle handle = ContextHelper.get().getRepositoryService().getResourceStreamHandle(fileRepoPath);
        try {
            InputStream artifactStream = handle.getInputStream();
            archiveOutputStream.putArchiveEntry(archiveEntry);
            log.debug("Writing path {} to output stream", fileRepoPath.toPath());
            IOUtils.copy(artifactStream, archiveOutputStream);
        } finally {
            IOUtils.closeQuietly(handle);
            archiveOutputStream.closeArchiveEntry();
            archiveOutputStream.flush();
        }
        logAccessTrafficAndStatsForSinglePath(fileRepoPath, size, start);
    }

    private void logAccessTrafficAndStatsForSinglePath(RepoPath path, long size, long start) {
        AccessLogger.downloaded(path);
        DownloadEntry downloadEntry = new DownloadEntry(path.getId(), size, System.currentTimeMillis() - start,
                HttpUtils.getRemoteClientAddress());
        ContextHelper.get().beanForType(TrafficService.class).handleTrafficEntry(downloadEntry);
        if (ConstantValues.downloadStatsEnabled.getBoolean()) {
            ContextHelper.get().beanForType(StatsService.class).fileDownloaded(path,
                    SecurityContextHolder.getContext().getAuthentication().getName(), System.currentTimeMillis());
        }
    }
}