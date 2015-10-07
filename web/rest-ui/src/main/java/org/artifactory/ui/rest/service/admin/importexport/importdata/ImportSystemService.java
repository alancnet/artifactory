package org.artifactory.ui.rest.service.admin.importexport.importdata;

import org.apache.commons.io.FileUtils;
import org.artifactory.api.common.ImportExportStatusHolder;
import org.artifactory.api.config.ImportSettingsImpl;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.StatusEntry;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.artifactory.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ImportSystemService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(ImportSystemService.class);

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ImportExportSettings importExportSettings = (ImportExportSettings) request.getImodel();
        ImportExportStatusHolder status = new ImportExportStatusHolder();
        boolean isZip = importExportSettings.isZip();
        File importFromFolder = null;
        File importFromPath = null;
        try {
            String pathname = importExportSettings.getPath();
            importFromFolder = new File(pathname);
            String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
            if (isZip) {
                importFromPath = new File(uploadDir, importFromFolder.getName() + "_extract");
                ZipUtils.extract(importFromFolder, importFromPath);
            } else {
                importFromPath = new File(pathname);
            }
            // return if path not exist
            if (!importFromPath.exists()) {
                updateResponseFeedback(response, importFromPath);
                return;
            }
            // return if folder path do not contain files
            if (importFromPath.isDirectory()) {
                if (updateResponse(response, importFromPath)) {
                    return;
                }
                importFromFolder = importFromPath;
            }
            // import system data
            List<StatusEntry> warnings = importSystem(status, importExportSettings, importFromFolder);
            // update response with post import warnings
            updateResponsePostImport(response, status, importFromPath, warnings);
        } catch (Exception e) {
            response.error("Failed to import system from '" + importFromPath + "': " +
                    e.getMessage());
            log.error("Failed to import system.", e);
        } finally {
            cleanFolderAndStatus(status, importFromFolder, importFromPath, isZip);
        }
    }

    /**
     * delete folder and clean status
     *
     * @param status           - import status
     * @param importFromFolder - import folder
     * @param importFromPath   - import folder path
     */
    private void cleanFolderAndStatus(ImportExportStatusHolder status, File importFromFolder, File importFromPath,
                                      boolean isZip) {
        if (isZip(importFromPath)) {
            //Delete the extracted dir
            try {
                if (importFromFolder != null && isZip) {
                    FileUtils.deleteDirectory(importFromFolder);
                }
            } catch (IOException e) {
                log.warn("Failed to delete export directory: " +
                        importFromFolder, e);
            }
        }
        status.reset();
    }

    private void updateResponsePostImport(RestResponse artifactoryResponse, ImportExportStatusHolder status,
                                          File importFromPath, List<StatusEntry> warnings) {
        if (!warnings.isEmpty()) {
            artifactoryResponse.warn(" Warnings have been produces during the export. Please ");
        }
        List<String> errors = new ArrayList<>();
        if (status.isError()) {
            int errorCount = status.getErrors().size();
            if (errorCount > 1) {
                String msg = errorCount + " errors occurred while importing system from '" + importFromPath + "': For more accurate information, please look at the log.";
                status.getErrors().forEach(error -> errors.add(error.getMessage()));
                artifactoryResponse.error(msg);
            } else {
                String msg = "Error while importing system from '" + importFromPath + "': " + status.getStatusMsg();
                artifactoryResponse.errors(errors);
                log.error(msg);
            }
        } else {
            artifactoryResponse.info("Successfully imported system from '" + importFromPath + "'.");
        }
    }

    /**
     * import system data
     *
     * @param status               - import status
     * @param importExportSettings - data import model - hold imports flags
     * @param importFromFolder     - import folder path
     * @return
     */
    private List<StatusEntry> importSystem(ImportExportStatusHolder status, ImportExportSettings importExportSettings,
                                           File importFromFolder) {
        status.status("Importing from directory...", log);
        boolean verbose = importExportSettings.isVerbose();
        boolean excludeMetadata = importExportSettings.isExcludeMetadata();
        boolean excludeContent = importExportSettings.isExcludeContent();
        ArtifactoryContext context = ContextHelper.get();
        // update import system flags data
        ImportSettingsImpl importSettings = new ImportSettingsImpl(importFromFolder, status);
        importSettings.setFailFast(false);
        importSettings.setFailIfEmpty(true);
        importSettings.setVerbose(verbose);
        importSettings.setIncludeMetadata(!excludeMetadata);
        importSettings.setExcludeContent(excludeContent);
        // import system data
        context.importFrom(importSettings);
        return status.getWarnings();
    }

    /**
     * update response
     *
     * @param artifactoryResponse
     * @param importFromPath
     * @return
     */
    private boolean updateResponse(RestResponse artifactoryResponse, File importFromPath) {
        if (importFromPath.list().length == 0) {
            artifactoryResponse.error("Directory '" + importFromPath + "' is empty.");
            return true;
        }
        return false;
    }

    /**
     * update response feedback
     *
     * @param artifactoryResponse - encapsulate
     * @param importFromPath      - import folder path
     */
    private void updateResponseFeedback(RestResponse artifactoryResponse, File importFromPath) {
        artifactoryResponse.error("Specified location '" + importFromPath +
                "' does not exist.");
        return;
    }

    /**
     * check if file is zip or not
     *
     * @param file - file to upload
     * @return if true file is zip
     */
    private boolean isZip(File file) {
        return file.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip_extract");
    }
}
