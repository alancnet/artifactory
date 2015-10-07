export class AdminSecurityGroupFormController {
    constructor($scope, $state, $stateParams, ArtifactoryGridFactory, GroupsDao, UserDao, GroupPermissionsDao,
            commonGridColumns) {

        this.DEFAULT_REALM = "artifactory";
        this.$scope = $scope;
        this.$state = $state;
        this.$stateParams = $stateParams;
        this.userDao = UserDao.getInstance();
        this.groupsDao = GroupsDao.getInstance();
        this.groupPermissionsDao = GroupPermissionsDao.getInstance();
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.permissionsGridOptions = {};
        this.commonGridColumns = commonGridColumns;
        this.input = {};


        if ($stateParams.groupname) {
            this.mode = 'edit';
            this.groupname = $stateParams.groupname;
            this.title = 'Edit ' + this.groupname + ' Group';
            this._getGroupData();
            this._createGrid();
            this._getPermissions();
        }
        else {
            this.mode = 'create';
            this.title = 'Add New Group';
            this.groupdata = {};
        }

        this._getAllUsers();

    }

    _getGroupData() {
        this.groupsDao.getSingle({name: this.groupname}).$promise.then((data) => {
            //console.log(data);
            this.groupdata = data;
        });
    }

    _getAllUsers() {
        this.userDao.getAll().$promise.then((data)=> {
            this.usersData = data;
            this.usersList = _.map(data, (user)=> {
                return user.name;
            });

        });
    }


    _createGrid() {
        this.permissionsGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getPermissionCloumns())
            .setRowTemplate('default');

    }

    _getPermissions() {
        this.groupPermissionsDao.get({groups: [this.groupname]}).$promise.then((data)=> {
            //console.log(data);
            this.permissionsGridOptions.setGridData(data);
        });
    }

    updateGroup() {
        let payload = angular.copy(this.groupdata);
        _.extend(payload, this.input);
        this.groupsDao.update({name: this.groupdata.groupName}, payload).$promise.then((data) => {
            this.$state.go('^.groups');
        });
    }

    createNewGroup() {
        let payload = angular.copy(this.groupdata);
        payload.realm = this.DEFAULT_REALM;
        _.extend(payload, this.input);
        this.groupsDao.create(payload).$promise.then((data) => {
            this.$state.go('^.groups');
        });
    }

    save() {
        if (this.mode === 'edit')
            this.updateGroup();
        if (this.mode === 'create')
            this.createNewGroup();
    }

    isSaveDisabled() {
        return this.groupForm.$invalid;
    }


    cancel() {
        this.$state.go('^.groups');
    }

    _getPermissionCloumns() {

        let nameCellTemplate = '<div class="ui-grid-cell-contents"><a href ui-sref="admin.security.permissions.edit({permission: row.entity.permissionName})">{{row.entity.permissionName}}</a></div>';

        return [
            {
                field: "permissionName",
                name: "Permission Target",
                displayName: "Permission Target",
                cellTemplate: nameCellTemplate,
                width:'20%'
            },
/*
            {
                field: "effectivePermission.principal",
                displayName: "Inherited From"
            },
*/
            {
                field: "repoKeys",
                name: "Repositories",
                displayName: "Repositories",
                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.numOfRepos}} | {{row.entity.repoKeys.join(\', \')}}</div>',
                width:'25%'
            },
            {
                field: "effectivePermission.managed",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.managed'),
                name: "Manage",
                displayName: "Manage",
                width:'9%'
            },
            {
                field: "effectivePermission.delete",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.delete'),
                name: "Delete/Overwrite",
                displayName: "Delete/Overwrite",
                width:'15%'
            },
            {
                field: "effectivePermission.deploy",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.deploy'),
                name: "Deploy/Cache",
                displayName: "Deploy/Cache",
                width:'14%'
            },
            {
                field: "effectivePermission.annotate",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.annotate'),
                name: "Annotate",
                displayName: "Annotate",
                width:'9%'
            },
            {
                field: "effectivePermission.read",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.read'),
                name: "Read",
                displayName: "Read",
                width:'8%'
            }
        ]
    }
}