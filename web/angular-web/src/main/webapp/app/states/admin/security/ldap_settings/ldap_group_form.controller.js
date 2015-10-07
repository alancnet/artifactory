import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

// Injectables:
let $q, $scope, $state, $stateParams, ArtifactoryGridFactory, LdapGroupsDao, LdapDao;
export class LdapGroupFormController {

    constructor(_$q_, _$scope_, _$state_, _$stateParams_, _ArtifactoryGridFactory_, _LdapGroupsDao_, _LdapDao_) {
        $q = _$q_;
        $scope = _$scope_;
        $state = _$state_;
        $stateParams = _$stateParams_;
        ArtifactoryGridFactory = _ArtifactoryGridFactory_;
        LdapGroupsDao = _LdapGroupsDao_;
        LdapDao = _LdapDao_;

        this.isNew = !$stateParams.ldapGroupName;
        this._initLdapSetting();
        this._initGroupLabels();
        this._initSelectizeConfig();
        this._initGroupSyncGrid();
        this._initStrategyOptions();
        this.isImportDisabled = true;
        this.TOOLTIP = TOOLTIP.admin.security.LDAPGroupsForm;
    }

    /***************************
    ** Initialization
    ****************************/
    _initLdapSetting() {
        if (this.isNew) {
            this.ldapGroup = {
                enabledLdap: ' ',
                strategy: "STATIC",
                subTree: true
            };
            this.ldapGroupPromise = $q.when(this.ldapGroup);
        }
        else {
            this.ldapGroupPromise = LdapGroupsDao.get({name: $stateParams.ldapGroupName}).$promise
                .then((ldapGroup) => this.ldapGroup = ldapGroup);
        }
    }

    _initGroupLabels() {
        if (this.isNew) this.onStrategyChange();
        else {
            this.ldapGroupPromise.then(() => {            
                this._setGroupLabels(this.ldapGroup.strategy);
            })
        }
    }

    _initSelectizeConfig() {
        LdapDao.query().$promise.then((ldapSettingsData) => {
            this.selectizeConfig = {
                sortField: 'text',
                create: true,
                maxItems: 1
            };

            this.selectizeOptions = ldapSettingsData.map((ldapSetting) => ldapSetting.key);
            this.selectizeOptions.push(' ');
        });
    }

    _initGroupSyncGrid() {
        this.syncGroupsGridOptions = ArtifactoryGridFactory.getGridInstance($scope)
                .setColumns(this._getSyncGroupsGridColumns())
                .setRowTemplate('default')
                .setBatchActions(this._getBatchActions())
                .setMultiSelect()
                .setGridData([]);

        this.syncGroupsGridOptions.onSelectionChange =
        this.syncGroupsGridOptions.onSelectionChangeBatch = () => {
            var selectedRows = this.syncGroupsGridOptions.api.selection.getSelectedRows();
            this.isImportDisabled = !selectedRows.length;
        };
    }

    _initStrategyOptions() {
        this.strategyOptions = [
            {value: 'STATIC', text: 'Static'},
            {value: 'DYNAMIC', text: 'Dynamic'},
            {value: 'HIERARCHICAL', text: 'Hierarchy'}
        ];
    }

    save() {
        let whenSaved = this.isNew ? LdapGroupsDao.save(this.ldapGroup) : LdapGroupsDao.update(this.ldapGroup);
        whenSaved.$promise.then(() => this._end());
    }

    cancel() {
        this._end();
    }

    _end() {
        $state.go('^.ldap_settings');
    }

    _getBatchActions() {
        return [
            {
                icon: 'icon icon-import',
                name: 'Import',
                callback: () => this.importSyncGroups()
            }
        ]
    }

    importSyncGroups() {
        if (this.isImportDisabled) return;

        var importData = {};
        var selectedRows = this.syncGroupsGridOptions.api.selection.getSelectedRows();
        _.extend(importData, {importGroups: selectedRows, ldapGroupSettings: this.ldapGroup});
        _.extend(importData, {name: this.ldapGroup.name});

        LdapGroupsDao.import(importData).$promise.then((data) => {
            this.refreshSyncGroups(this.ldapGroup.usernameFilter);
        })
    }

    importSyncGroup(row) {
        var importData = {};
        _.extend(importData, {importGroups: [row], ldapGroupSettings: this.ldapGroup});
        _.extend(importData, {name: this.ldapGroup.name});

        LdapGroupsDao.import(importData).$promise.then((data) => {
            this.refreshSyncGroups(this.ldapGroup.usernameFilter);
        })
    }

    refreshSyncGroups(username) {
        if (!this.ldapGroupsEditForm.$valid) return;

        var refreshData = {};
        _.extend(refreshData,this.ldapGroup);
        _.extend(refreshData,{name: refreshData.name, username: username});

        LdapGroupsDao.refresh(refreshData).$promise.then((result) => {
            result.data.forEach((group)=>{
                group.syncState = group.requiredUpdate === 'IN_ARTIFACTORY';
            });
            this.syncGroupsGridOptions.setGridData(result.data);
        }).catch(() => {
            this.syncGroupsGridOptions.setGridData([]);
        }).finally(() => {
            if (this) this.isImportDisabled = true;
        });
    }

    /***************************
    ** Strategy
    ****************************/    
    _setGroupLabels(strategy) {
        switch(strategy.toLowerCase()) {
            case 'static':
            case 'dynamic':
                this.labels = {groupKeyMember: 'Group Member Attribute'};
                break;
            case 'hierarchical':
                this.labels = {groupKeyMember: 'User DN Group Key'};
                break;
        }
    }

    onStrategyChange() {
        var strategy = this.ldapGroup.strategy;
        this._getStrategy(strategy).then((data) => {
            this.ldapGroup.groupMemberAttribute = data.groupKeyMember;
            this.ldapGroup.groupNameAttribute = data.groupNameAttribute;
            this.ldapGroup.descriptionAttribute = data.description;
            this.ldapGroup.filter = data.filter;
        });

        this._setGroupLabels(strategy);
    }

    _getStrategy(strategy) {
        return LdapGroupsDao.getstrategy({name:'dummy', strategy: strategy.toLowerCase()}).$promise;
    }

    /***************************
    ** Grid
    ****************************/
    //get the columns for the synchronize groups grid (inside the ldap groups' modal)
    _getSyncGroupsGridColumns() {

        return [
            {
                name: "Group Name",
                displayName: "Group Name",
                field: "groupName",
                width: '30%'
            },
            {
                name: "Description",
                displayName: "Description",
                field: "description",
                width: '60%'
            },
            {
                name: "Sync State",
                displayName: "Sync State",
                field: "syncState",
                cellTemplate: '<div ng-if="row.entity.syncState" class="grid-checkbox"><input class="text-center" ' +
                'ng-model="row.entity.syncState" type="checkbox" disabled/><span class="icon icon-v"></span></div>',
                width: '10%',
                customActions: [{
                    icon: 'icon icon-import',
                    tooltip: 'Import',
                    name: 'Import',
                    callback: row => this.importSyncGroup(row)
                }]
            }
        ]
    }
}