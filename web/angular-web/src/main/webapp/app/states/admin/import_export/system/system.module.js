import {AdminImportExportSystemController} from './system.controller';

function systemConfig($stateProvider) {
    $stateProvider
            .state('admin.import_export.system', {
                url: '/system',
                templateUrl: 'states/admin/import_export/system/system.html',
                controller: 'AdminImportExportSystemController as SystemController'
            })
}

export default angular.module('import_export.system', [])
        .config(systemConfig)
        .controller('AdminImportExportSystemController', AdminImportExportSystemController)