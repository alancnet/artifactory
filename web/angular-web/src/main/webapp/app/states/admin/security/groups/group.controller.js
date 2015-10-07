export class AdminSecurityGroupsController {

    constructor(ArtifactoryModal, $scope, $state, GroupsDao, ArtifactoryGridFactory, uiGridConstants, commonGridColumns) {
        this.DEFAULT_REALM = "artifactory";
        this.gridOption = {};
        this.uiGridConstants = uiGridConstants;
        this.commonGridColumns = commonGridColumns;
        this.groupsDao = GroupsDao.getInstance();
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.modal = ArtifactoryModal;
        this.$scope = $scope;
        this.$state = $state;
        this._createGrid();
        this._initGroups();
    }

    _initGroups() {
        this.groupsDao.getAll().$promise.then((groups)=> {
            this.gridOption.setGridData(groups);
        });
    }

    _createGrid() {
        this.gridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getColumns())
                .setButtons(this._getActions())
                .setMultiSelect()
                .setRowTemplate('default')
                .setBatchActions(this._getBatchActions());
    }

    deleteGroup(group) {
        let json = {groupNames:[group.groupName]}
        this.modal.confirm(`Are you sure you want to delete group '${group.name}?'`)
            .then(() => this.groupsDao.delete(json).$promise.then(()=>this._initGroups()));
    }

    bulkDelete() {
        //Get All selected users
        let selectedRows = this.gridOption.api.selection.getSelectedRows();
        //Create an array of the selected groups names
        let names = _.map(selectedRows, (group) => {return group.groupName;});
        //Create Json for the bulk request
        let json = {groupNames: names};
        //console.log('Bulk delete....');
        //Ask for confirmation before delete and if confirmed then delete bulk of users
        this.modal.confirm(`Are you sure you want to delete ${names.length} groups ?`).
        then(() => this.groupsDao.delete(json).$promise.then(() => this._initGroups()));
    }
    getColumns() {
        return [
            {
                field: "groupName",
                name: "Group Name",
                displayName: "Group Name",
                cellTemplate: '<div class="ui-grid-cell-contents" ui-sref="^.groups.edit({groupname: row.entity.groupName})"><a href="">{{row.entity.groupName}}</a></div>',
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                width: '20%'
            },
            {
                field: "permissions",
                name: "Permissions",
                displayName: "Permissions",
                cellTemplate: '<div ng-if="row.entity.permissions && row.entity.permissions.length" class="ui-grid-cell-contents">{{row.entity.permissions.length}} | {{row.entity.permissions.join(\', \')}}</div>' +
                '<div ng-if="!row.entity.permissions || !row.entity.permissionsList.length" class="ui-grid-cell-contents">-</div>',
                width: '60%',
                cellClass: 'tooltip-show-list'
            },
            {
                name: "External",
                displayName: "External",
                field: "External",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.external'),
                width: '10%'
            },

            {
                name: "Auto Join",
                displayName: "Auto Join",
                field: "Auto Join",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.autoJoin'),
                width: '10%'
            }
        ]
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: (row) => this.deleteGroup(row)
            }

        ];
    }
   _getBatchActions() {
        return [
            {
                icon: 'icon icon-clear',
                name: 'Delete',
                callback: () => this.bulkDelete()
            }
        ]
    }

}