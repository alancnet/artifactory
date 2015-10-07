import {AdminAdvancedSystemLogsController} from './system_logs.controller';

function systemLogsConifg($stateProvider) {

    $stateProvider
            .state('admin.advanced.system_logs', {
                url: '/system_logs',
                templateUrl: 'states/admin/advanced/system_logs/system_logs.html',
                controller: 'AdminAdvancedSystemLogsController as SystemLogsController'
            })
}

export default angular.module('advanced.system_logs', [])
        .config(systemLogsConifg)
        .controller('AdminAdvancedSystemLogsController', AdminAdvancedSystemLogsController)