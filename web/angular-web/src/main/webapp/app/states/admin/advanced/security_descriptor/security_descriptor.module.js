import {AdminAdvancedSecurityDescriptorController} from './security_descriptor.controller';

function securityDescriptorConfig($stateProvider) {

    $stateProvider
            .state('admin.advanced.security_descriptor', {
                params: {feature: 'securityDescriptor'},
                url: '/security_descriptor',
                templateUrl: 'states/admin/advanced/security_descriptor/security_descriptor.html',
                controller: 'AdminAdvancedSecurityDescriptorController as SecurityDescriptorController'
            })
}

export default  angular.module('advanced.security_descriptor', [])
        .config(securityDescriptorConfig)
        .controller('AdminAdvancedSecurityDescriptorController', AdminAdvancedSecurityDescriptorController)