package org.artifactory.ui.rest.model.admin.services.filesystem;

import java.io.File;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class FileSystemItem extends BaseModel {
    private String fileSystemItemName;
    private boolean isFolder;

    public FileSystemItem(File file) {
        fileSystemItemName = file.getName();
        isFolder = file.isDirectory();
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    public String getFileSystemItemName() {
        return fileSystemItemName;
    }

    public void setFileSystemItemName(String fileSystemItemName) {
        this.fileSystemItemName = fileSystemItemName;
    }
}
