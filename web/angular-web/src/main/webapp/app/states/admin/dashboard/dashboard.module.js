function dashboardConfig($stateProvider) {
    $stateProvider
            .state('admin.dashboard', {
                url: '/dashboard',
                templateUrl: 'states/admin/dashboard/dashboard.html'
            })
}

export default angular.module('admin.dashboard', [])
        .config(dashboardConfig)