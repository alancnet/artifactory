import {AdminAdvancedMaintenanceController} from './maintenance.controller';

function maintenanceConfig($stateProvider) {

    $stateProvider
            .state('admin.advanced.maintenance', {
                params: {feature: 'maintenance'},
                url: '/maintenance',
                templateUrl: 'states/admin/advanced/maintenance/maintenance.html',
                controller: 'AdminAdvancedMaintenanceController as Maintenance'
            })
}

export default angular.module('advanced.maintenance', [])
        .config(maintenanceConfig)
        .controller('AdminAdvancedMaintenanceController', AdminAdvancedMaintenanceController);