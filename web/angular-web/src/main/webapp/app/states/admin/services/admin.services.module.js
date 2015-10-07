import Backups from './backups/backups.module';
import Indexer from './indexer/indexer.module';

import {AdminServicesController} from './services.controller';

function adminServicesConfig($stateProvider) {
    $stateProvider
            .state('admin.services', {
                url: '/services',
                template: '<ui-view></ui-view>',
                controller: 'AdminServicesController as AdminServices'
            })
}

export default angular.module('admin.services', [
    Backups.name,
    Indexer.name
])
        .config(adminServicesConfig)
        .controller('AdminServicesController', AdminServicesController)