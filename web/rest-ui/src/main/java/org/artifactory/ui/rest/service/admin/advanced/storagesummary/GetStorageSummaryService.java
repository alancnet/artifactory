package org.artifactory.ui.rest.service.admin.advanced.storagesummary;

import org.artifactory.api.repo.storage.RepoStorageSummaryInfo;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.storage.FileStoreStorageSummary;
import org.artifactory.storage.StorageService;
import org.artifactory.storage.StorageSummaryInfo;
import org.artifactory.ui.rest.model.admin.advanced.storagesummary.BinariesSummary;
import org.artifactory.ui.rest.model.admin.advanced.storagesummary.FileStoreSummary;
import org.artifactory.ui.rest.model.admin.advanced.storagesummary.RepositorySummary;
import org.artifactory.ui.rest.model.admin.advanced.storagesummary.StorageSummary;
import org.artifactory.util.NumberFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetStorageSummaryService implements RestService {

    @Autowired
    private StorageService storageService;


    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        StorageSummary storageSummary = new StorageSummary();
        StorageSummaryInfo storageSummaryInfo = storageService.getStorageSummaryInfo();
        // update binaries summary
        updateBinariesSummary(storageSummaryInfo, storageSummary);
        // update file store summary
        updateFileStoreSummary(storageSummary);
        // update repositorySummary
        updateRepositorySummary(storageSummaryInfo, storageSummary);
        // update response
        response.iModel(storageSummary);
    }

    /**
     * update repository summary
     *
     * @param storageSummaryInfo - summary info
     */
    private void updateRepositorySummary(StorageSummaryInfo storageSummaryInfo, StorageSummary storageSummary) {
        //populate repository info to list of models
        List<RepositorySummary> repositorySummaryList = new ArrayList<>();
        Set<RepoStorageSummaryInfo> repoStorageSummaries = storageSummaryInfo.getRepoStorageSummaries();
        repoStorageSummaries.forEach(repoStorageSummary -> repositorySummaryList.add(
                new RepositorySummary(repoStorageSummary, storageSummaryInfo.getTotalSize())));
        // update total data
        updateTotalRepositoryData(storageSummaryInfo, repositorySummaryList);
        storageSummary.setRepositoriesSummaryList(repositorySummaryList);
    }

    /**
     * update total repository data
     *
     * @param storageSummaryInfo    - storage info data
     * @param repositorySummaryList - list of storage repository models
     */
    private void updateTotalRepositoryData(StorageSummaryInfo storageSummaryInfo,
            List<RepositorySummary> repositorySummaryList) {
        RepositorySummary repositorySummary = new RepositorySummary();
        repositorySummary.setRepoKey("TOTAL");
        repositorySummary.setRepoType(RepoStorageSummaryInfo.RepositoryType.NA);
        repositorySummary.setFoldersCount(storageSummaryInfo.getTotalFolders());
        repositorySummary.setFilesCount(storageSummaryInfo.getTotalFiles());
        repositorySummary.setUsedSpace(StorageUnit.toReadableString(storageSummaryInfo.getTotalSize()));
        repositorySummary.setItemsCount(storageSummaryInfo.getTotalItems());
        repositorySummaryList.add(repositorySummary);
    }

    /**
     * update storage summary with binaries repositories
     *
     * @param storageSummary - storageSummary Model
     */
    private void updateFileStoreSummary(StorageSummary storageSummary) {
        FileStoreStorageSummary fileStoreSummaryInfo = storageService.getFileStoreStorageSummary();
        FileStoreSummary fileStoreSummary = new FileStoreSummary();
        fileStoreSummary.setStorageType(fileStoreSummaryInfo.getBinariesStorageType().toString());
        File binariesFolder = fileStoreSummaryInfo.getBinariesFolder();
        String storageDirLabel = binariesFolder != null ?
                binariesFolder.getAbsolutePath() : "Filesystem storage is not used";
        fileStoreSummary.setStorageDirectory(storageDirLabel);
        fileStoreSummary.setTotalSpace(StorageUnit.toReadableString(fileStoreSummaryInfo.getTotalSpace()));
        fileStoreSummary.setUsedSpace(
                StorageUnit.toReadableString(fileStoreSummaryInfo.getUsedSpace()) + " (" +
                        NumberFormatter.formatPercentage(fileStoreSummaryInfo.getUsedSpaceFraction()) + ")");
        fileStoreSummary.setFreeSpace(
                StorageUnit.toReadableString(fileStoreSummaryInfo.getFreeSpace()) + " (" +
                        NumberFormatter.formatPercentage(fileStoreSummaryInfo.getFreeSpaceFraction()) + ")");
        storageSummary.setFileStoreSummary(fileStoreSummary);
    }

    /**
     * update storage summary with binaries repositories
     *
     * @param storageSummaryInfo - storage info
     * @param storageSummary     - storageSummary Model
     */
    private void updateBinariesSummary(StorageSummaryInfo storageSummaryInfo, StorageSummary storageSummary) {
        BinariesSummary binariesSummary = new BinariesSummary();
        binariesSummary.setBinariesCount(NumberFormatter.formatLong(
                storageSummaryInfo.getBinariesInfo().getBinariesCount()));

        binariesSummary.setBinariesSize(StorageUnit.toReadableString(
                storageSummaryInfo.getBinariesInfo().getBinariesSize()));

        binariesSummary.setOptimization(NumberFormatter.formatPercentage(storageSummaryInfo.getOptimization()));
        binariesSummary.setArtifactsSize(StorageUnit.toReadableString(storageSummaryInfo.getTotalSize()));
        binariesSummary.setItemsCount(NumberFormatter.formatLong((storageSummaryInfo.getTotalItems())));
        binariesSummary.setArtifactsCount(NumberFormatter.formatLong((storageSummaryInfo.getTotalFiles())));

        storageSummary.setBinariesSummary(binariesSummary);
    }
}
