import {AdminAdvancedStorageSummaryController} from './storage_summary.controller';

function storageSummaryConfig($stateProvider) {

    $stateProvider
            .state('admin.advanced.storage_summary', {
                url: '/storage_summary',
                templateUrl: 'states/admin/advanced/storage_summary/storage_summary.html',
                controller: 'AdminAdvancedStorageSummaryController as StorageSummaryController'
            })
}

export default angular.module('advanced.storage_summary', [])
        .config(storageSummaryConfig)
        .controller('AdminAdvancedStorageSummaryController', AdminAdvancedStorageSummaryController)