import {LdapSettingsController} from './ldap_settings.controller.js'
import {LdapSettingFormController} from './ldap_setting_form.controller.js'
import {LdapGroupFormController} from './ldap_group_form.controller.js'

function ldapSettingConfig($stateProvider) {

    $stateProvider
            .state('admin.security.ldap_settings', {
                url: '/ldap_settings',
                templateUrl: 'states/admin/security/ldap_settings/ldap_settings.html',
                controller: 'LdapSettingsController as Ldap'
            })
            .state('admin.security.ldap_settings.new', {
                parent: 'admin.security',
                url: '/ldap_settings/new',
                templateUrl: 'states/admin/security/ldap_settings/ldap_setting_form.html',
                controller: 'LdapSettingFormController as LdapSettingForm'
            })
            .state('admin.security.ldap_settings.edit', {
                parent: 'admin.security',
                url: '/ldap_settings/:ldapSettingKey/edit',
                templateUrl: 'states/admin/security/ldap_settings/ldap_setting_form.html',
                controller: 'LdapSettingFormController as LdapSettingForm'
            })
            .state('admin.security.ldap_settings.new_ldap_group', {
                parent: 'admin.security',
                url: '/ldap_groups/new',
                templateUrl: 'states/admin/security/ldap_settings/ldap_group_form.html',
                controller: 'LdapGroupFormController as LdapGroupForm'
            })
            .state('admin.security.ldap_settings.edit_ldap_group', {
                parent: 'admin.security',
                url: '/ldap_groups/:ldapGroupName/edit',
                templateUrl: 'states/admin/security/ldap_settings/ldap_group_form.html',
                controller: 'LdapGroupFormController as LdapGroupForm'
            })
}

export default angular.module('security.ldap_settings', [])
        .config(ldapSettingConfig)
        .controller('LdapSettingsController', LdapSettingsController)
        .controller('LdapSettingFormController', LdapSettingFormController)
        .controller('LdapGroupFormController', LdapGroupFormController);