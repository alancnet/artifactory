import {artifactoryNotificationsInterceptor}  from './artifactory_notifications_interceptor.js';
import {ArtifactoryDao, ArtifactoryDaoFactory}          from './artifactory_dao.js';
import {AdminSecurityGeneralDao} from './dao/admin_security_general_dao';
import {GroupsDao}               from './dao/groups_dao';
import {GroupPermissionsDao}     from './dao/group_permissions_dao';
import {HttpSsoDao}              from './dao/http_sso_dao';
import {LicensesDao}             from './dao/licenses_dao';
import {MailDao}                 from './dao/mail_dao';
import {PasswordsEncryptionDao}  from './dao/passwords_encryption_dao';
import {ProxiesDao}              from './dao/proxies_dao';
import {RegisterProDao}          from './dao/register_pro_dao';
import {UserDao}                 from './dao/user_dao';
import {SamlDao}                 from './dao/saml_dao';
import {BintrayDao}              from './dao/bintray_dao';
import {BlackDuckDao}            from './dao/black_duck_dao';
import {PropertySetsDao, PropertyFactory, PropertySetFactory}         from './dao/property_sets_dao';
import {BackupDao}               from './dao/backup_dao';
import {BrowseFilesDao}          from './dao/browse_files_dao';
import {SystemInfoDao}           from './dao/system_info_dao';
import {SecurityDescriptorDao}   from './dao/security_descriptor_dao';
import {ConfigDescriptorDao}     from './dao/config_descriptor_dao';
import {IndexerDao}              from './dao/indexer_dao';
import {StorageSummaryDao}       from './dao/storage_summary_dao'
import {TreeBrowserDao}          from './dao/tree_browser_dao';
import {TreeNodeFactory}         from './dao/tree_node';
import {RepoDataDao}             from './dao/repo_data_dao';
import {CronTimeDao}             from './dao/cron_time_dao';
import {DateFormatDao}           from './dao/date_format_dao';
import {NameValidatorDao}        from './dao/name_validator_dao';
import {UniqueIdValidatorDao}    from './dao/unique_id_validator_dao';
import {XmlNameDao}              from './dao/xml_name_dao';
import {ArtifactGeneralDao}      from './dao/artifact/artifact_general_dao';
import {ArtifactPermissionsDao}  from './dao/artifact/artifact_permissions_dao';
import {ArtifactBuildsDao}       from './dao/artifact/artifact_builds_dao';
import {ArtifactViewSourceDao}   from './dao/artifact/artifact_viewsource_dao';
import {ArtifactPropertyDao}     from './dao/artifact/artifact_property_dao';
import {ArtifactWatchesDao}      from './dao/artifact/artifact_watches_dao';
import {ArtifactActionsDao}      from './dao/artifact/artifact_actions_dao';
import {PredefineDao}            from './dao/predefine_values_dao';
import {RepoPropertySetDao}      from './dao/repo_property_set_dao';
import {ArtifactSearchDao}       from './dao/artifact/artifact_search_dao';
import {DependencyDeclarationDao}from './dao/dependency_declaration_dao';
import {ArtifactDeployDao}       from './dao/artifact/artifact_deploy_dao';
import {ArtifactViewsDao}        from './dao/artifact/artifact_views_dao';
import {FooterDao}               from './dao/footer_dao';
import {CrowdIntegrationDao}     from './dao/crowd_integration_dao';
import {ExportDao}               from './dao/export_dao';
import {ImportDao}               from './dao/import_dao';
import {MaintenanceDao}          from './dao/maintenance_dao';
import {LdapDao}                 from './dao/ldap_dao';
import {LdapGroupsDao}           from './dao/ldap_groups_dao';
import {PushToBintrayDao}        from './dao/push_to_bintray_dao';
import {GeneralConfigDao}        from './dao/general_config_dao';
import {GovernanceDao}           from './dao/governance_dao';
import {SigningKeysDao}          from './dao/signing_keys_dao';
import {KeystoreDao}             from './dao/keystore_dao';
import {BuildsDao}               from './dao/builds/builds_dao';
import {UserProfileDao}          from './dao/user_profile_dao';
import {PermissionsDao}          from './dao/permissions_dao';
import {ChecksumsDao}            from './dao/checksums_dao';
import {FilteredResourceDao}     from './dao/filtered_resource_dao';
import {ArtifactLicensesDao}     from './dao/artifact/artifact_licenses_dao';
import {HomePageDao}             from './dao/home_page_dao';
import {SetMeUpDao}              from './dao/set_me_up_dao';
import {SystemLogsDao}           from './dao/system_logs_dao';
import {RepositoriesDao}         from './dao/repositories_dao';
import {RepositoriesLayoutsDao}  from './dao/repositories_layouts_dao';
import {HaDao}                   from './dao/ha_dao';
import {StashResultsDao}       from './dao/stash_results_dao';

