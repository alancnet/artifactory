package org.artifactory.download;

import org.apache.http.HttpStatus;
import org.artifactory.api.archive.ArchiveType;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.download.FolderDownloadInfo;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.aql.AqlService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.traffic.TrafficService;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.Semaphore;

import static org.apache.http.HttpStatus.*;

/**
 * Serves requests for download of folders as archive
 *
 * @author Dan Feldman
 */
@Service
@Reloadable(beanClass = InternalFolderDownloadService.class, initAfter = {InternalRepositoryService.class})
public class FolderDownloadServiceImpl implements InternalFolderDownloadService {
    private static final Logger log = LoggerFactory.getLogger(FolderDownloadServiceImpl.class);

    @Autowired
    AqlService aqlService;

    @Autowired
    InternalRepositoryService repoService;

    @Autowired
    FileService fileService;

    @Autowired
    AuthorizationService authService;

    @Autowired
    TrafficService trafficService;

    private int maxDownloadSizeMb;
    private long maxFiles;
    private boolean serviceEnabled;
    private ConcurrentDownloadCounter concurrentDownloadCounter;

    @Override
    public void init() {
        FolderDownloadConfigDescriptor config = getFolderDownloadConfig();
        this.maxDownloadSizeMb = config.getMaxDownloadSizeMb();
        this.maxFiles = config.getMaxFiles();
        this.serviceEnabled = config.isEnabled();
        this.concurrentDownloadCounter = new ConcurrentDownloadCounter(config.getMaxConcurrentRequests(), true);
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
        FolderDownloadConfigDescriptor newConfig = getFolderDownloadConfig();
        FolderDownloadConfigDescriptor oldConfig = oldDescriptor.getFolderDownloadConfig();
        if (!newConfig.equals(oldConfig)) {
            this.maxDownloadSizeMb = newConfig.getMaxDownloadSizeMb();
            this.maxFiles = newConfig.getMaxFiles();
            this.serviceEnabled = newConfig.isEnabled();
            //Take the extra effort to compare old and new because resize is a synchronized method
            if (newConfig.getMaxConcurrentRequests() != oldConfig.getMaxConcurrentRequests()) {
                concurrentDownloadCounter.resize(newConfig.getMaxConcurrentRequests());
            }
        }
    }

    @Override
    public boolean getAvailableDownloadSlot() {
        if (!concurrentDownloadCounter.tryAcquire()) {
            log.debug("No available download slots, current available count in semaphore: {}",
                    concurrentDownloadCounter.availablePermits());
            return false;
        }
        return true;
    }

    @Override
    public void releaseDownloadSlot() {
        concurrentDownloadCounter.release();
    }

    @Override
    public InputStream process(RepoPath pathToDownload, ArchiveType archiveType, BasicStatusHolder status) {
        if (!assertPath(pathToDownload, status)) {
            return null;
        }
        FolderDownloadInfo info = collectFolderInfo(pathToDownload);
        if (pathExceedsLimits(info, status, pathToDownload.toPath())) {
            return null;
        }
        try {
            if (!getAvailableDownloadSlot()) {
                status.error("There are too many folder download requests currently running, try again later.",
                        HttpStatus.SC_BAD_REQUEST, log);
                return null;
            }
            FolderArchiveStreamer streamer = new FolderArchiveStreamer(pathToDownload, archiveType, maxDownloadSizeMb,
                    maxFiles, status);
            return streamer.go();
        } catch (Exception e) {
            status.error("Error executing folder download: " + e.getMessage(), log);
            log.debug("Caught exception while creating output stream for folder download: ", e);
        }
        return null;
    }

    @Override
    public FolderDownloadInfo collectFolderInfo(RepoPath folder) {
        return new FolderDownloadInfo(StorageUnit.MB.fromBytes(fileService.getFilesTotalSize(folder)),
                repoService.getArtifactCount(folder));
    }

    @Override
    public FolderDownloadConfigDescriptor getFolderDownloadConfig() {
        return ContextHelper.get().beanForType(CentralConfigService.class).getDescriptor().getFolderDownloadConfig();
    }

    private boolean assertPath(RepoPath pathToDownload, BasicStatusHolder status) {
        if(authService.isAnonymous()) {
            status.error("You must be logged in to download a folder or repository.", SC_FORBIDDEN, log);
            return false;
        } else if (!serviceEnabled) {
            status.error("Downloading folders as archive was disabled by your system admin.", SC_FORBIDDEN, log);
            return false;
        }
        String folderPath = pathToDownload.toPath();
        ItemInfo itemInfo;
        Repo repo = repoService.repositoryByKey(pathToDownload.getRepoKey());
        if (repo == null) {
            status.error(pathToDownload.getRepoKey() + " is not a repository.", SC_NOT_FOUND, log);
            return false;
        } else if (!repo.isLocal() && !repo.isCache()) {
            status.error("Downloading a folder or a repository's root is only available for local (or cache) " +
                    "repositories", SC_NOT_FOUND, log);
            return false;
        }
        if (!pathToDownload.isRoot()) {
            //Not root, check folder status
            try {
                itemInfo = repoService.getItemInfo(pathToDownload);
            } catch (ItemNotFoundRuntimeException inf) {
                status.error("Path '" + folderPath + "' does not exist, aborting folder download", SC_NOT_FOUND, log);
                return false;
            }
            if (!itemInfo.isFolder()) {
                status.error("Path '" + folderPath + "' is not a folder, aborting folder download", SC_BAD_REQUEST,
                        log);
                return false;
            }
        }
        if (!authService.canRead(pathToDownload)) {
            status.error("You don't have the required permissions to download " + folderPath + ".", SC_FORBIDDEN, log);
            return false;
        }
        return true;
    }

    private boolean pathExceedsLimits(FolderDownloadInfo info, BasicStatusHolder status, String folderPath) {
        if (info.getSizeMb() > maxDownloadSizeMb) {
            status.error("Size of path '" + folderPath + "' (" + String.format("%.2f", info.getSizeMb()) + "MB) exceeds"
                    + " the max allowed " + "folder download size (" + maxDownloadSizeMb + "MB).", SC_BAD_REQUEST, log);
            return true;
        } else if (info.getTotalFiles() > maxFiles) {
            status.error("Number of files under the path '" + folderPath + "' (" + info.getTotalFiles() + ") exceeds " +
                    "the max allowed file count for folder download (" + maxFiles + ").", SC_BAD_REQUEST, log);
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    /**
     * A resizable semaphore to act as the concurrent downloads counter for the service.
     */
    private static class ConcurrentDownloadCounter extends Semaphore {

        private int permits;

        public ConcurrentDownloadCounter(int permits, boolean fair) {
            super(permits, fair);
            this.permits = permits;
        }

        public synchronized void resize(int newSize) {
            log.debug("Resizing download counter, old size: {}, new size: {}", permits, newSize);
            int delta = newSize - permits;
            if (delta == 0) {
                log.trace("Same size chosen - no need to resize");
                return;
            } else if (delta > 0) {
                log.trace("Adding {} permits", delta);
                this.release(delta);
            } else if (delta < 0) {
                log.trace("Reducing {} permits", Math.abs(delta));
                this.reducePermits(Math.abs(delta));
            }
            this.permits = newSize;
            log.debug("Current available permits in counter: {}", this.availablePermits());
        }
    }
}
