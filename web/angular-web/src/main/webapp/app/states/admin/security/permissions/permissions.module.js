import {AdminSecurityPermissionsController} from './permissions.controller'
import {AdminSecurityPermissionsFormController} from './permissons_form.controller'

function permissionsConfig($stateProvider) {

    $stateProvider
        .state('admin.security.permissions', {
            url: '/permissions',
            templateUrl: 'states/admin/security/permissions/permissions.html',
            controller: 'AdminSecurityPermissionsController as Permissions'
        })
        .state('admin.security.permissions.edit', {
            parent: 'admin.security',
            url: '/permissions/{permission}/edit',
            templateUrl: 'states/admin/security/permissions/permission_form.html',
            controller: 'AdminSecurityPermissionsFormController as PermissionForm'
        })
        .state('admin.security.permissions.new', {
            parent: 'admin.security',
            url: '/permission/new',
            templateUrl: 'states/admin/security/permissions/permission_form.html',
            controller: 'AdminSecurityPermissionsFormController as PermissionForm'
        })
}

export default angular.module('security.permissions', [])
    .config(permissionsConfig)
    .controller('AdminSecurityPermissionsController', AdminSecurityPermissionsController)
    .controller('AdminSecurityPermissionsFormController', AdminSecurityPermissionsFormController)