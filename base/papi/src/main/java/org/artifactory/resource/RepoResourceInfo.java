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
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.common.Info;
import org.artifactory.repo.RepoPath;

import java.util.Set;

/**
 * Date: 8/1/11
 * Time: 7:09 PM
 *
 * @author Fred Simon
 */
public interface RepoResourceInfo extends Info {
    RepoPath getRepoPath();

    String getName();

    long getLastModified();

    long getSize();

    /**
     * @return The actual sha1 checksum of the file. Null if not determined yet.
     */
    String getSha1();

    /**
     * @return The actual md5 checksum of the file. Null if not determined yet.
     */
    String getMd5();

    ChecksumsInfo getChecksumsInfo();

    /**
     * @return Set of checksum infos
     * @deprecated Should use the container getter
     */
    @Deprecated
    Set<ChecksumInfo> getChecksums();
}
