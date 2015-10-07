import {CrowdIntegrationController} from './crowd_integration.controller';

function crowdIntegrationConfig($stateProvider) {

    $stateProvider
            .state('admin.security.crowd_integration', {
                url: '/crowd_integration',
                templateUrl: 'states/admin/security/crowd_integration/crowd_integration.html',
                controller: 'CrowdIntegrationController as Crowd'
            })
}

export default angular.module('security.crowd_integration', [])
        .config(crowdIntegrationConfig)
        .controller('CrowdIntegrationController', CrowdIntegrationController);