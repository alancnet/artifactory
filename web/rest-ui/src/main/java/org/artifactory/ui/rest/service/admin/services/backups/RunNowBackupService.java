package org.artifactory.ui.rest.service.admin.services.backups;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.BackupService;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RunNowBackupService implements RestService {

    @Autowired
    private BackupService backupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("RunNowBackup");
        runNowBackup(request, response);
    }

    /**
     * run now backup
     *
     * @param artifactoryRequest  - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void runNowBackup(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse) {
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        BackupDescriptor backupDescriptor = (BackupDescriptor) artifactoryRequest.getImodel();
        backupService.scheduleImmediateSystemBackup(backupDescriptor, statusHolder);
        updateResponseFeedback(artifactoryResponse, statusHolder);
    }

    /**
     * update response with feedback
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param statusHolder        - msg status holder
     */
    private void updateResponseFeedback(RestResponse artifactoryResponse, BasicStatusHolder statusHolder) {
        if (statusHolder.isError()) {
            artifactoryResponse.error(statusHolder.getStatusMsg());
        } else {
            artifactoryResponse.info("System backup was successfully scheduled to run in the background.");
        }
    }
}
