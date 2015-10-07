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

import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.md.MetadataInfo;
import org.artifactory.md.MutableMetadataInfo;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author yoavl
 */
public class MetadataInfoImpl implements MutableMetadataInfo {
    private static final Logger log = LoggerFactory.getLogger(MetadataInfoImpl.class);

    private final RepoPath repoPath;
    private String name;
    private long created;
    private long lastModified;
    private String lastModifiedBy;
    private long size;
    private ChecksumsInfo checksumsInfo;

    public MetadataInfoImpl(RepoPath parentRepoPath, String metadataName) {
        this.repoPath = getMetadataRepoPath(parentRepoPath, metadataName);
        this.name = metadataName;
        this.checksumsInfo = new ChecksumsInfo();
    }

    public MetadataInfoImpl(RepoPath repoPath) {
        this.repoPath = repoPath;
        this.name = NamingUtils.getMetadataName(repoPath.getPath());
        this.checksumsInfo = new ChecksumsInfo();
    }

    public MetadataInfoImpl(MetadataInfo copy) {
        this.repoPath = copy.getRepoPath();
        this.name = copy.getName();
        this.created = copy.getCreated();
        this.lastModified = copy.getLastModified();
        this.lastModifiedBy = copy.getLastModifiedBy();
        this.size = copy.getSize();
        this.checksumsInfo = new ChecksumsInfo(copy.getChecksumsInfo());
    }

    @Override
    public RepoPath getRepoPath() {
        return repoPath;
    }

    @Override
    public ChecksumsInfo getChecksumsInfo() {
        return checksumsInfo;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getCreated() {
        return created;
    }

    @Override
    public void setCreated(long created) {
        this.created = created;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    @Override
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override
    public String getSha1() {
        return checksumsInfo.getSha1();
    }

    @Override
    public String getMd5() {
        return checksumsInfo.getMd5();
    }

    @Override
    public Set<ChecksumInfo> getChecksums() {
        return checksumsInfo.getChecksums();
    }

    @Override
    public void setChecksums(Set<ChecksumInfo> checksums) {
        checksumsInfo.setChecksums(checksums);
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
    public String toString() {
        return "MetadataInfo{repoPath=" + repoPath + '}';
    }

    private RepoPath getMetadataRepoPath(RepoPath parentRepoPath, String metadataName) {
        String path = parentRepoPath.getPath();
        boolean alreadyMetadataPath = NamingUtils.isMetadata(path);
        if (alreadyMetadataPath) {
            log.warn("Path {} is already a metadata path.", path);
        }
        //TODO: [by yl] Evaluate the impact of normalizing the path to use the standard metadata format (a.jar#mdname)

        return InternalRepoPathFactory.create(parentRepoPath.getRepoKey(),
                alreadyMetadataPath ? path : NamingUtils.getMetadataPath(path, metadataName));
    }
}
