package org.artifactory.rest.common.service;

import java.io.File;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.artifactory.rest.common.model.FileData;
import org.artifactory.rest.common.model.FileModel;
import org.artifactory.rest.common.util.ResponseHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component("streamingRestResponse")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class StreamRestResponse extends ArtifactoryRestResponse implements StreamResponse {

    private String fileName;
    private boolean isDownload;
    private File file;
    private boolean fileEntity;

    @Override
    public Response buildResponse() {
        if (super.getIModel() != null && super.getIModel() instanceof FileModel) {
            fileEntity = true;
            return ResponseHandler.buildFileResponse(this, isDownload);
        } else if (super.getIModel() != null && super.getIModel() instanceof StreamingOutput) {
            fileEntity = false;
            return ResponseHandler.buildFileResponse(this, isDownload);
        } else {
            fileEntity = false;
            return super.buildResponse();
        }

    }

    @Override
    public void setDownloadFile(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setDownload(boolean isDownload) {
        this.isDownload = isDownload;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public Object getEntity() {
        if (fileEntity) {
            return ((FileModel) getIModel()).getFileResource();
        } else if (getIModel() instanceof FileData) {
            return file;
        } else {
            return super.getEntity();
        }
    }
}
