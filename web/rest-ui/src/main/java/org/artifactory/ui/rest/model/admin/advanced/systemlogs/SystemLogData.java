package org.artifactory.ui.rest.model.admin.advanced.systemlogs;

import org.artifactory.rest.common.model.BaseModel;

import java.util.Date;

/**
 * @author Lior Hasson
 */
public class SystemLogData extends BaseModel {
    private Date lastUpdateModified;
    private Date lastUpdateLabel;
    private String logContent;
    private long fileSize;

    public long getFileSize() {
        return fileSize;
    }

    public Date getLastUpdateModified() {
        return lastUpdateModified;
    }

    public Date getLastUpdateLabel() {
        return lastUpdateLabel;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLastUpdateModified(Date lastUpdateModified) {
        this.lastUpdateModified = lastUpdateModified;
    }

    public void setLastUpdateLabel(Date lastUpdateLabel) {
        this.lastUpdateLabel = lastUpdateLabel;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
