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

package org.artifactory.model.xstream.fs;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.fs.MutableItemInfo;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.PathUtils;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Basic information about the file. Internally not stored as XML but as node properties
 *
 * @author yoavl
 */
@XStreamAlias(org.artifactory.fs.FileInfo.ROOT)
public class FileInfoImpl extends ItemInfoImpl implements InternalFileInfo {

    private long size;
    private String mimeType;
    private FileAdditionalInfo additionalInfo;

    public FileInfoImpl(RepoPath repoPath) {
        super(repoPath);
        this.size = 0;
        //Force a mime type
        setMimeType(null);
        this.additionalInfo = new FileAdditionalInfo();
    }

    public FileInfoImpl(InternalFileInfo info) {
        super(info);
        this.size = info.getSize();
        setMimeType(info.getMimeType());
        this.additionalInfo = new FileAdditionalInfo(info.getAdditionalInfo());
    }

    /**
     * Required by xstream
     *
     * @param info
     */
    protected FileInfoImpl(FileInfoImpl info) {
        this((InternalFileInfo) info);
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public long getAge() {
        return getLastModified() != 0 ? System.currentTimeMillis() - getLastModified() : -1;
    }

    @Override
    public String getMimeType() {
        if (this.mimeType == null) {
            this.mimeType = NamingUtils.getMimeTypeByPathAsString(getRelPath());
        }
        return mimeType;
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getSha1() {
        return additionalInfo.getSha1();
    }

    @Override
    public String getMd5() {
        return additionalInfo.getMd5();
    }

    @Override
    @Nonnull
    public ChecksumsInfo getChecksumsInfo() {
        return additionalInfo.getChecksumsInfo();
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                super.toString() +
                ", size=" + size +
                ", mimeType='" + mimeType + '\'' +
                ", extension=" + additionalInfo +
                '}';
    }

    @Override
    public boolean isIdentical(ItemInfo info) {
        if (!super.isIdentical(info)) {
            return false;
        }

        FileInfoImpl fileInfo = (FileInfoImpl) info;

        if (this.size != fileInfo.size || !PathUtils.safeStringEquals(this.mimeType, fileInfo.mimeType)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public FileAdditionalInfo getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Should not be called by clients - for internal use
     *
     * @return
     */
    @Override
    public void setAdditionalInfo(FileAdditionalInfo additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public Set<ChecksumInfo> getChecksums() {
        return additionalInfo.getChecksums();
    }

    @Override
    public void setChecksums(Set<ChecksumInfo> checksums) {
        additionalInfo.setChecksums(checksums);
    }

    @Override
    public void createTrustedChecksums() {
        this.additionalInfo.createTrustedChecksums();
    }

    @Override
    public void addChecksumInfo(org.artifactory.checksum.ChecksumInfo info) {
        additionalInfo.addChecksumInfo(info);
    }

    @Override
    public boolean merge(MutableItemInfo itemInfo) {
        boolean modified = super.merge(itemInfo);
        if (itemInfo instanceof MutableFileInfo) {
            FileInfo fileInfo = (FileInfo) itemInfo;
            if (fileInfo.getSize() > 0 && this.size != fileInfo.getSize()) {
                this.size = fileInfo.getSize();
                modified = true;
            }
            String mt = fileInfo.getMimeType();
            if (PathUtils.hasText(mt) && !PathUtils.safeStringEquals(mt, mimeType)) {
                this.mimeType = mt;
                modified = true;
            }
        }
        return modified;
    }
}
