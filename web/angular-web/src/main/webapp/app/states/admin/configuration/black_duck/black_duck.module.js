import {AdminConfigurationBlack_duckController} from './black_duck.controller'

function blackDuckConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.black_duck', {
                url: '/black_duck',
                templateUrl: 'states/admin/configuration/black_duck/black_duck.html',
                controller: 'AdminConfigurationBlack_duckController as BlackDuck'
            })
}


export default angular.module('configuration.black_duck', [])
        .config(blackDuckConfig)
        .controller("AdminConfigurationBlack_duckController", AdminConfigurationBlack_duckController);