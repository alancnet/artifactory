import EVENTS from '../../../../constants/common_events.constants.js';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminAdvancedMaintenanceController {
    constructor(MaintenanceDao, ArtifactoryNotifications, ArtifactoryEventBus, ArtifactoryModal) {
        this.maintenanceDao = MaintenanceDao;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.artifactoryModal = ArtifactoryModal;
        this.maintenanceSettings = {};
        this.TOOLTIP = TOOLTIP.admin.advanced.maintenance;

        this._getData();
    }

    _getData() {
        this.maintenanceDao.get().$promise.then(data => {
            this.backupMaintance = angular.copy(data);
            this.maintenanceSettings.cleanUnusedCachedCron = data.cleanUnusedCachedCron;
            this.maintenanceSettings.cleanVirtualRepoCron = data.cleanVirtualRepoCron;
            this.maintenanceSettings.garbageCollectorCron = data.garbageCollectorCron;
            this.maintenanceSettings.quotaControl = data.quotaControl;
            this.maintenanceSettings.storageLimit = data.storageLimit;
            this.maintenanceSettings.storageWarning = data.storageWarning;
        });
    }

    save() {
        if (this.maintenanceForm.$valid) {
            this.maintenanceDao.update(this.maintenanceSettings);
        }
    }

    clear() {
        this.artifactoryEventBus.dispatch(EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
        this._getData();
    }

    resetQuotaFields() {
        if (!this.maintenanceSettings.quotaControl) {
            this.maintenanceSettings.storageLimit = this.backupMaintance.storageLimit;
            this.maintenanceSettings.storageWarning = this.backupMaintance.storageWarning;
        }
    }

    _runAction(name) {
        this.maintenanceDao.perform({module: name});
    }

    runGarbageCollection() {
        this._runAction('garbageCollection');
    }

    runUnusedCachedArtifactsCleanup() {
        this._runAction('cleanUnusedCache');
    }

    compressInternalDatabase() {
        this.artifactoryModal.confirm('Are you sure you want to compress the internal database?')
            .then(() => this._runAction('compress'));
    }

    pruneUnreferencedData() {
        this._runAction('prune');
    }

    cleanVirtualRepositories() {
        this._runAction('cleanVirtualRepo');
    }

}