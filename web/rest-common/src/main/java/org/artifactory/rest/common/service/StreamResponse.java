package org.artifactory.rest.common.service;

/**
 * @author Chen Keinan
 */
public interface StreamResponse {

    public void setDownloadFile(String fileName);

    public String getFileName();

    public void setDownload(boolean isDownload);
}
