package org.artifactory.ui.rest.model.admin.advanced.systemlogs;

import org.artifactory.rest.common.model.FileModel;
import org.artifactory.rest.common.model.RestModel;

import java.io.FileInputStream;

/**
 * @author Lior Hasson
 */
public class SystemLogFile implements RestModel, FileModel {
    private FileInputStream stream;

    @Override
    public Object getFileResource() {
        return getStream();
    }

    public FileInputStream getStream() {
        return stream;
    }

    public void setStream(FileInputStream stream) {
        this.stream = stream;
    }
}
