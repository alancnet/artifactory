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

import org.artifactory.checksum.ChecksumType;
import org.artifactory.fs.RepoResource;
import org.artifactory.mime.MimeType;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;

/**
 * A checksum resource is used as a response for checksum request and it wraps the actual file resource for which the
 * checksum is requested. Currently it is built only for existing checksum (meaning isFound is always true).
 *
 * @author Yossi Shaul
 */
public class ChecksumResource extends ResolvedResource {
    private final ChecksumType type;

    public ChecksumResource(RepoResource wrappedResource, ChecksumType type, String checksum) {
        super(wrappedResource, checksum, false);
        this.type = type;
    }

    @Override
    public RepoPath getRepoPath() {
        RepoPath fileRepoPath = super.getRepoPath();
        return InternalRepoPathFactory.create(fileRepoPath.getRepoKey(), fileRepoPath.getPath() + type.ext());
    }

    @Override
    public RepoPath getResponseRepoPath() {
        RepoPath repoPath = super.getResponseRepoPath();
        // super might already call this class getRepoPath, so fix the path only if required 
        if (!repoPath.getPath().endsWith(type.ext())) {
            repoPath = InternalRepoPathFactory.create(repoPath.getRepoKey(), repoPath.getPath() + type.ext());
        }
        return repoPath;
    }

    @Override
    public long getSize() {
        return getContent() != null ? getContent().length() : type.length();
    }

    @Override
    public String getMimeType() {
        return MimeType.checksum;
    }
}
