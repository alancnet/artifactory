package org.artifactory.ui.rest.service.admin.services.backups;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.services.backups.Backup;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBackupService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("GarbageCollection");
        String backupName = request.getPathParamByKey("id");
        // populate data to backup model and update response
        populateBackupModelAndUpdateResponse(response, backupName);
    }

    /**
     * populate data to backup model and update response data
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param backupName          - backup name
     */
    private void populateBackupModelAndUpdateResponse(RestResponse artifactoryResponse, String backupName) {
        if (isMultiBackup(backupName)) {
            // get backup models list from config descriptor
            List<RestModel> backupList = getMultiBackupModels();
            artifactoryResponse.iModelList(backupList);
        } else {
            RestModel singleBackupModels = getSingleBackupModels(backupName);
            artifactoryResponse.iModel(singleBackupModels);
        }
    }

    /**
     * check if  single or multi backup is require based on path param data
     *
     * @param backupName - path param
     * @return if true require multi backup
     */
    private boolean isMultiBackup(String backupName) {
        return backupName == null || backupName.length() == 0;
    }


    /**
     * get BackupModel from Descriptor
     *
     * @return list of backup models
     */
    private List<RestModel> getMultiBackupModels() {
        List<BackupDescriptor> backupDescriptorsList = centralConfigService.getMutableDescriptor().getBackups();
        List<RestModel> backupList = new ArrayList<>();
        // populate backup descriptor to model
        populateMultiBackupDescriptorToModel(backupDescriptorsList, backupList);
        return backupList;
    }

    /**
     * get BackupModel from Descriptor
     *
     * @return l backup model
     */
    private RestModel getSingleBackupModels(String backupName) {
        BackupDescriptor backup = centralConfigService.getMutableDescriptor().getBackup(backupName);
        // populate backup descriptor to model
        return populateSingleBackupDescriptorToModel(backup);
    }

    /**
     * populate backup descriptor to model
     *
     * @param backupDescriptorsList - backup descriptor list
     * @param backupList            - backup model list
     */
    private void populateMultiBackupDescriptorToModel(List<BackupDescriptor> backupDescriptorsList,
            List<RestModel> backupList) {
        backupDescriptorsList.forEach(backupDescriptor -> {
            backupList.add(new Backup(backupDescriptor, false));
        });
    }

    /**
     * populate backup descriptor to model
     *
     * @param backupDescriptor - backup descriptor
     * @return backup model
     */
    private RestModel populateSingleBackupDescriptorToModel(BackupDescriptor backupDescriptor) {
        return new Backup(backupDescriptor, true);
    }
}
