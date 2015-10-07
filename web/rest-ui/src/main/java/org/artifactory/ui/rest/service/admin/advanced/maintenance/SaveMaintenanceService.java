package org.artifactory.ui.rest.service.admin.advanced.maintenance;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.cleanup.CleanupConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.gc.GcConfigDescriptor;
import org.artifactory.descriptor.quota.QuotaConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.advanced.maintenance.Maintenance;
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
public class SaveMaintenanceService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("SaveMaintenance");
        Maintenance maintenance = (Maintenance) request.getImodel();
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        updateGarbageCollection(maintenance, mutableDescriptor);
        // update quota
        updateQuotaConfig(maintenance, mutableDescriptor);
        // update cleanup
        updateCleanUpConfig(maintenance, mutableDescriptor);
        // update virtual repo
        updateVirtualRepoCleanUp(maintenance, mutableDescriptor);
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
        response.info("Maintenance settings were successfully saved.");
    }

    /**
     * update virtual repo clean up
     *
     * @param maintenance       -- maintenance model
     * @param mutableDescriptor config descriptor
     */
    private void updateVirtualRepoCleanUp(Maintenance maintenance, MutableCentralConfigDescriptor mutableDescriptor) {
        CleanupConfigDescriptor virtualCacheCleanupConfig = mutableDescriptor.getVirtualCacheCleanupConfig();
        if (virtualCacheCleanupConfig == null) {
            virtualCacheCleanupConfig = new CleanupConfigDescriptor();
        }
        virtualCacheCleanupConfig.setCronExp(maintenance.getCleanVirtualRepoCron());
        mutableDescriptor.setVirtualCacheCleanupConfig(virtualCacheCleanupConfig);
    }

    /**
     * update clean up config
     *
     * @param maintenance       - maintenance model
     * @param mutableDescriptor - config descriptor
     */
    private void updateCleanUpConfig(Maintenance maintenance, MutableCentralConfigDescriptor mutableDescriptor) {
        CleanupConfigDescriptor cleanupConfig = mutableDescriptor.getCleanupConfig();
        if (cleanupConfig == null) {
            cleanupConfig = new CleanupConfigDescriptor();
        }
        cleanupConfig.setCronExp(maintenance.getCleanUnusedCachedCron());
        mutableDescriptor.setCleanupConfig(cleanupConfig);
    }

    /**
     * update quota config data
     *
     * @param maintenance       - maintenance model
     * @param mutableDescriptor - config descriptor
     */
    private void updateQuotaConfig(Maintenance maintenance, MutableCentralConfigDescriptor mutableDescriptor) {
        QuotaConfigDescriptor quotaConfig = mutableDescriptor.getQuotaConfig();
        if (quotaConfig == null) {
            quotaConfig = new QuotaConfigDescriptor();
        }
        quotaConfig.setEnabled(maintenance.isQuotaControl());
        quotaConfig.setDiskSpaceLimitPercentage(maintenance.getStorageLimit());
        quotaConfig.setDiskSpaceWarningPercentage(maintenance.getStorageWarning());
        mutableDescriptor.setQuotaConfig(quotaConfig);
    }

    /**
     * update garbage collection config
     *
     * @param maintenance       -- maintenance model
     * @param mutableDescriptor - config descriptor
     */
    private void updateGarbageCollection(Maintenance maintenance, MutableCentralConfigDescriptor mutableDescriptor) {
        GcConfigDescriptor gcConfig = mutableDescriptor.getGcConfig();
        if (gcConfig == null) {
            gcConfig = new GcConfigDescriptor();
        }
        gcConfig.setCronExp(maintenance.getGarbageCollectorCron());
    }
}
