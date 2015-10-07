package org.artifactory.ui.rest.model.admin.advanced.maintenance;

import org.artifactory.descriptor.cleanup.CleanupConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.gc.GcConfigDescriptor;
import org.artifactory.descriptor.quota.QuotaConfigDescriptor;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class Maintenance extends BaseModel {
    private String garbageCollectorCron;
    private String cleanUnusedCachedCron;
    private String cleanVirtualRepoCron;
    private boolean quotaControl;
    private Integer storageLimit;
    private Integer storageWarning;

    public Maintenance() {
    }

    public Maintenance(MutableCentralConfigDescriptor mutableDescriptor) {
        updateGcConfig(mutableDescriptor);
        updateCleanUnusedCache(mutableDescriptor);
        updateCacheCleanupConfig(mutableDescriptor);
        updateQuotaConfig(mutableDescriptor);
    }

    /**
     * update gc config
     *
     * @param mutableDescriptor - config descriptor
     */
    private void updateGcConfig(MutableCentralConfigDescriptor mutableDescriptor) {
        GcConfigDescriptor gcConfig = mutableDescriptor.getGcConfig();
        if (gcConfig != null) {
            this.garbageCollectorCron = gcConfig.getCronExp();
        }
    }

    /**
     * update clean unused cached config
     *
     * @param mutableDescriptor - config descriptor
     */
    private void updateCleanUnusedCache(MutableCentralConfigDescriptor mutableDescriptor) {
        CleanupConfigDescriptor cleanupConfig = mutableDescriptor.getCleanupConfig();
        if (cleanupConfig != null) {
            this.cleanUnusedCachedCron = cleanupConfig.getCronExp();
        }
    }

    /**
     * update virtual cache cleanup config
     *
     * @param mutableDescriptor - config descriptor
     */
    private void updateCacheCleanupConfig(MutableCentralConfigDescriptor mutableDescriptor) {
        CleanupConfigDescriptor virtualCacheCleanupConfig = mutableDescriptor.getVirtualCacheCleanupConfig();
        if (virtualCacheCleanupConfig != null) {
            this.cleanVirtualRepoCron = virtualCacheCleanupConfig.getCronExp();
        }
    }

    /**
     * update quota  config
     *
     * @param mutableDescriptor - config descriptor
     */
    private void updateQuotaConfig(MutableCentralConfigDescriptor mutableDescriptor) {
        QuotaConfigDescriptor quotaConfig = mutableDescriptor.getQuotaConfig();
        if (quotaConfig != null) {
            this.quotaControl = quotaConfig.isEnabled();
            this.storageLimit = quotaConfig.getDiskSpaceLimitPercentage();
            this.storageWarning = quotaConfig.getDiskSpaceWarningPercentage();
        } else {
            this.storageLimit = 95;
            this.storageWarning = 85;
        }
    }

    public String getGarbageCollectorCron() {
        return garbageCollectorCron;
    }

    public void setGarbageCollectorCron(String garbageCollectorCron) {
        this.garbageCollectorCron = garbageCollectorCron;
    }

    public String getCleanUnusedCachedCron() {
        return cleanUnusedCachedCron;
    }

    public void setCleanUnusedCachedCron(String cleanUnusedCachedCron) {
        this.cleanUnusedCachedCron = cleanUnusedCachedCron;
    }

    public String getCleanVirtualRepoCron() {
        return cleanVirtualRepoCron;
    }

    public void setCleanVirtualRepoCron(String cleanVirtualRepoCron) {
        this.cleanVirtualRepoCron = cleanVirtualRepoCron;
    }

    public Boolean isQuotaControl() {
        return quotaControl;
    }

    public void setQuotaControl(Boolean quotaControl) {
        this.quotaControl = quotaControl;
    }

    public Integer getStorageLimit() {
        return storageLimit;
    }

    public void setStorageLimit(Integer storageLimit) {
        this.storageLimit = storageLimit;
    }

    public Integer getStorageWarning() {
        return storageWarning;
    }

    public void setStorageWarning(Integer storageWarning) {
        this.storageWarning = storageWarning;
    }
}
