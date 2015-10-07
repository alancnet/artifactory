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

package org.artifactory.resource;

import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.checksum.Checksum;
import org.artifactory.io.checksum.Checksums;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.StringInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * A resource who's content is already resolved (no need to get it's handle for its content) that holds the
 * resource content in a string.
 *
 * @author Yossi Shaul
 */
public class ResolvedResource implements RepoResource {

    private final RepoResource wrappedResource;
    private final RepoResourceInfo repoResourceInfo;
    private final String content;
    private boolean expirable;

    public ResolvedResource(RepoResource wrappedResource, String content) {
        this(wrappedResource, content, true);
    }

    public ResolvedResource(RepoResource wrappedResource, String content, boolean overrideResourceInfoChecksums) {
        this.wrappedResource = wrappedResource;
        this.content = content;
        this.repoResourceInfo = overrideChecksums(wrappedResource.getInfo(),
                overrideResourceInfoChecksums ? content : null);
    }

    public String getContent() {
        return content;
    }

    @Override
    public RepoPath getRepoPath() {
        return wrappedResource.getRepoPath();
    }

    @Override
    public RepoPath getResponseRepoPath() {
        return wrappedResource.getResponseRepoPath();
    }

    @Override
    public void setResponseRepoPath(RepoPath responsePath) {
        wrappedResource.setResponseRepoPath(responsePath);
    }

    @Override
    public RepoResourceInfo getInfo() {
        return repoResourceInfo;
    }

    @Override
    public boolean isFound() {
        return wrappedResource.isFound();
    }

    @Override
    public boolean isExactQueryMatch() {
        return wrappedResource.isExactQueryMatch();
    }

    @Override
    public boolean isExpired() {
        return wrappedResource.isExpired();
    }

    @Override
    public boolean isMetadata() {
        return wrappedResource.isMetadata();
    }

    @Override
    public long getSize() {
        return wrappedResource.getSize();
    }

    @Override
    public long getCacheAge() {
        return wrappedResource.getCacheAge();
    }

    @Override
    public long getLastModified() {
        return wrappedResource.getLastModified();
    }

    @Override
    public String getMimeType() {
        return wrappedResource.getMimeType();
    }

    private static RepoResourceInfo overrideChecksums(RepoResourceInfo original, String content) {
        if (original == null || content == null) {
            return original;
        }

        try {
            ByteArrayInputStream bais = new StringInputStream(content);

            Checksum[] checksums = Checksums.calculate(bais, ChecksumType.BASE_CHECKSUM_TYPES);
            ChecksumsInfo checksumsInfo = new ChecksumsInfo();
            for (Checksum checksum : checksums) {
                ChecksumInfo checksumInfo =
                        new ChecksumInfo(checksum.getType(), checksum.getChecksum(), checksum.getChecksum());
                checksumsInfo.addChecksumInfo(checksumInfo);
            }
            if (!original.getChecksumsInfo().isIdentical(checksumsInfo)) {
                MutableRepoResourceInfo repoResourceCopy = InfoFactoryHolder.get()
                        .copyRepoResource(original);
                repoResourceCopy.setChecksums(checksumsInfo.getChecksums());
                return repoResourceCopy;
            } else {
                return original;
            }
        } catch (IOException e) {
            // rare since the checksum is calculated on in memory byte array built from a string
            throw new RuntimeException("Failed to calculate content checksum", e);
        }
    }

    @Override
    public boolean isExpirable() {
        return expirable || wrappedResource.isExpirable();
    }

    @Override
    public void expirable() {
        expirable = true;
    }
}
