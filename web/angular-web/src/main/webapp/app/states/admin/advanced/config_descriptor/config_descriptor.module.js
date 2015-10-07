import {AdminAdvancedConfigDescriptorController} from './config_descriptor.controller';

function configDescriptorConfig($stateProvider) {

    $stateProvider
            .state('admin.advanced.config_descriptor', {
                params: {feature: 'configDescriptor'},
                url: '/config_descriptor',
                templateUrl: 'states/admin/advanced/config_descriptor/config_descriptor.html',
                controller: 'AdminAdvancedConfigDescriptorController as ConfigDescriptorController'
            })
}

export default angular.module('advanced.config_descriptor', [])
        .config(configDescriptorConfig)
        .controller('AdminAdvancedConfigDescriptorController', AdminAdvancedConfigDescriptorController);