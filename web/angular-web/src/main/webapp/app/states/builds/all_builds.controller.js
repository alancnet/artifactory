import EVENTS from '../../constants/artifacts_events.constants';

export class AllBuildsController {
    constructor($scope, $q, $timeout, ArtifactoryGridFactory, BuildsDao, ArtifactoryEventBus, ArtifactoryModal,
            ArtifactoryNotifications, uiGridConstants, User) {

        this.$scope = $scope;
        this.uiGridConstants = uiGridConstants;
        this.user = User.getCurrent();
        this.$timeout = $timeout;
        this.$q = $q;
        this.allBuildsGridOptions = {};
        this.buildsDao = BuildsDao;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.modal = ArtifactoryModal;
        this._createGrid();
        this._getBuildsData();
        this.firstFetch = true;

        this.updateBreadcrumbs();
    }

    updateBreadcrumbs() {
        this.artifactoryEventBus.dispatch(EVENTS.BUILDS_BREADCRUMBS, name);
    }

    deleteBuild(row) {
        let json ={
            buildsCoordinates:[
                {
                    buildName:row.buildName
                }
            ]
        }
        this.modal.confirm("Are you sure you wish to delete all the builds '" + row.buildName + "'?")
            .then(() => {
                this.buildsDao.deleteAll(json).$promise.then(() => {
                    this._getBuildsData();
                });
            })
    }

    bulkDelete() {
        let selectedRows = this.allBuildsGridOptions.api.selection.getSelectedRows();
        let confirmMessage = 'Are you sure you wish to delete ' + selectedRows.length;
        confirmMessage += selectedRows.length > 1 ? ' build projects?' : ' build project?';

        this.modal.confirm(confirmMessage)
            .then(() => {
                    let buildsToDelete = selectedRows.map(build => {
                        return {
                            buildName:build.buildName
                        }
                    });
                    //console.log(buildsToDelete);
                    let json = {
                        buildsCoordinates:buildsToDelete
                    }
                    this.buildsDao.deleteAll(json).$promise.then(() => {
                        this._getBuildsData();
                    });
                    //this.artifactoryNotifications.create({info: 'Builds deleted'})
            });
    }

    _getBuildsData(pagination) {

        let defaultPagination = {
            pageNum: 1,
            numOfRows: 25,
            direction: "asc",
            orderBy: "lastBuildTime"
        };
        this.buildsDao.get(defaultPagination).$promise.then((data) => {

            this.allBuildsGridOptions.setGridData(data.pagingData);
        });
    }

    _createGrid() {

        this.allBuildsGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getColumns())
                .setRowTemplate('default')
                .setMultiSelect()
                .setButtons(this._getActions())
                .setBatchActions(this._getBatchActions());

    }

    _getColumns() {

        let nameCellTemplate = '<div ng-click="grid.appScope.AllBuilds.updateBreadcrumbs()" ' +
            'class="ui-grid-cell-contents"><a href="" ui-sref="builds.history({buildName:row.entity.buildName})">{{row.entity.buildName}}</a></div>';
        let numberCellTemplate = '<div ng-click="grid.appScope.AllBuilds.updateBreadcrumbs()" ' +
                'class="ui-grid-cell-contents"><a href="" ui-sref="builds.info({buildName:row.entity.buildName,buildNumber:row.entity.buildNumber,startTime:row.entity.time})">{{row.entity.buildNumber}}</a></div>';
        let timeCellTemplate = '<div class="ui-grid-cell-contents">{{row.entity.lastBuildTime }}</a>';

        return [
            {
                name: "Project Name",
                displayName: "Project Name",
                field: "buildName",
                cellTemplate: nameCellTemplate,
                width: '60%'
            },
            {
                name: "Last Build ID",
                displayName: "Last Build ID",
                field: "buildNumber",
                cellTemplate: numberCellTemplate,
                width: '20%'
            },
            {
                name: "Last Build Time",
                displayName: "Last Build Time",
                cellTemplate: timeCellTemplate,
                field: "time",
                sort: {
                    direction: this.uiGridConstants.DESC
                },
                type: 'number',
                width: '20%'
            }
        ]
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
                icon: 'clear',
                name: 'Delete',
                callback: () => this.bulkDelete(),
                visibleWhen: () => this.user.isAdmin()
            }
        ]
    }
}
