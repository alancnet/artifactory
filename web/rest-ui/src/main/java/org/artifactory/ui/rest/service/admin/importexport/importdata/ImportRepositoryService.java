package org.artifactory.ui.rest.service.admin.importexport.importdata;

import org.apache.commons.io.FileUtils;
import org.artifactory.api.common.ImportExportStatusHolder;
import org.artifactory.api.config.ImportSettingsImpl;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.StatusEntry;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.artifactory.util.Files;
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
public class ImportRepositoryService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ImportRepositoryService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ImportExportStatusHolder status = new ImportExportStatusHolder();
        ImportExportSettings importExportSettings = (ImportExportSettings) request.getImodel();
        boolean isZip = importExportSettings.isZip();

        try {
            // get folder path
            String filePath = importExportSettings.getPath();
            String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
            if (isZip) {
                filePath = uploadDir + "/" + filePath;
            }
            String repoKey = importExportSettings.getRepository();
            boolean importAllRepos = isImportAllRepos(repoKey);
            boolean verbose = importExportSettings.isVerbose();
            boolean excludeMetadata = importExportSettings.isExcludeMetadata();
            File folder = new File(filePath);
            folder = setImportAllRepo(filePath, importAllRepos, folder);
            status.setVerbose(verbose);
            // create import setting
            ImportSettingsImpl importSettings = new ImportSettingsImpl(folder, status);
            // import repository
            importRepository(response, status, repoKey, filePath, importAllRepos, verbose, excludeMetadata,
                    importSettings);
            // delete temp folder after import
            File fileToDelete = new File(uploadDir, filePath);
            if (fileToDelete.exists() && isZip) {
                fileToDelete.delete();
            }
            if (status.isError()) {
                response.error(status.getLastError().getMessage());
            }
        } catch (Exception e) {
            if (isZip) {
                Files.removeFile(new File(importExportSettings.getPath()));
            }
            response.error(e.getMessage());
        }
    }

    /**
     * update import all flag
     *
     * @param repoKey - repo key
     * @return if true - import all repositories
     */
    private boolean isImportAllRepos(String repoKey) {
        if (repoKey.equals("All Repositories")) {
            return true;
        }
        return false;
    }

    /**
     * import repositories data
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param status              - import status
     * @param repoKey             - repository key
     * @param folderPath          - folder path
     * @param importAllRepos      - if true import to all repositories
     * @param verbose             - if true set logging verbose
     * @param excludeMetadata     - if true exclude meta data
     * @param importSettings      - import setting data
     */
    private void importRepository(RestResponse artifactoryResponse, ImportExportStatusHolder status, String repoKey,
                                  String folderPath, boolean importAllRepos, boolean verbose, boolean excludeMetadata,
                                  ImportSettingsImpl importSettings) {
        try {
            importRepository(repoKey, importAllRepos, verbose, excludeMetadata, importSettings);
            // update response feedback
            updateResponseFeedback(artifactoryResponse, status, repoKey, folderPath);
            // delete folder
            String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
            if (folderPath.startsWith(uploadDir)) {
                FileUtils.deleteDirectory(new File(folderPath));
            }
        } catch (Exception e) {
            status.error(e.getMessage(), log);
        }
    }

    /**
     * update response feedback with import status
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param status              - import status
     * @param repoKey             - repository key
     * @param folderPath          - folder path
     */
    private void updateResponseFeedback(RestResponse artifactoryResponse, ImportExportStatusHolder status, String repoKey,
                                        String folderPath) {
        List<StatusEntry> errors = status.getErrors();
        List<StatusEntry> warnings = status.getWarnings();
        if (!errors.isEmpty()) {
            artifactoryResponse.error(" error(s) reported during the import. ");
        } else if (!warnings.isEmpty()) {
            artifactoryResponse.warn(" warning(s) reported during the import.");
        } else {
            artifactoryResponse.info("Successfully imported '" + folderPath + "' into '" + repoKey + "'.");
        }
    }

    /**
     * import repository from folder
     *
     * @param repoKey         - repository key
     * @param importAllRepos  - if true import to all repositories
     * @param verbose         - if true set logging verbose
     * @param excludeMetadata - if true exclude meta data
     * @param importSettings  - import setting data
     */
    private void importRepository(String repoKey, boolean importAllRepos, boolean verbose, boolean excludeMetadata,
                                  ImportSettingsImpl importSettings) {
        updateImportSetting(verbose, excludeMetadata, importSettings);
        if (importAllRepos) {
            repositoryService.importAll(importSettings);
        } else {
            importSettings.setIndexMarkedArchives(true);
            repositoryService.importRepo(repoKey, importSettings);
        }
    }

    /**
     * udpate import setting data
     *
     * @param verbose         - if true set logging verbose
     * @param excludeMetadata - if true exclude meta data
     * @param importSettings  - import setting data
     */
    private void updateImportSetting(boolean verbose, boolean excludeMetadata, ImportSettingsImpl importSettings) {
        importSettings.setFailIfEmpty(true);
        importSettings.setVerbose(verbose);
        importSettings.setIncludeMetadata(!excludeMetadata);
    }

    /**
     * set import all repo settings
     *
     * @param folderPath
     * @param importAllRepos - if true impport to all repositories
     * @param folder         - folder path
     * @return folder path
     */
    private File setImportAllRepo(String folderPath, boolean importAllRepos, File folder) {
        if (importAllRepos) {
            File repositoriesExportDir = new File(folderPath, "repositories");
            if (repositoriesExportDir.isDirectory()) {
                folder = repositoriesExportDir;
            }
        }
        return folder;
    }
}
