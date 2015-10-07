import {AdminConfigurationRegisterController} from './register_pro.controller';

function registerProConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.register_pro', {
                params: {feature: 'register_pro'},
                url: '/register_pro',
                templateUrl: 'states/admin/configuration/register_pro/register_pro.html',
                controller: 'AdminConfigurationRegisterController as AdminConfigurationRegister'
            })
}

export default angular.module('configuration.register_pro', [])
        .config(registerProConfig)
        .controller('AdminConfigurationRegisterController', AdminConfigurationRegisterController);