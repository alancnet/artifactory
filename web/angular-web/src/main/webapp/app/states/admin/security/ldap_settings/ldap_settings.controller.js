export class LdapSettingsController {
    /*
     z-index: 999;
     background-color: red;
     height: 100%;
     width: 97%;
     opacity: tr;
     position: absolute;
     */


    constructor(ArtifactoryModal, ArtifactoryFeatures, LdapDao, LdapGroupsDao, $timeout, $scope, ArtifactoryGridFactory,
            uiGridConstants) {
        this.ldapDao = LdapDao;
        this.ldapGroupsDao = LdapGroupsDao;
        this.modal = ArtifactoryModal;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.settingsGridOption = {};
        this.uiGridConstants = uiGridConstants;
        this.groupsGridOption = {};
        this.ldapSettingsData = {};
        this.ldapGroupsData = {};
        this.$timeout = $timeout;
        this.artifactoryFeatures = ArtifactoryFeatures;
        this.$scope = $scope;
        this._initLdap();

    }

    resetLdapSettings() {
        this._getLdapSettingsView();
    }

    //Call REST API for deletion of ldap settings
    deleteLdapSetting(key) {
        this.modal.confirm("Are you sure you want to delete LDAP: '" + key + "'?")
                .then(() => this._doDeleteLdapSetting(key));
    }

    _doDeleteLdapSetting(key) {
        this.ldapDao.delete({key: key}).$promise.then((data) => {
            this._getLdapSettingsView();
        })
    }

    /***************************
     ** Delete
     ****************************/

    //Call REST API for deletion of ldap group
    deleteLdapGroup(name) {
        this.modal.confirm("Are you sure you want to delete LDAP group: '" + name + "'?")
                .then(() => this._doDeleteLdapGroup(name));
    }

    _doDeleteLdapGroup(name) {
        this.ldapGroupsDao.delete({name: name}).$promise.then((data) => {
            this._getLdapGroupsView();
        })
    }

    deleteSelectedLdapSettings() {
        let selectedRows = this.settingsGridOption.api.selection.getSelectedGridRows();
        this.modal.confirm(`Are you sure you want to delete ${selectedRows.length} LDAP settings?`)
                .then(() => {
                    selectedRows.forEach((row) => this._doDeleteLdapSetting(row.entity.key));
                });
    }

    deleteSelectedLdapGroups() {
        let selectedRows = this.groupsGridOption.api.selection.getSelectedGridRows();
        this.modal.confirm(`Are you sure you want to delete ${selectedRows.length} LDAP groups?`)
                .then(() => {
                    selectedRows.forEach((row) => this._doDeleteLdapGroup(row.entity.name));
                });
    }

    /***************************
     ** Move up / down
     ****************************/
    //check if a row (in the settings grid) can move up or down
    canMove(key, dir) {
        var data = this.ldapSettingsData;
        var index = this._indexOf(key);
        return ((dir === 'up' && index > 0) || (dir === 'down' && index < data.length - 1));
    }

    //move a row (in the settings grid) up or down
    moveLdapSetting(key, dir) {
        var data = this.ldapSettingsData;
        var index = this._indexOf(key);
        if (dir === 'up' && index > 0) {
            this._swapSettings(index, index - 1);
        }
        else if (dir === 'down' && index < data.length - 1) {
            this._swapSettings(index, index + 1);
        }
    }

    /***************************
     ** Initialization
     ****************************/

    //initialize everything...
    _initLdap() {

        this._createGrids();
        this._getLdapSettingsView();
        this._getLdapGroupsView().then(()=>this._disableAsOss());

    }

    //get settings ('view') data from the REST API
    _getLdapSettingsView() {
        this.ldapDao.query().$promise.then((data) => {
            this.ldapSettingsData = data;
            this.settingsGridOption.setGridData(this.ldapSettingsData);
        })
    }

    //get ldap groups ('view') data from the REST API
    _getLdapGroupsView() {
        return this.ldapGroupsDao.query().$promise.then((data) => {
            this.ldapGroupsData = data;
            this.groupsGridOption.setGridData(this.ldapGroupsData);
        })
    }

    _disableAsOss() {

    }

    //create both ldap settings grid and ldap groups grid
    _createGrids() {
        this.settingsGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getSettingsColumns())
            //.setMultiSelect(this.)
                .setDraggable(this.reorderLdap.bind(this))
                .setButtons(this._getSettingsActions());
        //.setBatchActions(this._getSettingsBatchActions());

        this.groupsGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getGroupsColumns())
            //.setMultiSelect()
                .setRowTemplate('default')
                .setButtons(this._getGroupsActions());
        //.setBatchActions(this._getGroupsBatchActions());
    }

    reorderLdap() {
        return this.ldapDao.reorder(this.getLdapOrder()).$promise;
    }

    getLdapOrder() {
        let ldapOrderList = [];
        this.settingsGridOption.data.forEach((data)=> {
            ldapOrderList.push(data.key);
        });
        return ldapOrderList;
    }


    /***************************
     ** Settings Grid
     ****************************/
    //get the columns for the settings grid
    _getSettingsColumns() {

        return [
            {
                name: "Settings Name",
                displayName: "Settings Name",
                field: "key",
                cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="^.ldap_settings.edit({ldapSettingKey: row.entity.key})">{{ COL_FIELD }}</a></div>',
                enableSorting: false
            },
            {
                name: "LDAP URL",
                displayName: "LDAP URL",
                field: "ldapUrl",
                cellTemplate: '<div class="ui-grid-cell-contents"><a ng-href="{{ COL_FIELD }}">{{ COL_FIELD }}</a></div>',
                enableSorting: false
            }

        ]
    }

    //get the actions for the settings grid
    _getSettingsActions() {
        return [
            /*{
             icon: 'icon icon-angle-double-up',
             tooltip: 'Move Up',
             visibleWhen: row => this.canMove(row.key,'up'),
             callback: row => this.moveLdapSetting(row.key,'up')
             },
             {
             icon: 'icon icon-angle-double-down',
             tooltip: 'Move Down',
             visibleWhen: row => this.canMove(row.key,'down'),
             callback: row => this.moveLdapSetting(row.key,'down')
             },*/
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: row => this.deleteLdapSetting(row.key)
            }

        ];
    }

    //
    //_getSettingsBatchActions() {
    //    return [
    //        {
    //            icon: 'clear',
    //            name: 'Delete',
    //            callback: () => this.deleteSelectedLdapSettings()
    //        },
    //    ]
    //}
    /***************************
     ** Groups Grid
     ****************************/
    _getGroupsColumns() {
        return [
            {
                name: "Group Name",
                displayName: "Group Name",
                field: "name",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="^.ldap_settings.edit_ldap_group({ldapGroupName: row.entity.name})">{{ COL_FIELD }}</a></div>'
            },
            {
                name: "LDAP Settings",
                displayName: "LDAP Settings",
                field: "enabledLdap"
            },
            {
                name: "Strategy",
                displayName: "Strategy",
                field: "strategy"
            }
        ]
    }

    _getGroupsActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: row => this.deleteLdapGroup(row.name)
            }

        ];
    }


    _getGroupsBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedLdapGroups()
            },
        ]
    }

    //get the index of the ldap settings (key) in this.ldapSettingsData
    _indexOf(key) {
        var data = this.ldapSettingsData;

        var index = -1;

        for (var i = 0; i < data.length; i++) {
            if (data[i].key === key) {
                index = i;
                break;
            }
        }

        return index;
    }

    //swap the order of two ldap settings
    _swapSettings(index1, index2) {
        var data = this.ldapSettingsData;

        var temp = data[index2];
        data[index2] = data[index1];
        data[index1] = temp;
    }
}