angular.module('artifactory.dao', ['ngResource', 'artifactory.services'])
    .factory('artifactoryNotificationsInterceptor', artifactoryNotificationsInterceptor)
    .service('ArtifactoryDao', ArtifactoryDao)
    .factory('ArtifactoryDaoFactory', ArtifactoryDaoFactory)
    .service('AdminSecurityGeneralDao', AdminSecurityGeneralDao)
    .service('GroupsDao', GroupsDao)
    .service('GroupPermissionsDao', GroupPermissionsDao)
    .service('HttpSsoDao', HttpSsoDao)
    .service('LicensesDao', LicensesDao)
    .service('MailDao', MailDao)
    .service('PasswordsEncryptionDao', PasswordsEncryptionDao)
    .factory('ProxiesDao', ProxiesDao)
    .service('RegisterProDao', RegisterProDao)
    .service('UserDao', UserDao)
    .service('SamlDao', SamlDao)
    .service('BintrayDao', BintrayDao)
    .service('BlackDuckDao', BlackDuckDao)
    .factory('PropertySetsDao', PropertySetsDao)
    .factory('Property', PropertyFactory)
    .factory('PropertySet', PropertySetFactory)
    .factory('BackupDao', BackupDao)
    .service('BrowseFilesDao', BrowseFilesDao)
    .service('SystemInfoDao', SystemInfoDao)
    .service('SecurityDescriptorDao', SecurityDescriptorDao)
    .service('ConfigDescriptorDao', ConfigDescriptorDao)
    .service('IndexerDao', IndexerDao)
    .service('StorageSummaryDao', StorageSummaryDao)
    .service('TreeBrowserDao', TreeBrowserDao)
    .factory('TreeNode', TreeNodeFactory)
    .service('RepoDataDao', RepoDataDao)
    .service('CronTimeDao', CronTimeDao)
    .service('DateFormatDao', DateFormatDao)
    .service('NameValidatorDao', NameValidatorDao)
    .service('UniqueIdValidatorDao', UniqueIdValidatorDao)
    .service('XmlNameDao', XmlNameDao)
    .service('ArtifactGeneralDao', ArtifactGeneralDao)
    .service('ArtifactPermissionsDao', ArtifactPermissionsDao)
    .service('ArtifactBuildsDao', ArtifactBuildsDao)
    .service('ArtifactViewSourceDao', ArtifactViewSourceDao)
    .service('ArtifactPropertyDao', ArtifactPropertyDao)
    .factory('ArtifactWatchesDao', ArtifactWatchesDao)
    .factory('ArtifactActionsDao', ArtifactActionsDao)
    .service('PredefineDao', PredefineDao)
    .service('RepoPropertySetDao', RepoPropertySetDao)
    .service('ArtifactSearchDao', ArtifactSearchDao)
    .service('DependencyDeclarationDao', DependencyDeclarationDao)
    .service('ArtifactDeployDao', ArtifactDeployDao)
    .service('ArtifactViewsDao', ArtifactViewsDao)
    .service('FooterDao', FooterDao)
    .service('CrowdIntegrationDao', CrowdIntegrationDao)
    .service('ExportDao', ExportDao)
    .service('ImportDao', ImportDao)
    .service('MaintenanceDao', MaintenanceDao)
    .service('LdapDao', LdapDao)
    .service('LdapGroupsDao', LdapGroupsDao)
    .service('PushToBintrayDao', PushToBintrayDao)
    .service('GeneralConfigDao', GeneralConfigDao)
    .service('GovernanceDao', GovernanceDao)
    .service('SigningKeysDao', SigningKeysDao)
    .service('KeystoreDao', KeystoreDao)
    .service('BuildsDao', BuildsDao)
    .service('UserProfileDao', UserProfileDao)
    .service('PermissionsDao', PermissionsDao)
    .service('ChecksumsDao', ChecksumsDao)
    .service('FilteredResourceDao', FilteredResourceDao)
    .service('ArtifactLicensesDao', ArtifactLicensesDao)
    .service('HomePageDao', HomePageDao)
    .service('SetMeUpDao', SetMeUpDao)
    .service('SystemLogsDao', SystemLogsDao)
    .service('RepositoriesDao', RepositoriesDao)
        .service('RepositoriesLayoutsDao', RepositoriesLayoutsDao)
        .service('StashResultsDao', StashResultsDao)
        .factory('HaDao', HaDao);
