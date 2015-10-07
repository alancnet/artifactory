package org.artifactory.ui.rest.model.admin.advanced.storagesummary;

import org.artifactory.api.repo.storage.RepoStorageSummaryInfo;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.util.NumberFormatter;

/**
 * @author Chen Keinan
 */
public class RepositorySummary {

    private String repoKey;
    private RepoStorageSummaryInfo.RepositoryType repoType;
    private long foldersCount;
    private long filesCount;
    private String usedSpace;
    private long itemsCount;
    private Double percentage;
    private String displayPercentage;
    private String packageType;

    public RepositorySummary() {
    }

    public RepositorySummary(RepoStorageSummaryInfo repoStorageSummaryInfo, long totalSize) {
        this.setRepoKey(repoStorageSummaryInfo.getRepoKey());
        this.setUsedSpace(StorageUnit.toReadableString(repoStorageSummaryInfo.getUsedSpace()));
        this.setPercentage(Double.longBitsToDouble(repoStorageSummaryInfo.getUsedSpace()) / Double.longBitsToDouble(totalSize));
        this.setDisplayPercentage(NumberFormatter.formatPercentage(getPercentage()));
        this.setFilesCount(repoStorageSummaryInfo.getFilesCount());
        this.setFoldersCount(repoStorageSummaryInfo.getFoldersCount());
        this.setItemsCount(repoStorageSummaryInfo.getItemsCount());
        this.packageType = repoStorageSummaryInfo.getType();
        this.repoType = repoStorageSummaryInfo.getRepoType();
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public RepoStorageSummaryInfo.RepositoryType getRepoType() {
        return repoType;
    }

    public void setRepoType(RepoStorageSummaryInfo.RepositoryType repoType) {
        this.repoType = repoType;
    }

    public String getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(String usedSpace) {
        this.usedSpace = usedSpace;
    }

    public long getFoldersCount() {
        return foldersCount;
    }

    public void setFoldersCount(long foldersCount) {
        this.foldersCount = foldersCount;
    }

    public long getFilesCount() {
        return filesCount;
    }

    public void setFilesCount(long filesCount) {
        this.filesCount = filesCount;
    }

    public long getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(long itemsCount) {
        this.itemsCount = itemsCount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getDisplayPercentage() {
        return displayPercentage;
    }

    public void setDisplayPercentage(String displayPercentage) {
        this.displayPercentage = displayPercentage;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }
}
