import {AdminAdvancedSystemInfoController} from './system_info.controller';


function systemInfoConfig($stateProvider) {
    $stateProvider
            .state('admin.advanced.system_info', {
                params: {feature: 'systemInfo'},
                url: '/system_info',
                templateUrl: 'states/admin/advanced/system_info/system_info.html',
                controller: 'AdminAdvancedSystemInfoController as SystemInfoController'
            })
}

export default angular.module('advanced.system_info', [])
        .config(systemInfoConfig)
        .controller('AdminAdvancedSystemInfoController', AdminAdvancedSystemInfoController);