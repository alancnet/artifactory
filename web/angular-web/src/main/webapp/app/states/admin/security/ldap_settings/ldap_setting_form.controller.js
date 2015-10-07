import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

let $stateParams, LdapDao, $state, ArtifactoryGridFactory;

export class LdapSettingFormController {
    constructor(_$stateParams_, _$state_, _LdapDao_, _ArtifactoryGridFactory_, ArtifactoryNotifications) {
        $state = _$state_;
        $stateParams = _$stateParams_;
        LdapDao = _LdapDao_;
        ArtifactoryGridFactory = _ArtifactoryGridFactory_;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.testConnection = {};
        this.isNew = !$stateParams.ldapSettingKey;
        this.TOOLTIP = TOOLTIP.admin.security.LDAPSettingsForm;
        this._initLdapSetting();
    }

    _initLdapSetting() {
        if (this.isNew) {
            this.ldap = {
                enabled: true,
                autoCreateUser: true,
                search: {searchSubTree: true},
                emailAttribute: 'mail'
            };
        }
        else {
            LdapDao.get({key: $stateParams.ldapSettingKey}).$promise
                    .then((ldapSetting) => this.ldap = ldapSetting);
        }
    }

    save() {
        if (this.ldapEditForm.$valid) {
            if (!this.ldap.userDnPattern && !this.ldap.search.searchFilter) {
                this.messageUserOrSearch();
            }
            else {
                if (this.ldap.search && this._isSearchFieldsNull(this.ldap.search)) {
                    this.ldap.search = undefined;
                }
                let whenSaved = this.isNew ? LdapDao.save(this.ldap) : LdapDao.update(this.ldap);
                whenSaved.$promise.then(() => this._end());
            }
        }
    }

    _isSearchFieldsNull(search) {
        return (!search.managerDn && !search.managerPassword && !search.searchBase && !search.searchFilter);
    }

    cancel() {
        this._end();
    }

    _end() {
        $state.go('^.ldap_settings');
    }

    doTestConnection() {
        if (this.ldapEditForm.$valid) {
            if (!this.ldap.userDnPattern && !this.ldap.search.searchFilter) {
                this.messageUserOrSearch();
            }
            else {
                var testData = {};
                _.extend(testData, this.ldap);
                _.extend(testData, this.testConnection);

                LdapDao.test(testData);
            }
        }
    }

    messageUserOrSearch() {
        this.artifactoryNotifications.create({error: 'LDAP settings should provide a userDnPattern or a searchFilter (or both)'});
    }
}