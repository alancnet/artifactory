import {LoginRequestController} from './login_request.controller';

function loginRequestCOnfig ($stateProvider) {
    $stateProvider

            .state('login_request', {
                url: '/oauth2/loginRequest',
                templateUrl: 'states/oauth/login_request/login_request.html',
                controller: 'LoginRequestController as LoginRequest',
                parent: 'login-layout'
            })
}

export default angular.module('login_request', [])
        .config(loginRequestCOnfig)
        .controller('LoginRequestController', LoginRequestController);
