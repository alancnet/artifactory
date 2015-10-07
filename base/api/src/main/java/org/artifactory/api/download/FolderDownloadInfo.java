package org.artifactory.api.download;

/**
 * Holds information about the folder requested for download
 *
 * @author Dan Feldman
 */
public class FolderDownloadInfo {

    private double sizeMb;
    private long totalFiles;

    public FolderDownloadInfo(double sizeMb, long totalFiles) {
        this.sizeMb = sizeMb;
        this.totalFiles = totalFiles;
    }

    public double getSizeMb() {
        return sizeMb;
    }

    public long getTotalFiles() {
        return totalFiles;
    }
}
