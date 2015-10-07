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

package org.artifactory.api.download;

import org.artifactory.api.archive.ArchiveType;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.repo.RepoPath;

import java.io.InputStream;

/**
 * A service for downloading entire directory contents as an archive.
 *
 * @author Dan Feldman
 */
public interface FolderDownloadService {

    /**
     * Tries to acquire an available download slot from the pool (which is limited by the amount specified in the config)
     *
     * @return true if slot acquired.
     */
    boolean getAvailableDownloadSlot();

    /**
     * Releases a download slot to the pool
     */
    void releaseDownloadSlot();

    /**
     * Collects all files under the current folder and returns an OutputStream with a type as required by
     * {@param archiveType}. Files that the user doesn't have permission to read are filtered and not included in the
     * archive.
     *
     * NOTE: It is the callers responsibility to acquire a download slot with {@link this#getAvailableDownloadSlot()}
     * when starting the operation and release it with {@link this#releaseDownloadSlot()} when done (and closing)
     * <b>writing the stream</b>
     *
     * @param folder      - Folder to download
     * @param archiveType - Type of archive to stream files in
     * @param status      - Status of operation
     * @return - OutputStream based on {@param archiveType}
     */
    InputStream process(RepoPath folder, ArchiveType archiveType, BasicStatusHolder status);

    /**
     * Collects file count and total size for the requested folder for the UI to show
     *
     * @param folder folder to get info about
     * @return FolderDownloadInfo model with the info
     */
    FolderDownloadInfo collectFolderInfo(RepoPath folder);

    /**
     * @return the {@link FolderDownloadConfigDescriptor}
     */
    FolderDownloadConfigDescriptor getFolderDownloadConfig();
}
