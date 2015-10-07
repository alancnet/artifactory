import FIELD_OPTIONS from '../../../constants/field_options.constats';

export class AdminRepositoriesController {

    constructor($scope, $state, ArtifactoryGridFactory, RepositoriesDao, ArtifactoryModal, uiGridConstants,
            ArtifactActionsDao, ArtifactoryFeatures) {
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.$state = $state;
        this.repositoriesDao = RepositoriesDao;
        this.$scope = $scope;
        this.modal = ArtifactoryModal;
        this.artifactActionsDao = ArtifactActionsDao;
        this.gridOption = {};
        this.uiGridConstants = uiGridConstants;
        this.currentRepoType = $state.params.repoType;
        this.features = ArtifactoryFeatures;
        this._createGrid();
        this._initRepos();
    }

    isCurrentRepoType(type) {
        return this.currentRepoType == type;
    }

    /**
     * Creates the grid according to current repo type, sets draggable according to the global repo status
     * NOTE: Multi select and batch actions are commented until batch delete repos is approved for prod.
     */
    _createGrid() {
        this.gridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getColumns());
            //.setMultiSelect()
            //.setBatchActions(this._getBatchActions())
        if(this.features.isGlobalRepoEnabled()) {
            this.gridOption.setDraggable(this.reorderRepositories.bind(this));
        }
        else {
            this.gridOption.setRowTemplate('default');
        }
    }

    _initRepos() {
        this.repositoriesDao.getRepositories({type: this.currentRepoType}).$promise
                .then((data) => {
                    _.forEach(data, (row) => {
                        row.displayType = _.find(FIELD_OPTIONS.repoPackageTypes, (type) => {
                            return type.value == row.repoType.toLowerCase();
                        }).text;
                    });
                    this.gridData = data;
                    this.gridOption.setGridData(data);
                });
    }

    reorderRepositories() {
        return this.repositoriesDao.reorderRepositories({repoType: this.currentRepoType}, this.getRepoOrder()).$promise
    }

    getRepoOrder() {
        let repoOrderList = [];
        this.gridData.forEach((data)=> {
            repoOrderList.push(data.repoKey);
        });
        return repoOrderList;
    }

    _deleteSelected(row) {
        this.modal.confirm("Are you sure you wish to delete this repository? All artifacts will be permanently deleted.", 'Delete ' + row.repoKey + " Repository", {confirm: 'Delete'})
                .then(()=> {
                    this.repositoriesDao.deleteRepository({
                        type: this.currentRepoType,
                        repoKey: row.repoKey
                    }).$promise.then((result)=> {
                                this._initRepos();
                            })
                });
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this._deleteSelectedRepos()
            }
        ]
    }

    _deleteSelectedRepos() {
        let selectedRows = this.gridOption.api.selection.getSelectedGridRows();
    }

    _editSelected(row) {
        this.$state.go('^.list.edit', {repoType: this.currentRepoType, repoKey: row.repoKey});
    }


    createNewRepo() {
        this.$state.go('^.list.new', {repoType: this.currentRepoType});
    }

    _calculateIndex(row) {
        this.artifactActionsDao.perform({
            action: 'calculateIndex',
            type: row.repoType,
            repoKey: row.repoKey
        })
    }

    localReplicationsRunNow(repoKey) {
        this.repositoriesDao.runNowReplications({repoKey: repoKey}).$promise.then(()=> {
        });
    }

    remoteExecuteReplicationNow(repoKey) {
        this.repositoriesDao.executeRemoteReplicationNow({repoKey: repoKey},
                this.repoInfo).$promise.then((result)=> {

                });
    }

    _getColumns() {
        switch(this.currentRepoType) {
            case 'local':
                return this._getLocalColumns();
            case 'remote':
                return this._getRemoteColumns();
            case 'virtual':
                return this._getVirtualColumns();
        }
    }

    _getLocalColumns() {
        return [
            {
                name: 'Repository Key',
                displayName: 'Repository Key',
                field: 'repoKey',
                cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="^.list.edit({repoType:\'local\',repoKey: row.entity.repoKey})" id="repositories-local-key">{{COL_FIELD}}</a></div>',
                width: '55%',
                enableSorting: !this.features.isGlobalRepoEnabled()
                //sort: {
                //    direction: this.uiGridConstants.ASC
                //}
            },
            {
                name: 'Type',
                displayName: 'Type',
                field: 'displayType',
                width: '15%',
                enableSorting: !this.features.isGlobalRepoEnabled()
            },
            {
                name: 'Recalculate Index',
                displayName: 'Recalculate Index',
                field: 'reindex',
                cellTemplate: '<div class="ui-grid-cell-contents text-center"><a class="grid-column-button icon icon-re-index" ng-click="!row.entity.hasReindexAction || grid.appScope.Repositories._calculateIndex(row.entity)" ng-disabled="!row.entity.hasReindexAction" jf-tooltip="{{row.entity.hasReindexAction ? \'Recalculate Index Now\' : \'Recalculate Not Supported For Repo Type\'}}" id="repositories-local-reindex"></a></div>',
                width: '15%',
                enableSorting: false
            },
            {
                name: 'Replications',
                displayName: 'Replications',
                field: 'replications',
                cellTemplate: '<div class="ui-grid-cell-contents text-center"><a class="grid-column-button icon icon-run" ng-click="!row.entity.replications || grid.appScope.Repositories.localReplicationsRunNow(row.entity.repoKey)" ng-disabled="!row.entity.replications" jf-tooltip="{{row.entity.replications ? \'Run Replication\' : \'No Replication Configured\'}}" id="repositories-local-replicate"></a></div>',
                width: '15%',
                actions: {
                    delete: row => this._deleteSelected(row)
                },
                enableSorting: false
            }
        ]
    }

    _getRemoteColumns() {
        return [
            {
                name: 'Repository Key',
                displayName: 'Repository Key',
                field: 'repoKey',
                cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="^.list.edit({repoType:\'remote\',repoKey: row.entity.repoKey})" id="repositories-remote-key">{{COL_FIELD}}</a></div>',
                width: '20%',
                enableSorting: !this.features.isGlobalRepoEnabled()
            },
            {
                name: 'URL',
                displayName: 'URL',
                field: 'url',
                width: '40%',
                enableSorting: !this.features.isGlobalRepoEnabled()
            },
            {
                name: 'Type',
                displayName: 'Type',
                field: 'displayType',
                width: '10%',
                enableSorting: !this.features.isGlobalRepoEnabled()
            },
            {
                name: 'Recalculate Index',
                displayName: 'Recalculate Index',
                field: 'reindex',
                cellTemplate: '<div class="ui-grid-cell-contents text-center"><a class="grid-column-button icon icon-re-index" ng-click="!row.entity.hasReindexAction || grid.appScope.Repositories._calculateIndex(row.entity)" ng-disabled="!row.entity.hasReindexAction" jf-tooltip="{{row.entity.hasReindexAction ? \'Recalculate Index Now\' : \'Recalculate Not Supported For Repo Type\'}}" id="repositories-local-reindex"></a></div>',
                width: '15%',
                enableSorting: false
            },
            {
                name: 'Replications',
                displayName: 'Replications',
                field: 'hasEnabledReplication',
                cellTemplate: '<div class="ui-grid-cell-contents text-center"><a class="grid-column-button icon icon-run" ng-click="!row.entity.hasEnabledReplication || grid.appScope.Repositories.remoteExecuteReplicationNow(row.entity.repoKey)" ng-disabled="!row.entity.hasEnabledReplication" jf-tooltip="{{row.entity.hasEnabledReplication ? \'Run Replication\' : \'No Replication Configured\'}}" id="repositories-local-replicate"></a></div>',
                width: '15%',
                actions: {
                    delete: row => this._deleteSelected(row)
                },
                enableSorting: false
            }
        ]
    }

    _getVirtualColumns() {
        return [
            {
                name: 'Repository Key',
                displayName: 'Repository Key',
                field: 'repoKey',
                cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="^.list.edit({repoType:\'virtual\',repoKey: row.entity.repoKey})" id="repositories-virtual-key">{{COL_FIELD}}</a></div>',
                width: '20%',
                enableSorting: !this.features.isGlobalRepoEnabled()
            },
            {
                name: 'Type',
                displayName: 'Type',
                field: 'displayType',
                width: '10%',
                enableSorting: !this.features.isGlobalRepoEnabled()
            },
            {
                name: 'Included Repositories',
                displayName: 'Included Repositories',
                field: 'numberOfIncludesRepositories',
                width: '15%',
                enableSorting: false
            },
            {
                name: 'Selected Repositories',
                displayName: 'Selected Repositories',
                field: 'selectedRepos',
                cellTemplate: '<div class="ui-grid-cell-contents" id="repositories-virtual-selected" ng-if="row.entity.selectedRepos.length">{{row.entity.selectedRepos.length}} | {{row.entity.selectedRepos.join(", ")}}</a></div>' +
                              '<div class="ui-grid-cell-contents" id="repositories-virtual-selected" ng-if="!row.entity.selectedRepos.length">-</a></div>',
                cellClass: 'tooltip-show-list',
                width: '40%',
                enableSorting: false
            },
            {
                name: 'Recalculate Index',
                displayName: 'Recalculate Index',
                field: 'reindex',
                cellTemplate: '<div class="ui-grid-cell-contents text-center"><a class="grid-column-button icon icon-re-index" ng-click="!row.entity.hasReindexAction || grid.appScope.Repositories._calculateIndex(row.entity)" ng-disabled="!row.entity.hasReindexAction" jf-tooltip="{{row.entity.hasReindexAction ? \'Recalculate Index Now\' : \'Recalculate Not Supported For Repo Type\'}}" id="repositories-virtual-reindex"></a></div>',
                width: '15%',
                actions: {
                    delete: row => this._deleteSelected(row)
                },
                enableSorting: false
            }
        ];
    }
}