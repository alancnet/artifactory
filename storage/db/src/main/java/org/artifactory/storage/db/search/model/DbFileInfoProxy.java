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

package org.artifactory.storage.db.search.model;

import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.fs.FileInfo;
import org.artifactory.storage.db.fs.entity.NodePath;

import java.util.Set;

/**
 * Date: 12/4/12
 * Time: 1:51 PM
 *
 * @author freds
 */
public class DbFileInfoProxy extends DbItemInfoProxy implements FileInfo {
    public DbFileInfoProxy(long nodeId, NodePath nodePath) {
        super(nodeId, nodePath);
    }

    @Override
    public FileInfo getMaterialized() {
        return (FileInfo) super.getMaterialized();
    }

    @Override
    public long getAge() {
        return getMaterialized().getAge();
    }

    @Override
    public String getMimeType() {
        return getMaterialized().getMimeType();
    }

    @Override
    public ChecksumsInfo getChecksumsInfo() {
        return getMaterialized().getChecksumsInfo();
    }

    @Override
    public long getSize() {
        return getMaterialized().getSize();
    }

    @Override
    public String getSha1() {
        return getMaterialized().getSha1();
    }

    @Override
    public String getMd5() {
        return getMaterialized().getMd5();
    }

    @Override
    public Set<ChecksumInfo> getChecksums() {
        return getMaterialized().getChecksums();
    }
}
