import {AdminSecurityUserController} from './users.controller';
import {AdminSecurityUserFormController} from './user_form.controller';

function usersConfig($stateProvider) {

    $stateProvider
        .state('admin.security.users', {
            url: '/users',
            templateUrl: 'states/admin/security/users/users.html',
            controller: 'AdminSecurityUserController as AdminSecurityUser'
        })
        .state('admin.security.users.edit', {
            parent: 'admin.security',
            url: '/users/{username}/edit',
            templateUrl: 'states/admin/security/users/user_form.html',
            controller: 'AdminSecurityUserFormController as UserForm'
        })
        .state('admin.security.users.new', {
            parent: 'admin.security',
            url: '/users/new',
            templateUrl: 'states/admin/security/users/user_form.html',
            controller: 'AdminSecurityUserFormController as UserForm'
        })

}

export default angular.module('security.users', [])
        .config(usersConfig)
    .controller('AdminSecurityUserController', AdminSecurityUserController)
    .controller('AdminSecurityUserFormController', AdminSecurityUserFormController);