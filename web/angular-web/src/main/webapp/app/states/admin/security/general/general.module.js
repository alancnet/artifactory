import {AdminSecurityGeneralController} from './general.controller';

function securityGeneralConfig($stateProvider) {

    $stateProvider
            .state('admin.security.general', {
                url: '/general',
                templateUrl: 'states/admin/security/general/general.html',
                controller: 'AdminSecurityGeneralController as AdminSecurityGeneral'
            })
}

export default angular.module('security.general', [])
        .config(securityGeneralConfig)
        .controller('AdminSecurityGeneralController', AdminSecurityGeneralController);