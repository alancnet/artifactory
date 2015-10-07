package org.artifactory.ui.rest.service.artifacts.search.searchresults;

import org.artifactory.api.common.ImportExportStatusHolder;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.common.StatusEntry;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.artifactory.ui.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExportSearchResultsService extends BaseSearchResultService {
    private static final Logger log = LoggerFactory.getLogger(RemoveSearchResultsService.class);

    @Autowired
    RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String searchName = request.getQueryParamByKey("name");
        ImportExportSettings setting = (ImportExportSettings) request.getImodel();
        SavedSearchResults savedSearchResults = RequestUtils.getResultsFromRequest(searchName,
                request.getServletRequest());
        ImportExportStatusHolder status = new ImportExportStatusHolder();
        String path = setting.getPath();
        try {
            // export results to specified path
            List<StatusEntry> warnings = exportSearchResultsToPath(setting, savedSearchResults, status, path);
            if (!warnings.isEmpty()) {
                updateWarnMessage(response);
            }
            if (status.isError()) {
                updateErrorMessage(response, searchName, status, path);
            } else {
                updateInfoMessage(response, searchName, path);
            }
        } catch (Exception e) {
            String message = "Exception occurred during export: " + e.getMessage();
            response.error(message);
            log.error(message, e);
        }
    }

    /**
     * export search results to path
     *
     * @param setting            - export settings
     * @param savedSearchResults - saved search results
     * @param status             - export results status
     * @param path               - export to path
     * @return
     */
    private List<StatusEntry> exportSearchResultsToPath(ImportExportSettings setting,
            SavedSearchResults savedSearchResults, ImportExportStatusHolder status, String path) {
        ExportSettingsImpl baseSettings = new ExportSettingsImpl(new File(path), status);
        baseSettings.setIncludeMetadata(!setting.isExcludeMetadata());
        baseSettings.setM2Compatible(setting.isCreateM2CompatibleExport());
        baseSettings.setCreateArchive(setting.isCreateZipArchive());
        baseSettings.setVerbose(setting.isVerbose());
        repoService.exportSearchResults(savedSearchResults, baseSettings);
        return status.getWarnings();
    }

    /**
     * update info message
     *
     * @param response   - encapsulate data related to response
     * @param searchName - search name
     * @param path       - export to path
     */
    private void updateInfoMessage(RestResponse response, String searchName, String path) {
        response.info("Successfully exported '" + searchName + "' to '" + path + "'.");
    }

    /**
     * update error message
     *
     * @param response   - encapsulate data related to response
     * @param searchName - search name
     * @param status     - export status
     * @param path       - export to path
     */
    private void updateErrorMessage(RestResponse response, String searchName, ImportExportStatusHolder status,
            String path) {
        String message = status.getStatusMsg();
        Throwable exception = status.getException();
        if (exception != null) {
            message = exception.getMessage();
        }
        response.error("Failed to export from: " + searchName + "' to '" + path + "'. Cause: " +
                message);
    }

    /**
     * update warn message
     *
     * @param response - encapsulate data related to response
     */
    private void updateWarnMessage(RestResponse response) {
        response.warn(" warning(s) reported during the export. Please review the logs for further information.");
    }

}
