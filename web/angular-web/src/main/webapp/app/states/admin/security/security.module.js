import General           from './general/general.module';
import Groups            from './groups/groups.module';
import HttpSso           from './http_sso/http_sso.module';
import Permissions       from './permissions/permissions.module';
import Users             from './users/users.module';
import Saml              from './saml_integration/saml_integration.module';
import CrowdIntegration  from './crowd_integration/crowd_integration.module';
import LdapSettings      from './ldap_settings/ldap_settings.module';
import SigningKeys       from './signing_keys/signing_keys.module';

import {AdminSecurityController} from './security.controller';

function securityConfig($stateProvider) {
    $stateProvider
            .state('admin.security', {
                url: '/security',
                template: '<ui-view></ui-view>',
                controller: 'AdminSecurityController as AdminSecurity'
            })
}


export default angular.module('admin.security', [
    General.name,
    Groups.name,
    HttpSso.name,
    Permissions.name,
    Users.name,
    Saml.name,
    CrowdIntegration.name,
    LdapSettings.name,
    SigningKeys.name
])
        .config(securityConfig)
        .controller('AdminSecurityController', AdminSecurityController);