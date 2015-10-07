package org.artifactory.ui.rest.model.admin.advanced.storagesummary;

/**
 * @author Chen Keinan
 */
public class BinariesSummary {

    private String binariesCount;
    private String binariesSize;
    private String artifactsSize;
    private String optimization;
    private String itemsCount;
    private String artifactsCount;
    private String totalSize;

    public String getBinariesSize() {
        return binariesSize;
    }

    public void setBinariesSize(String binariesSize) {
        this.binariesSize = binariesSize;
    }

    public String getArtifactsSize() {
        return artifactsSize;
    }

    public void setArtifactsSize(String artifactsSize) {
        this.artifactsSize = artifactsSize;
    }

    public String getOptimization() {
        return optimization;
    }

    public void setOptimization(String optimization) {
        this.optimization = optimization;
    }

    public String getBinariesCount() {
        return binariesCount;
    }

    public void setBinariesCount(String binariesCount) {
        this.binariesCount = binariesCount;
    }

    public String getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(String itemsCount) {
        this.itemsCount = itemsCount;
    }

    public String getArtifactsCount() {
        return artifactsCount;
    }

    public void setArtifactsCount(String artifactsCount) {
        this.artifactsCount = artifactsCount;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }
}
