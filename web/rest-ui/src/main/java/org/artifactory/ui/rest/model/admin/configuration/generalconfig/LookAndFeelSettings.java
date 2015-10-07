package org.artifactory.ui.rest.model.admin.configuration.generalconfig;

import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.ui.rest.model.utils.FileUpload;

/**
 * @author Chen keinan
 */
public class LookAndFeelSettings extends BaseModel {

    private FileUpload fileUpload;
    private String logoUrl;

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public void setFileUpload(FileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
