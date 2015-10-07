package org.artifactory.ui.rest.model.utils;

import java.io.File;

import org.artifactory.rest.common.model.BaseModel;

import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * @author Chen Keinan
 */
public class FileUpload extends BaseModel {

    private String folderName;
    private FormDataMultiPart formDataMultiPart;
    private File file;

    public FileUpload(FormDataMultiPart fileUpload) {
        this.formDataMultiPart = fileUpload;
    }

    public FileUpload(String folderName) {
        this.folderName = folderName;
    }

    public FileUpload() {}

    public FormDataMultiPart getFormDataMultiPart() {
        return formDataMultiPart;
    }

    public void setFormDataMultiPart(FormDataMultiPart formDataMultiPart) {
        this.formDataMultiPart = formDataMultiPart;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
