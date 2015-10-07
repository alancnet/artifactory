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

package org.artifactory.backup;

import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.sapi.common.FileExportInfo;

import java.io.File;

/**
 * Date: 12/12/11
 * Time: 11:29
 *
 * @author Dror Bereznitsky
 */
public class FileExportInfoImpl implements FileExportInfo {
    private FolderInfo parentInfo;
    private FileInfo fileInfo;
    private File targetPath;
    private FileExportStatus status;

    public FileExportInfoImpl(FileInfo fileInfo, File targetPath, FileExportStatus status) {
        this.fileInfo = fileInfo;
        this.targetPath = targetPath;
        this.status = status;
    }

    public FileExportInfoImpl(FolderInfo parentInfo, File targetPath, FileExportStatus status) {
        this.parentInfo = parentInfo;
        this.targetPath = targetPath;
        this.status = status;
    }

    @Override
    public FileInfo getFileInfo() {
        return fileInfo;
    }

    @Override
    public FolderInfo getParentInfo() {
        return parentInfo;
    }

    @Override
    public File getTargetPath() {
        return targetPath;
    }

    @Override
    public FileExportStatus status() {
        return status;
    }
}
