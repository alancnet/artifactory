package org.artifactory.ui.rest.model.setmeup;

import org.artifactory.rest.common.model.FileModel;
import org.artifactory.rest.common.model.RestModel;

/**
 * @author chen keinan
 */
public class ScriptDownload implements RestModel, FileModel {
    private String fileContent;

    @Override
    public Object getFileResource() {
        return fileContent;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
}
