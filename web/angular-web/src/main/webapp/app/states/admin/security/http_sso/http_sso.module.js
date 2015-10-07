import {AdminSecurityHttpSSoController} from './http_sso.controller';

function httpSsoConfig($stateProvider) {

    $stateProvider
            .state('admin.security.http_sso', {
                params: {feature: 'HTTPSSO'},
                url: '/http_sso',
                templateUrl: 'states/admin/security/http_sso/http_sso.html',
                controller: 'AdminSecurityHttpSSoController as AdminSecurityHttpSSo'
            })
}

export default angular.module('security.http_sso', [])
        .config(httpSsoConfig)
        .controller('AdminSecurityHttpSSoController', AdminSecurityHttpSSoController);