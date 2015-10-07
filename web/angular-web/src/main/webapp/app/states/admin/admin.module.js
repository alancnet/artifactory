import Configuration     from './configuration/configuration.module';
import Repositories      from './repositories/repositories.module';
import Advanced          from './advanced/advanced.module';
import Dashboard         from './dashboard/dashboard.module';
import ImportExport      from './import_export/import_export.module';
import Security          from './security/security.module';
import Services          from './services/admin.services.module';

import {AdminController} from './admin.controller';

function adminConfig($stateProvider) {
    $stateProvider
            .state('admin', {
                url: '/admin',
                parent: 'app-layout',
                templateUrl: "states/admin/admin.html",
                controller: 'AdminController as Admin'
            })
}

export default angular.module('admin.module', [
    Configuration.name,
    Repositories.name,
    Advanced.name,
    Dashboard.name,
    ImportExport.name,
    Security.name,
    Services.name
])
        .config(adminConfig)
        .controller('AdminController', AdminController)