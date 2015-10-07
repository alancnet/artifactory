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

package org.artifactory.storage.db.fs.model;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.binstore.BinaryInfo;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.exception.CancelException;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.sapi.fs.MutableVfsFile;
import org.artifactory.storage.BinaryInsertRetryException;
import org.artifactory.storage.binstore.service.InternalBinaryStore;
import org.artifactory.storage.fs.VfsException;
import org.artifactory.storage.fs.repo.StoringRepo;
import org.artifactory.storage.fs.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

/**
 * A mutable DB file.
 *
 * @author Yossi Shaul
 */
public class DbMutableFile extends DbMutableItem<MutableFileInfo> implements MutableVfsFile {
    private static final Logger log = LoggerFactory.getLogger(DbMutableFile.class);

    private StatsInfo stats;
    private boolean placedBinaryDeleteProtectionLock;

    public DbMutableFile(StoringRepo storingRepo, long id, FileInfo info) {
        super(storingRepo, id, InfoFactoryHolder.get().copyFileInfo(info));
    }

    @Override
    public void setClientChecksum(@Nonnull ChecksumType type, @Nullable String checksum) {
        if (type == ChecksumType.sha1) {
            setClientSha1(checksum);
        } else {
            setClientMd5(checksum);
        }
    }

    @Override
    public void setClientSha1(@Nullable String sha1) {
        mutableInfo.addChecksumInfo(new ChecksumInfo(ChecksumType.sha1, sha1, mutableInfo.getSha1()));
    }

    @Override
    public void setClientMd5(String md5) {
        mutableInfo.addChecksumInfo(new ChecksumInfo(ChecksumType.md5, md5, mutableInfo.getMd5()));
    }

    @Override
    public void fillInfo(FileInfo source) {
        super.fillInfo(source);
        ChecksumsInfo resourceChecksums = source.getChecksumsInfo();
        ChecksumInfo sha1Info = resourceChecksums.getChecksumInfo(ChecksumType.sha1);
        String clientSha1 = sha1Info == null ? null : sha1Info.getOriginalOrNoOrig();
        setClientSha1(clientSha1);
        ChecksumInfo md5Info = resourceChecksums.getChecksumInfo(ChecksumType.md5);
        String clientMd5 = md5Info == null ? null : md5Info.getOriginalOrNoOrig();
        setClientMd5(clientMd5);
    }

    @Override
    public boolean tryUsingExistingBinary(String sha1, String md5, long length) throws BinaryInsertRetryException {
        BinaryInfo binary = getBinaryStore().addBinaryRecord(sha1, md5, length);
        if (binary != null) {
            setFileBinaryInfo(binary);
            return true;
        }
        return false;
    }

    @Override
    public void fillBinaryData(InputStream in) {
        //Check if needs to create checksum and not checksum file
        log.debug("Calculating checksums of '{}'.", getRepoPath());
        BinaryInfo binaryInfo;
        try {
            // call the binary service to add the current binary
            binaryInfo = getBinaryStore().addBinary(in);
        } catch (IOException e) {
            throw new VfsException("Failed to add input stream for '" + getRepoPath() + "'", e);
        }

        setFileBinaryInfo(binaryInfo);
    }

    @Override
    public void setStats(StatsInfo statsInfo) {
        this.stats = statsInfo;
    }

    private void setFileBinaryInfo(@Nonnull BinaryInfo binary) {
        getBinaryStore().incrementNoDeleteLock(binary.getSha1());
        placedBinaryDeleteProtectionLock = true;
        // The size needs to be always consistent so no rely on MD
        mutableInfo.setSize(binary.getLength());
        ChecksumInfo sha1Orig = mutableInfo.getChecksumsInfo().getChecksumInfo(ChecksumType.sha1);
        String clientSha1 = sha1Orig == null ? null : sha1Orig.getOriginalOrNoOrig();
        mutableInfo.addChecksumInfo(new ChecksumInfo(ChecksumType.sha1, clientSha1, binary.getSha1()));
        ChecksumInfo md5Orig = mutableInfo.getChecksumsInfo().getChecksumInfo(ChecksumType.md5);
        String clientMd5 = md5Orig == null ? null : md5Orig.getOriginalOrNoOrig();
        mutableInfo.addChecksumInfo(new ChecksumInfo(ChecksumType.md5, clientMd5, binary.getMd5()));
    }

    @Override
    public boolean delete() {
        if (isNew()) {
            log.error("Deleting a newly created item: {}", getRepoPath());
            throw new IllegalStateException("Cannot delete a newly created item: " + getRepoPath());
        }

        if (markForDeletion) {
            return true;
        }

        try {
            fireBeforeDeleteEvent();
        } catch (CancelException e) {
            log.info("Deletion of {} was canceled by plugin", getRepoPath());
            throw e;
        }

        markForDeletion = true;

        fireAfterDeleteEvent();
        return true;
    }

    @Override
    public void releaseResources() {
        super.releaseResources();
        releaseInsertedStreamLock();
    }

    @Override
    public boolean hasPendingChanges() {
        if (inError) {
            return false;
        }
        return stats != null || super.hasPendingChanges();
    }

    @Override
    protected boolean readyForPersistence() {
        return !StringUtils.isBlank(getSha1());
    }

    @Override
    protected void resetAfterSave() {
        super.resetAfterSave();
        stats = null;
    }

    @Override
    protected void doDeleteInternal() {
        // delete file specific data
        getStatsService().deleteStats(id);
    }

    @Override
    protected long doCreateNode() {
        long nodeId = getFileService().createFile(mutableInfo);
        if (stats != null) {
            getStatsService().setStats(nodeId, stats);
        }
        return nodeId;
    }

    private void releaseInsertedStreamLock() {
        if (placedBinaryDeleteProtectionLock) {
            getBinaryStore().decrementNoDeleteLock(getSha1());
        }
    }

    @Override
    protected void doUpdateNode() {
        getFileService().updateFile(id, mutableInfo);
        // stats are not updatable by mutable file
    }

    protected InternalBinaryStore getBinaryStore() {
        return ContextHelper.get().beanForType(InternalBinaryStore.class);
    }

    @Override
    public String getSha1() {
        return mutableInfo.getSha1();
    }

    @Override
    public String getMd5() {
        return mutableInfo.getMd5();
    }

    @Override
    public InputStream getStream() {
        return getBinariesService().getBinary(getSha1());
    }

    @Override
    public long length() {
        return mutableInfo.getSize();
    }

    protected StatsService getStatsService() {
        return ContextHelper.get().beanForType("statsServiceImpl", StatsService.class);
    }

}
