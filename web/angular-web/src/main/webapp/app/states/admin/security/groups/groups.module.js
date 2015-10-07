import {AdminSecurityGroupsController} from './group.controller';
import {AdminSecurityGroupFormController} from './group_form.controller';

function groupsConfig($stateProvider) {

    $stateProvider
        .state('admin.security.groups', {
            url: '/groups',
            templateUrl: 'states/admin/security/groups/groups.html',
            controller: 'AdminSecurityGroupsController as AdminSecurityGroups'
        })
        .state('admin.security.groups.edit', {
            parent: 'admin.security',
            url: '/groups/{groupname}/edit',
            templateUrl: 'states/admin/security/groups/group_form.html',
            controller: 'AdminSecurityGroupFormController as GroupForm'
        })
        .state('admin.security.groups.new', {
            parent: 'admin.security',
            url: '/groups/new',
            templateUrl: 'states/admin/security/groups/group_form.html',
            controller: 'AdminSecurityGroupFormController as GroupForm'
        })

}

export default angular.module('security.groups', [])
        .config(groupsConfig)
        .controller('AdminSecurityGroupsController', AdminSecurityGroupsController)
    .controller('AdminSecurityGroupFormController', AdminSecurityGroupFormController)