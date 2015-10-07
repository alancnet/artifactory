import {AdminServicesBackupsController} from './backups.controller';
import {AdminServicesBackupFormController} from './backup_form.controller';

function backupsConfig($stateProvider) {

    $stateProvider
            .state('admin.services.backups', {
                params: {feature: 'backups'},
                url: '/backups',
                templateUrl: 'states/admin/services/backups/backups.html',
                controller: 'AdminServicesBackupsController as AdminServicesBackups'
            })
            .state('admin.services.backups.new', {
                params: {feature: 'backups'},
                parent: 'admin.services',
                url: '/backups/new',
                templateUrl: 'states/admin/services/backups/backup_form.html',
                controller: 'AdminServicesBackupFormController as BackupForm'
            })
            .state('admin.services.backups.edit', {
                params: {feature: 'backups'},
                parent: 'admin.services',
                url: '/backups/:backupKey/edit',
                templateUrl: 'states/admin/services/backups/backup_form.html',
                controller: 'AdminServicesBackupFormController as BackupForm'
            })
}

export default angular.module('backups', [])
        .config(backupsConfig)
        .controller('AdminServicesBackupsController', AdminServicesBackupsController)
        .controller('AdminServicesBackupFormController', AdminServicesBackupFormController)