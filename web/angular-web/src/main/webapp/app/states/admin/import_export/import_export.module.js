import Repositories from './repositories/repositories.module';
import System from './system/system.module';

import {AdminImportExportController} from './import_export.controller';

function importExportConfig($stateProvider) {
    $stateProvider
            .state('admin.import_export', {
                url: '/import_export',
                template: '<ui-view></ui-view>',
                controller: 'AdminImportExportController as AdminImportExport'
            })
}

export default angular.module('admin.import_export', [
    Repositories.name,
    System.name
])
        .config(importExportConfig)
        .controller('AdminImportExportController', AdminImportExportController);

