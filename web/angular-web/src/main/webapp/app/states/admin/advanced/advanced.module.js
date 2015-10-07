import ConfigDescriptor     from './config_descriptor/config_descriptor.module';
import Maintenance          from './maintenance/maintenance.module';
import SecurityDescriptor   from './security_descriptor/security_descriptor.module';
import StorageSummary       from './storage_summary/storage_summary.module';
import SystemInfo           from './system_info/system_info.module';
import SystemLogs           from './system_logs/system_logs.module';


import {AdminAdvancedController} from './advanced.controller';

function advancedConfig($stateProvider) {
    $stateProvider
            .state('admin.advanced', {
                url: '/advanced',
                template: '<ui-view></ui-view>',
                controller: 'AdminAdvancedController as AdminAdvanced'
            })
}

export default angular.module('admin.advanced', [
    ConfigDescriptor.name,
    Maintenance.name,
    SecurityDescriptor.name,
    StorageSummary.name,
    SystemInfo.name,
    SystemLogs.name
])
        .config(advancedConfig)
        .controller('AdminAdvancedController', AdminAdvancedController);

