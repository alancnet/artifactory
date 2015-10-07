package org.artifactory.ui.rest.service.admin.importexport.exportdata;

import org.artifactory.api.common.ImportExportStatusHolder;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.api.repo.BackupService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.StatusEntry;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExportRepositoryService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ExportRepositoryService.class);
    private static final String ALL_REPOS = "All Repositories";

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    BackupService backupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ImportExportSettings importExportSettings = (ImportExportSettings) request.getImodel();
        /// get export flags from model
        File exportToPath = new File(importExportSettings.getPath());
        boolean excludeMetadata = importExportSettings.isExcludeMetadata();
        boolean m2Compatible = importExportSettings.isCreateM2CompatibleExport();
        boolean verbose = importExportSettings.isVerbose();
        String sourceRepoKey = importExportSettings.getRepository();
        ImportExportStatusHolder status = new ImportExportStatusHolder();
        ExportSettingsImpl exportSettings = new ExportSettingsImpl(exportToPath, status);
        // update export settings flags
        updateExportSettings(excludeMetadata, m2Compatible, verbose, exportSettings);
        try {
            // export repository data
            exportRepository(exportToPath, sourceRepoKey, exportSettings);
            // update response with export status
            updateResponseWithExportStatus(response, exportToPath, sourceRepoKey, status);
        } catch (Exception e) {
            updateErrorStatus(response, e);
        }
    }

    /**
     * update export settings flags
     *
     * @param excludeMetadata
     * @param m2Compatible
     * @param verbose
     * @param exportSettings
     */
    private void updateExportSettings(boolean excludeMetadata, boolean m2Compatible, boolean verbose,
            ExportSettingsImpl exportSettings) {
        exportSettings.setIncludeMetadata(!excludeMetadata);
        exportSettings.setM2Compatible(m2Compatible);
        exportSettings.setVerbose(verbose);
    }

    /**
     * update export error status
     *
     * @param artifactoryResponse
     * @param e
     */
    private void updateErrorStatus(RestResponse artifactoryResponse, Exception e) {
        String message = "Exception occurred during export: ";
        artifactoryResponse.error(message + e.getMessage());
        log.error(message, e);
    }

    /**
     * update response with info , warn and error status
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param exportToPath        - export to path
     * @param sourceRepoKey       - export repo key or all
     * @param status              - export status
     */
    private void updateResponseWithExportStatus(RestResponse artifactoryResponse, File exportToPath, String sourceRepoKey,
            ImportExportStatusHolder status) {
        List<StatusEntry> warnings = status.getWarnings();
        if (!warnings.isEmpty()) {
            artifactoryResponse.warn(
                    "Warnings have been produces during the export. Please review the logs further information.");
        }
        if (status.isError()) {
            String message = status.getStatusMsg();
            Throwable exception = status.getException();
            if (exception != null) {
                message = exception.getMessage();
            }
            artifactoryResponse.error(
                    "Failed to export from: " + sourceRepoKey + "' to '" + exportToPath + "'. Cause: " +
                            message);
        } else {
            artifactoryResponse.info("Successfully exported '" + sourceRepoKey + "' to '" + exportToPath + "'.");
        }
    }

    /**
     * export repository data to path
     *
     * @param exportToPath   - export to path
     * @param sourceRepoKey  - specific repo key or all
     * @param exportSettings - export settings
     */
    private void exportRepository(File exportToPath, String sourceRepoKey, ExportSettingsImpl exportSettings) {
        if (ALL_REPOS.equals(sourceRepoKey)) {
            backupService.backupRepos(exportToPath, exportSettings);
        } else {
            repositoryService.exportRepo(sourceRepoKey, exportSettings);
        }
    }
}

