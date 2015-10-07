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

package org.artifactory.api.repo;

import org.artifactory.repo.RepoPath;

import java.io.Serializable;

/**
 * A value object with results for archive file content request.
 *
 * @author Yossi Shaul
 */
public class ArchiveFileContent implements Serializable {

    private String content;
    private RepoPath sourceArchive;
    private String sourcePath;
    private String reason;

    public ArchiveFileContent(String content, RepoPath sourceArchive, String sourcePath) {
        this.content = content;
        this.sourceArchive = sourceArchive;
        this.sourcePath = sourcePath;
    }

    /**
     * @return The content of the source, null if ont found.
     */
    public String getContent() {
        return content;
    }

    /**
     * @return Repo path of the archive containing the source.
     */
    public RepoPath getSourceArchive() {
        return sourceArchive;
    }

    /**
     * @return Source file path inside the archive.
     */
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * @return Failure reason if the source was not found.
     */
    public String getFailureReason() {
        return reason;
    }

    public static ArchiveFileContent contentNotFound(String reason) {
        ArchiveFileContent notFound = new ArchiveFileContent(null, null, null);
        notFound.reason = reason;
        return notFound;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
