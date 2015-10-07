package org.artifactory.ui.rest.model.admin.advanced.storagesummary;

import java.util.List;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class StorageSummary extends BaseModel {

    private BinariesSummary binariesSummary;
    private FileStoreSummary fileStoreSummary;
    private List<RepositorySummary> repositoriesSummaryList;

    public BinariesSummary getBinariesSummary() {
        return binariesSummary;
    }

    public void setBinariesSummary(BinariesSummary binariesSummary) {
        this.binariesSummary = binariesSummary;
    }

    public FileStoreSummary getFileStoreSummary() {
        return fileStoreSummary;
    }

    public void setFileStoreSummary(FileStoreSummary fileStoreSummary) {
        this.fileStoreSummary = fileStoreSummary;
    }

    public List<RepositorySummary> getRepositoriesSummaryList() {
        return repositoriesSummaryList;
    }

    public void setRepositoriesSummaryList(List<RepositorySummary> repositoriesSummaryList) {
        this.repositoriesSummaryList = repositoriesSummaryList;
    }
}
