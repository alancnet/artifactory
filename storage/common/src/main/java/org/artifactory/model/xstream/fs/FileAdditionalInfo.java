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
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author freds
 * @date Oct 12, 2008
 */
@XStreamAlias(FileAdditionalInfo.ROOT)
public class FileAdditionalInfo extends ItemAdditionalInfo {
    public static final String ROOT = "artifactory-file-ext";

    private ChecksumsInfo checksumsInfo;

    public FileAdditionalInfo() {
        super();
        this.checksumsInfo = new ChecksumsInfo();
    }

    public FileAdditionalInfo(ChecksumsInfo checksumsInfo) {
        super();
        this.checksumsInfo = new ChecksumsInfo(checksumsInfo);
    }

    public FileAdditionalInfo(FileAdditionalInfo additionalInfo) {
        super(additionalInfo);
        this.checksumsInfo = new ChecksumsInfo(additionalInfo.getChecksumsInfo());
    }

    @Nonnull
    public ChecksumsInfo getChecksumsInfo() {
        if (checksumsInfo == null) {
            this.checksumsInfo = new ChecksumsInfo();
        }
        return checksumsInfo;
    }

    @Override
    public boolean isIdentical(ItemAdditionalInfo other) {
        if (!(other instanceof FileAdditionalInfo)) {
            return false;
        }
        if (!super.isIdentical(other)) {
            return false;
        }

        FileAdditionalInfo fileExtraInfo = (FileAdditionalInfo) other;
        if (this.checksumsInfo != null ? !this.checksumsInfo.isIdentical(fileExtraInfo.checksumsInfo)
                : fileExtraInfo.checksumsInfo != null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean merge(ItemAdditionalInfo additionalInfo) {
        boolean modified = super.merge(additionalInfo);
        if (additionalInfo instanceof FileAdditionalInfo) {
            FileAdditionalInfo fileAdditionalInfo = (FileAdditionalInfo) additionalInfo;
            if (!fileAdditionalInfo.isIdentical(this)) {
                ChecksumsInfo otherChecksumInfo = fileAdditionalInfo.getChecksumsInfo();
                if (!otherChecksumInfo.isEmpty()) {
                    if (getChecksumsInfo().isEmpty()) {
                        checksumsInfo = new ChecksumsInfo();
                        checksumsInfo.setChecksums(otherChecksumInfo.getChecksums());
                        modified = true;
                    } else {
                        for (ChecksumType type : ChecksumType.BASE_CHECKSUM_TYPES) {
                            ChecksumInfo other = otherChecksumInfo.getChecksumInfo(type);
                            ChecksumInfo mine = this.checksumsInfo.getChecksumInfo(type);
                            if (mine == null) {
                                addChecksumInfo(other);
                                modified = true;
                            } else if (!other.isIdentical(mine)) {
                                modified = true;
                                this.checksumsInfo.addChecksumInfo(other);
                            }
                        }
                    }
                }
            }
        }
        return modified;
    }

    public String getSha1() {
        return checksumsInfo.getSha1();
    }

    public String getMd5() {
        return checksumsInfo.getMd5();
    }

    public Set<ChecksumInfo> getChecksums() {
        return checksumsInfo.getChecksums();
    }

    public void createTrustedChecksums() {
        checksumsInfo.createTrustedChecksums();
    }

    public void setChecksums(Set<ChecksumInfo> checksums) {
        checksumsInfo.setChecksums(checksums);
    }

    public void addChecksumInfo(ChecksumInfo info) {
        checksumsInfo.addChecksumInfo(info);
    }

    @Override
    public String toString() {
        return "FileAdditionalInfo{" + super.toString() + "}";
    }
}