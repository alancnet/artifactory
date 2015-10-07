package org.artifactory.traffic;

/*
 * @author Lior Azar
 */
public class TransferUsage {

    private long download; // downloaded bytes (without the excluded ips)
    private long upload;    // uploaded bytes (without the excluded ips)
    private long excludedDownload;  // excluded download traffic in bytes
    private long excludedUpload; // excluded upload traffic in bytes

    public long getDownload() {
        return download;
    }

    public long getUpload() {
        return upload;
    }

    public long getExcludedDownload() {
        return excludedDownload;
    }

    public long getExcludedUpload() {
        return excludedUpload;
    }

    public void setDownload(long download) {
        this.download = download;
    }

    public void setUpload(long upload) {
        this.upload = upload;
    }

    public void setExcludedDownload(long excludedDownload) {
        this.excludedDownload = excludedDownload;
    }

    public void setExcludedUpload(long excludedUpload) {
        this.excludedUpload = excludedUpload;
    }
}
