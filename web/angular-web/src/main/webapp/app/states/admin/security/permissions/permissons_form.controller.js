import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminSecurityPermissionsFormController {
    constructor($scope, $state, $stateParams, $q, ArtifactoryModal, ArtifactoryGridFactory, RepoDataDao,
            PermissionsDao, commonGridColumns, User) {
        this.$scope = $scope;
        this.$q = $q;
        this.repoDataDao = RepoDataDao;
        this.commonGridColumns = commonGridColumns;
        this.user = User.getCurrent();
        this.modal = ArtifactoryModal;
        this.currentTab = 'repo';
        this.$state = $state;
        this.title = "New Permission";
        this.$stateParams = $stateParams;
        this.permission = {};
        this.permissionsDao = PermissionsDao.getInstance();
        this.newPermission = false;
        this.groupsGrid = [];
        this.usersGrid = [];
        this.selectedItems = [];
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.groupsGridOption = {};
        this.usersGridOption = {};
        this.TOOLTIP = TOOLTIP.admin.security.permissionsForm;
        this._createGroupsGrid();
        this._createUsersGrid();
        if (this.$stateParams.permission) {
            this.initUpdatePermissionForm(this.$stateParams.permission);
            this.title = "Edit " + this.$stateParams.permission + ' Permission';
            this.newPermission = false;
        }
        else {
            this.newPermission = true;
            this.title = "New Permission";
            this._initNewPermissionForm();
        }
    }

    _getUsersAndGroups() {
        this.permissionsDao.getAllUsersAndGroups().$promise.then((response)=> {
            this.allUsers = response.allUsers;
            this.allGroups = response.allGroups;
            this._filterAvailableUsers();
            this._filterAvailableGroups();
        });
    }

    _getAllRepos() {
        let deferred = this.$q.defer();

        this.repoDataDao.getAllForPerms({"permission": true}).$promise.then((result)=> {
            this.allRepos = result.repoTypesList;
            deferred.resolve();
        });
        return deferred.promise;

    }

    _initNewPermissionForm() {

        this.permission.anyLocal = false;
        this.permission.anyRemote = false;

        this.permission.includePattern = '**';

        this.permission.availableRepoKeys = [];
        this.permission.repoKeys = [];

        this._getUsersAndGroups();

        this._getAllRepos().then(()=>{
            this.permission.availableRepoKeys = _.map(this.allRepos, (repo)=> {
                return repo;
            });
        });
    }

    initUpdatePermissionForm(permission) {

        this.permissionsDao.getPermission({name: permission}).$promise.then((result)=> {

            //console.log(result);
            this.permission = result;

            this.permission.repoKeys = _.map(this.permission.repoKeys,(repo)=>{
                repo["__fixed__"]=(repo.type === 'local' && this.permission.anyLocal) || (repo.type === 'remote' && this.permission.anyRemote);
                repo._iconClass = "icon " + (repo.type === 'local' ? "icon-local-repo" : (repo.type === 'remote' ? "icon-remote-repo" : (repo.type === 'virtual' ? "icon-virtual-repo" : "icon-notif-error")));
                return repo;
            });

            this.permission.availableRepoKeys = _.map(this.permission.availableRepoKeys,(repo)=>{
                repo._iconClass = "icon " + (repo.type === 'local' ? "icon-local-repo" : (repo.type === 'remote' ? "icon-remote-repo" : (repo.type === 'virtual' ? "icon-virtual-repo" : "icon-notif-error")));
                return repo;
            });

            this.usersGridOption.setGridData(result.users);
            this.groupsGridOption.setGridData(result.groups);

            this._getUsersAndGroups();

        });
    }

    /**check and set current tab**/

    setCurrentTab(tab) {
        this.currentTab = tab;
    }

    isCurrentTab(tab) {
        return this.currentTab === tab;
    }

    /**
     * button pre and  forwd at the bottom page**/
    prevStep() {
        if (this.currentTab == 'groups') {
            this.setCurrentTab('repo');
            return;
        }
        if (this.currentTab == 'users') {
            this.setCurrentTab('groups');
            return;
        }
    }

    fwdStep() {
        if (this.currentTab == 'repo') {
            this.setCurrentTab('groups');
            return;
        }
        if (this.currentTab == 'groups') {
            this.setCurrentTab('users');
            return;
        }
    }


    addGroup(group) {
        if (!this.permission.groups) this.permission.groups = [];
        this.permission.groups.push({principal: group,annotate:false,delete:false,deploy:false,managed:false,read:false,mask:31});
        this.groupsGridOption.setGridData(this.permission.groups);
        this._filterAvailableGroups();

/*
        if (group.name) {
            this.groups = _.remove(this.permission.groups, {name: group.name});
            this.groupsGrid.push({principal: group.name});
        }
        else {
            this.groups = _.remove(this.permission.groups, group);
            this.groupsGrid.push(group);
        }

        this.groupsGridOption.setGridData(this.groupsGrid);
*/

    }

    addUser(user) {
        if (!this.permission.users) this.permission.users = [];
        this.permission.users.push({principal: user,annotate:false,delete:false,deploy:false,managed:false,read:false,mask:31});
        this.usersGridOption.setGridData(this.permission.users);
        this._filterAvailableUsers();
/*
        if (user.name) {
            this.users = _.remove(this.permission.users, {name: user.name});
            this.usersGrid.push({principal: user.name});
        }
        else {
            this.users = _.remove(this.permission.users, user);
            this.usersGrid.push(user);
        }
        this.usersGridOption.setGridData(this.usersGrid);
*/
    }

    setAnyLocalRepo() {
        if (this.permission.anyLocal) {
            this.permission.availableRepoKeys.forEach((repo)=> {
                if (repo.type == "local") {
                    repo["__fixed__"]=true;
                    if (!_.contains(this.permission.repoKeys, repo)) {
                        this.permission.repoKeys.push(repo);
                    }
                }
            });
            this.permission.repoKeys.forEach((repo)=> {
                if (repo.type == "local") {
                    repo["__fixed__"] = true;
                }
            });
            _.remove(this.permission.availableRepoKeys, {type: "local"});
        }
        else {
            this.permission.repoKeys.forEach((repo)=> {
                if (repo.type == "local") {
                    if (!_.contains(this.permission.availableRepoKeys, repo)) {
                        this.permission.availableRepoKeys.push(repo);
                    }
                }
            });
            _.remove(this.permission.repoKeys, {type: "local"});
        }
    }

    setAnyRemoteRepo() {
        if (this.permission.anyRemote) {
            this.permission.availableRepoKeys.forEach((repo)=> {
                if (repo.type == "remote") {
                    repo["__fixed__"]=true;
                    if (!_.contains(this.permission.repoKeys, repo)) {
                        this.permission.repoKeys.push(repo);
                    }
                }
            });
            this.permission.repoKeys.forEach((repo)=> {
                if (repo.type == "remote") {
                    repo["__fixed__"] = true;
                }
            });
            _.remove(this.permission.availableRepoKeys, {type: "remote"});
        }
        else {
            this.permission.repoKeys.forEach((repo)=> {
                if (repo.type == "remote") {
                    if (!_.contains(this.permission.availableRepoKeys, repo)) {
                        this.permission.availableRepoKeys.push(repo);
                    }
                }
            });
            _.remove(this.permission.repoKeys, {type: "remote"});
        }
    }

    _createGroupsGrid() {
        this.groupsGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setRowTemplate('default')
                .setMultiSelect()
                .setColumns(this._getGroupsColumns())
                .setButtons(this._getGroupsActions())
                .setGridData([])
                .setBatchActions(this._getGroupsBatchActions());
    }

    _createUsersGrid() {
        this.usersGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setRowTemplate('default')
                .setMultiSelect()
                .setColumns(this._getUsersColumns())
                .setGridData([])
                .setButtons(this._getUsersActions())
                .setBatchActions(this._getUsersBatchActions());

        this.usersGridOption.isRowSelectable = (row) => {
            return row.entity.principal !== this.user.name;
        }
    }

    _getUsersColumns() {
        return [
            {
                name: 'User',
                displayName: 'User',
                field: 'principal',
                width: '26%'
            },
            {
                name: 'Manage',
                displayName: 'Manage',
                field: 'managed',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.managed', 'row.entity.managed?row.entity.delete=row.entity.deploy=row.entity.annotate=row.entity.read=true:null', 'grid.appScope.PermissionForm.isDisableManager(row.entity)'),
                width: '12%'
            },
            {
                name: 'Delete/Overwrite',
                displayName: 'Delete/Overwrite',
                field: 'delete',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.delete', 'row.entity.delete?row.entity.deploy=row.entity.annotate=row.entity.read=true:null'),
                width: '20%'
            },
            {
                name: 'Deploy/Cache',
                displayName: 'Deploy/Cache',
                field: 'deploy',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.deploy', 'row.entity.deploy?row.entity.annotate=row.entity.read=true:null'),
                width: '20%'
            },
            {
                name: 'Annotate',
                displayName: 'Annotate',
                field: 'annotate',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.annotate', 'row.entity.annotate?row.entity.read=true:null'),
                width: '12%'
            },
            {
                name: 'Read',
                displayName: 'Read',
                field: 'read',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.read'),
                width: '10%'
            }
        ]
    }

    _getGroupsColumns() {
        return [
            {
                name: 'Group',
                displayName: 'Group',
                field: 'principal',
                width: '26%'
            },
            {
                name: 'Manage',
                displayName: 'Manage',
                field: 'managed',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.managed', 'row.entity.managed?row.entity.delete=row.entity.deploy=row.entity.annotate=row.entity.read=true:null'),
                width: '12%'
            },
            {
                name: 'Delete/Overwrite',
                displayName: 'Delete/Overwrite',
                field: 'delete',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.delete', 'row.entity.delete?row.entity.deploy=row.entity.annotate=row.entity.read=true:null'),
                width: '20%'
            },
            {
                name: 'Deploy/Cache',
                displayName: 'Deploy/Cache',
                field: 'deploy',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.deploy', 'row.entity.deploy?row.entity.annotate=row.entity.read=true:null'),
                width: '20%'
            },
            {
                name: 'Annotate',
                displayName: 'Annotate',
                field: 'annotate',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.annotate', 'row.entity.annotate?row.entity.read=true:null'),
                width: '12%'
            },
            {
                name: 'Read',
                displayName: 'Read',
                field: 'read',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.read'),
                width: '10%'
            }
        ]
    }


    _getUsersBatchActions() {

        return [
            {
                icon: 'clear',
                name: 'Remove',
                callback: () => this._deleteSelectedUsers()
            }
        ]
    }

    _getGroupsBatchActions() {

        return [
            {
                icon: 'clear',
                name: 'Remove',
                callback: () => this._deleteSelectedGroups()
            }
        ]
    }

    _deleteSelectedGroups() {
        let self = this;
        let selectedGroups = this.groupsGridOption.api.selection.getSelectedRows();
        let confirmMessage = 'Are you sure you wish to delete ' + selectedGroups.length;

        selectedGroups.forEach((group)=> {
            _.remove(this.permission.groups,group);
        });
        this.groupsGridOption.setGridData(this.permission.groups);
        this._filterAvailableGroups();
    }

    _deleteSelectedUsers() {
        let selectedUsers = this.usersGridOption.api.selection.getSelectedRows();
        selectedUsers.forEach((user)=> {
            _.remove(this.permission.users, user);
        });
        this.usersGridOption.setGridData(this.permission.users);
        this._filterAvailableUsers();
    }

    _getGroupsActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Remove',
                callback: row => this._deleteGroup(row)
            }
        ]
    }

    _deleteGroup(row) {
        _.remove(this.permission.groups,row);
        this.groupsGridOption.setGridData(this.permission.groups);
        this._filterAvailableGroups();

    }

    _getUsersActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Remove',
                callback: row => this._deleteUser(row),
                visibleWhen: row => row.principal !== this.user.name
            }
        ]
    }

    _deleteUser(row) {
    //    this.modal.confirm('Are you sure you wish to delete this user?')
      //      .then(()=> {
        _.remove(this.permission.users,row);
        this.usersGridOption.setGridData(this.permission.users);

        this._filterAvailableUsers();

/*
                _.remove(this.usersGrid, row);
                this.permission.users.push(row);
                this.usersGridOption.setGridData(this.usersGrid);
*/
        //    });
    }

    save() {
//        this.permission.users = this.usersGrid;
//        this.permission.groups = this.groupsGrid;
        if (this.newPermission) {
            this.permissionsDao.create(this.permission).$promise.then(()=> {
                this.$state.go('^.permissions')
            });
        }
        else {
            this.permissionsDao.update(this.permission).$promise.then(()=> {
                this.$state.go('^.permissions')
            });
        }
    }

    isDisableRepositories() {
        return !this.user.isAdmin() && !this.newPermission;
    }

    isDisableManager(row) {
        return row.principal === this.user.name;
    }

    _filterAvailableGroups() {
        this.availableGroups = _.filter(this.allGroups,(group)=>{
            return _.findWhere(this.permission.groups, {principal: group}) === undefined;
        });
    }
    _filterAvailableUsers() {
        this.availableUsers = _.filter(this.allUsers,(user)=>{
            return _.findWhere(this.permission.users, {principal: user}) === undefined;
        });
    }

}