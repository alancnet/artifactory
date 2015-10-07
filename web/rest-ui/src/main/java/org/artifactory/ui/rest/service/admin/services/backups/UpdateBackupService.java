package org.artifactory.ui.rest.service.admin.services.backups;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
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
public class UpdateBackupService implements RestService {
    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("UpdateBackup");
        BackupDescriptor backupDescriptor = (BackupDescriptor) request.getImodel();
        // update backup
        updateBackup(backupDescriptor);
        // update feedback msg
        response.info("Successfully updated backup '" + backupDescriptor.getKey() + "'");
    }

    /**
     * update backup to descriptor
     *
     * @param backupDescriptor - updated backup descriptor
     */
    private void updateBackup(BackupDescriptor backupDescriptor) {
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        mutableDescriptor.removeBackup(backupDescriptor.getKey());
        mutableDescriptor.addBackup(backupDescriptor);
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
    }
}
