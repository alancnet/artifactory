import {BaseController} from './base.controller';

function baseConfig($stateProvider) {
	$stateProvider
		// Base state for all application states
        .state('app-layout', {
            url: '',
            abstract: true,
            templateUrl: 'layouts/application.html',
            controller: 'BaseController as Base'
        })
		// Base state for all login related states (login, forgot password, etc.)
        .state('login-layout', {
            url: '',
            abstract: true,
            templateUrl: 'layouts/login.html'
        })

}

export default angular.module('base.module', [])
		.config(baseConfig)
        .controller('BaseController', BaseController)
