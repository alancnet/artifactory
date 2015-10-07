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
import org.artifactory.fs.FolderInfo;
import org.artifactory.repo.RepoPath;

/**
 * @author yoavl
 */
@XStreamAlias(FolderInfo.ROOT)
public class FolderInfoImpl extends ItemInfoImpl implements InternalFolderInfo {

    private FolderAdditionalInfo additionalInfo;

    public FolderInfoImpl(RepoPath repoPath) {
        super(repoPath);
        additionalInfo = new FolderAdditionalInfo();
    }

    public FolderInfoImpl(InternalFolderInfo info) {
        super(info);
        additionalInfo = new FolderAdditionalInfo(info.getAdditionalInfo());
    }

    /**
     * Required by xstream
     *
     * @param info
     */
    protected FolderInfoImpl(FolderInfoImpl info) {
        this(((InternalFolderInfo) info));
    }

    /**
     * Should not be called by clients - for internal use
     *
     * @return
     */
    @Override
    public void setAdditionalInfo(FolderAdditionalInfo additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public String toString() {
        return "FolderInfo{" + super.toString() + ", extension=" + additionalInfo + '}';
    }

    @Override
    public boolean isIdentical(org.artifactory.fs.ItemInfo info) {
        return super.isIdentical(info);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    @Deprecated
    public FolderAdditionalInfo getAdditionalInfo() {
        return additionalInfo;
    }
}