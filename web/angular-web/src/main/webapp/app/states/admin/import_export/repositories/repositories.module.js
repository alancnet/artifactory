import {ImportExportRepositoriesController} from './repositories.controller';


function repositoriesConfig($stateProvider) {
    $stateProvider
            .state('admin.import_export.repositories', {
                params: {feature: 'repositories'},
                url: '/repositories',
                templateUrl: 'states/admin/import_export/repositories/repositories.html',
                controller: 'ImportExportRepositoriesController as Repositories'
            })
}

export default angular.module('import_export.repositories', [])
        .config(repositoriesConfig)
        .controller('ImportExportRepositoriesController', ImportExportRepositoriesController)

