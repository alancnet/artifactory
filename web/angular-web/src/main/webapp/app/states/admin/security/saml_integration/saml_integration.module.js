import {AdminSecuritySamlIntegrationController} from './saml_integration.controller';

function samlIntegrationConfig($stateProvider) {

    $stateProvider
            .state('admin.security.saml_integration', {
                url: '/saml_integration',
                templateUrl: 'states/admin/security/saml_integration/saml_integration.html',
                controller: 'AdminSecuritySamlIntegrationController as AdminSecuritySamlIntegration'
            })
}

export default angular.module('security.saml_integration', [])
        .config(samlIntegrationConfig)
        .controller('AdminSecuritySamlIntegrationController', AdminSecuritySamlIntegrationController);