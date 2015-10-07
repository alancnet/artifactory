import {AdminConfigurationHAController} from './ha.controller';

function haConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.ha', {
                params: {feature: 'highAvailability'},
                url: '/ha',
                templateUrl: 'states/admin/configuration/ha/ha.html',
                controller: 'AdminConfigurationHAController as AdminConfigurationHA'
            })
}

export default angular.module('configuration.ha', [])
        .config(haConfig)
        .controller('AdminConfigurationHAController', AdminConfigurationHAController)