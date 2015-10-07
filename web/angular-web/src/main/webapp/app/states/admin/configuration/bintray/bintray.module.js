import {AdminConfigurationBintrayController} from './bintray.controller';

function bintrayConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.bintray', {
                url: '/bintray',
                templateUrl: 'states/admin/configuration/bintray/bintray.html',
                controller: 'AdminConfigurationBintrayController as AdminConfigurationBintray'
            })
}

export default angular.module('configuration.bintray', [])
        .config(bintrayConfig)
        .controller('AdminConfigurationBintrayController', AdminConfigurationBintrayController);