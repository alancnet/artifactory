function config ($stateProvider) {

    $stateProvider
            .state('server_error', {
                templateUrl: 'states/server_error/server_error.html'
            })
}

export default angular.module('server_error', [])
        .config(config)