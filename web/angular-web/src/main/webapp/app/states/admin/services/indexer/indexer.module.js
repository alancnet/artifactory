import {AdminServicesIndexerController} from './indexer.controller';

function indexerConfig($stateProvider) {
    $stateProvider
            .state('admin.services.indexer', {
                params: {feature: 'indexer'},
                url: '/indexer',
                templateUrl: 'states/admin/services/indexer/indexer.html',
                controller: 'AdminServicesIndexerController as AdminServicesIndexer'
            })
}

export default angular.module('indexer', [])
        .config(indexerConfig)
        .controller('AdminServicesIndexerController', AdminServicesIndexerController);