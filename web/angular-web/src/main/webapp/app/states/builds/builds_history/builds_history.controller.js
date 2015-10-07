import EVENTS from '../../../constants/artifacts_events.constants';
let headerCellGroupingTemplate = require("raw!../../../ui_components/artifactory_grid/templates/headerCellTemplate.html");

export class BuildsHistoryController {
    constructor($scope, $q, $timeout, $stateParams, ArtifactoryGridFactory, BuildsDao, ArtifactoryEventBus, ArtifactoryModal,
            ArtifactoryNotifications, uiGridConstants, User) {
        this.$stateParams = $stateParams;
        this.$scope = $scope;
        this.uiGridConstants = uiGridConstants;
        this.user = User.getCurrent();
        this.$timeout = $timeout;
        this.$q = $q;
        this.buildsHistoryGridOption = {};
        this.buildTitle='History for Build  \''+this.$stateParams.buildName+'\'';
        this.totalBuilds='';
        this.buildsDao = BuildsDao;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.modal = ArtifactoryModal;
        this.firstFetch = true;

        this.artifactoryEventBus.dispatch(EVENTS.BUILDS_BREADCRUMBS);


        this._createGrid();
        this._getBuildsData();
    }

    deleteBuild(row) {
        let json ={
            buildsCoordinates:[
                {
                    buildName:this.$stateParams.buildName,
                    buildNumber:row.buildNumber,
                    date:row.time
                }
            ]
        }
        this.modal.confirm("Are you sure you wish to delete the build '"+this.$stateParams.buildName+"' #"+row.buildNumber+"?")
                .then(() => {
                    this.buildsDao.delete(json).$promise.then(() => {
                        this._getBuildsData();
                    });
                })
    }

    bulkDelete() {
        let selectedRows = this.buildsHistoryGridOption.api.selection.getSelectedRows();
        let confirmMessage = 'Are you sure you wish to delete ' + selectedRows.length;
        confirmMessage += selectedRows.length > 1 ? ' builds?' : ' build?';

        this.modal.confirm(confirmMessage)
                .then(() => {
                    let buildsToDelete = selectedRows.map(build => {
                        return {
                            buildName:this.$stateParams.buildName,
                            buildNumber:build.buildNumber,
                            date:build.time
                        }
                    });
                    //console.log(buildsToDelete);
                    let json = {
                        buildsCoordinates:buildsToDelete
                    }
                    this.buildsDao.delete(json).$promise.then(() => {
                        this._getBuildsData();
                    });
                });
    }

    _getBuildsData(pagination) {
        if (pagination) {
            if (this.firstFetch) {
                this.firstFetch = false;
                pagination.orderBy = 'buildNumber';
                pagination.direction =  'asc';
            }
            return this.buildsDao.getData(pagination, {
                action: 'history',
                name: this.$stateParams.buildName
            }).$promise;
        }
        else {
            let defaultPagination = {
                pageNum: 1,
                numOfRows: 25,
                direction: "asc",
                orderBy: "buildNumber"
            };

            this.buildsDao.getData(defaultPagination, {
                action: 'history',
                name: this.$stateParams.buildName
            })
            .$promise.then((data) => {
                this.buildsHistoryGridOption.setGridData(data.pagingData);
            });
        }
    }

    _createGrid() {
        this.buildsHistoryGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getColumns())
                .setRowTemplate('default')
            //            .setExternalPagination((pagination) => this._getBuildsData(pagination))
                .setMultiSelect()
                .setButtons(this._getActions())
                .setBatchActions(this._getBatchActions());

    }

    _getColumns() {

        return [
            {
                name: "Build ID",
                displayName: "Build ID",
                field: "buildNumber",
                cellTemplate: '<div class="ui-grid-cell-contents"><a href="" ng-click="grid.appScope.BuildsHistory.onClick(row.entity.build_number)" ui-sref="builds.info({buildName:grid.appScope.BuildsHistory.$stateParams.buildName,buildNumber:row.entity.buildNumber,startTime:row.entity.time,tab:\'general\'})">{{row.entity.buildNumber}}</a></div>',
                width: '40%'
            },
            {
                name: "CI Server",
                displayName: "CI Server",
                field: 'ciUrl',
                cellTemplate: '<div class="ui-grid-cell-contents"><a href="{{row.entity.ciUrl}}" target="_blank">{{row.entity.ciUrl}}</a></div>',
                width: '30%'
            },
            {
                name: "Status",
                displayName: "Status",
                field: "releaseStatus",
                headerCellTemplate: headerCellGroupingTemplate,
                width: '10%'
            },
            {
                name: "Build Time",
                displayName: "Build Time",
                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.lastBuildTime}}</div>',
                field: "time",
                sort: {
                    direction: this.uiGridConstants.DESC
                },
                type: 'number',
                width: '20%'
            }

        ]
    }


    onClick(name) {
        this.artifactoryEventBus.dispatch(EVENTS.BUILDS_BREADCRUMBS, name);
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: row => this.deleteBuild(row),
                visibleWhen: () => this.user.isAdmin()
            }

        ];
    }

    _getBatchActions() {
        return [
            {
                icon: 'icon icon-clear',
                name: 'Delete',
                callback: () => this.bulkDelete(),
                visibleWhen: () => this.user.isAdmin()
            }
        ]
    }
}
