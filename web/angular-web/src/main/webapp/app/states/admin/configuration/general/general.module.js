import {AdminConfigurationGeneralController} from './general.controller';

function generalConfig($stateProvider) {

    $stateProvider
            .state('admin.configuration.general', {
                url: '/general',
                templateUrl: 'states/admin/configuration/general/general.html',
                controller: 'AdminConfigurationGeneralController as AdminConfiguration'
            })
}

export default angular.module('configuration.general', [])
    .config(generalConfig)
    .controller('AdminConfigurationGeneralController',AdminConfigurationGeneralController)
