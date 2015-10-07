package org.artifactory.ui.rest.service.admin.services.backups;

import org.artifactory.api.config.CentralConfigService;
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
public class DeleteBackupService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("DeleteBackup");
        String backupId = request.getPathParamByKey("id");
        // delete backup
        deleteBackup(backupId);
        // update feedback
        response.info("Backup " + backupId + " successfully deleted.");
    }

    /**
     * delete Backup from descriptor
     *
     * @param backupId - backup id from request
     */
    private void deleteBackup(String backupId) {
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        mutableDescriptor.removeBackup(backupId);
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
    }
}
