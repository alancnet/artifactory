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

package org.artifactory.sapi.common;

import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;

import java.io.File;

/**
 * Date: 12/12/11
 * Time: 11:24
 *
 * @author Dror Bereznitsky
 */
public interface FileExportInfo {
    FileInfo getFileInfo();

    FolderInfo getParentInfo();

    File getTargetPath();

    FileExportStatus status();

    public enum FileExportStatus {
        PENDING, ADDED, REMOVED, SKIPPED;
    }
}
