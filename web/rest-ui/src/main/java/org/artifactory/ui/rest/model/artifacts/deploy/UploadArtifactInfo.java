package org.artifactory.ui.rest.model.artifacts.deploy;

import org.artifactory.api.artifact.UnitInfo;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.ui.rest.model.utils.FileUpload;

import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * @author Chen Keinan
 */
public class UploadArtifactInfo extends BaseModel {

    private FileUpload fileUpload;
    private UnitInfo unitInfo;
    private String fileName;
    private String repoKey;
    private String unitConfigFileContent;
    private boolean publishUnitConfigFile;

    public UploadArtifactInfo() {
    }
    public UploadArtifactInfo(FormDataMultiPart fileUpload) {
        this.fileUpload = new FileUpload(fileUpload);
    }

    public FormDataMultiPart fetchFormDataMultiPart() {
        return fileUpload.getFormDataMultiPart();
    }

    public UnitInfo getUnitInfo() {
        return unitInfo;
    }

    public void setUnitInfo(UnitInfo unitInfo) {
        this.unitInfo = unitInfo;
    }

    public void cleanData() {
        fileUpload = null;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getUnitConfigFileContent() {
        return unitConfigFileContent;
    }

    public void setUnitConfigFileContent(String unitConfigFileContent) {
        this.unitConfigFileContent = unitConfigFileContent;
    }

    public boolean isPublishUnitConfigFile() {
        return publishUnitConfigFile;
    }

    public void setPublishUnitConfigFile(boolean publishUnitConfigFile) {
        this.publishUnitConfigFile = publishUnitConfigFile;
    }
}
