import {ForgotPasswordController} from './forgot_password.controller.js';
import {ResetPasswordController} from './reset_password.controller.js';

function forgotPasswordConfig ($stateProvider) {
    $stateProvider

            .state('forgot-password', {
                url: '/forgot-password',
                templateUrl: 'states/forgot_password/forgot_password.html',
                controller: 'ForgotPasswordController as ForgotPassword',
                parent: 'login-layout'
            })

            .state('reset-password', {
                url: '/resetpassword?key',
                templateUrl: 'states/forgot_password/reset_password.html',
                controller: 'ResetPasswordController as ResetPassword',
                parent: 'login-layout'
            })
}

export default angular.module('forgotPassword', [])
        .config(forgotPasswordConfig)
        .controller('ForgotPasswordController', ForgotPasswordController)
        .controller('ResetPasswordController', ResetPasswordController);