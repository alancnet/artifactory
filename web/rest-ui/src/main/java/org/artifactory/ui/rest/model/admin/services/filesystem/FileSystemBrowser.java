package org.artifactory.ui.rest.model.admin.services.filesystem;

import java.util.List;

import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.model.RestModel;

/**
 * @author Chen Keinan
 */
public class FileSystemBrowser extends BaseModel {

    private List<String> roots;
    private List<RestModel> fileSystemItems;
    private Boolean isWindows;

    public List<String> getRoots() {
        return roots;
    }

    public void setRoots(List<String> roots) {
        this.roots = roots;
    }

    public List<RestModel> getFileSystemItems() {
        return fileSystemItems;
    }

    public void setFileSystemItems(List<RestModel> fileSystemItems) {
        this.fileSystemItems = fileSystemItems;
    }

    public boolean isWindows() {
        return isWindows;
    }

    public void setIsWindows(boolean isWindows) {
        this.isWindows = isWindows;
    }
}
