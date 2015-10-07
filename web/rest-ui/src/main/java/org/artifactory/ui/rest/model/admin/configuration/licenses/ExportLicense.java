package org.artifactory.ui.rest.model.admin.configuration.licenses;

import org.artifactory.rest.common.model.FileModel;
import org.artifactory.rest.common.model.RestModel;

/**
 * @author Chen Keinan
 */
public class ExportLicense implements RestModel, FileModel {

    private String licenseXMLFile;

    public ExportLicense(String licenseXMLFile) {
        this.licenseXMLFile = licenseXMLFile;
    }

    public String toString() {
        return licenseXMLFile.toString();
    }

    @Override
    public Object getFileResource() {
        return licenseXMLFile;
    }
}
