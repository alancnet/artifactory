import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/builds.constants';
let headerCellGroupingTemplate = require("raw!../../../../ui_components/artifactory_grid/templates/headerCellTemplate.html");

const defaultPagination = {
    pageNum: 1,
    numOfRows: 25,
    direction: "asc",
    orderBy: "name"
}


class jfPublishedModulesController {
    constructor($scope, $state, $stateParams, BuildsDao, ArtifactBuildsDao, ArtifactActionsDao, ArtifactoryGridFactory,
            artifactoryDownload, ArtifactoryModal, ArtifactoryFeatures, ArtifactoryEventBus, $timeout, uiGridConstants,
            commonGridColumns,
            User, ArtifactoryStorage) {
        this.$timeout = $timeout;
        this.$stateParams = $stateParams;
        this.$state = $state;
        this.$scope = $scope;
        this.uiGridConstants = uiGridConstants;
        this.commonGridColumns = commonGridColumns;
        this.initialModuleId = $stateParams.moduleID;
        this.buildsDao = BuildsDao;
        this.artifactActionsDao = ArtifactActionsDao;
        this.artifactBuildsDao = ArtifactBuildsDao.getInstance();
        this.download = artifactoryDownload;
        this.modal = ArtifactoryModal;
        this.modulesGridOptions = {};
        this.artifactsGridOptions = {};
        this.dependenciesGridOptions = {};
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.user = User;
        this.modulesCount = 0;
        this.artifactsCount = 0;
        this.dependenciesCount = 0;
        this.DICTIONARY = DICTIONARY.generalInfo;
        this.selectedModule = null;
        this.artifactoryStorage = ArtifactoryStorage;
        this.comparableNumbers = [''];
        this.selectedBuildNumber = '';
        this.artifactoryFeatures = ArtifactoryFeatures;

        this._getComparableBuildNumbers();
        this._createGrids();

        if ($stateParams.moduleID) {
            this.selectedModule = $stateParams.moduleID;
            this.getSubData();
        }
        else {
            this.selectedModule = null;
        }

    }

    showArtifactInTree(row) {
        let browser = this.artifactoryStorage.getItem('BROWSER') || 'tree';
        if (browser === 'stash') browser = 'tree';
        let path = row.repoKey + '/' + row.path;
        this.$state.go('artifacts.browsers.path', {
            tab: "General",
            artifact: path,
            browser: browser
        });
    }

    downloadArtifact(row) {
        this.download(row.downloadLink);
    }

    viewCodeArtifact(row) {
        this.artifactActionsDao.perform(
                {action: 'view'},
                {
                    repoKey: row.repoKey,
                    path: row.path
                })
                .$promise.then((result) => {
                    this.modal.launchCodeModal(row.name, result.data.fileContent,
                            {name: row.type, json: true});
                });
    }

    selectModule(entity) {
        if (!this.artifactoryFeatures.isDisabled("publishedmodule")) {
            this.$state.go('builds.info', {
                buildName: this.$stateParams.buildName,
                buildNumber: this.$stateParams.buildNumber,
                startTime: this.$stateParams.startTime,
                tab: this.$stateParams.tab,
                moduleID: entity.moduleId
            });
        }
    }

    getSubData() {
        if (this.compare && this.selectedBuildNumber && this.selectedBuildNumber.buildNumber) {
            this._getArtifactsDiff();
            this._getDependenciesDiff();
        }
        else {
            this._getArtifacts();
            this._getDependencies();
        }
    }

    onCompareChanged() {
        if (!(this.selectedBuildNumber && this.selectedBuildNumber.buildNumber)) {
            // Don't get data if haven't selected build number yet
            return;
        }
        this.getSubData();
    }

    _getModulesData() {
        this.buildsDao.getData(defaultPagination, {
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'publishedModules'
        })
                .$promise.then((data) => {
                    this.modulesCount = data.pagingData.length;
                    this.modulesGridOptions.setGridData(data.pagingData);
                    if (this.initialModuleId) {
                        this.initialModuleId = null;
                        let module = _.findWhere(data.pagingData, {moduleId: this.$stateParams.moduleID})
                        //                        this.modulesGridOptions.selectItem(module);
                    }
                });
    }

    _getArtifacts() {
        this.buildsDao.getData(defaultPagination, {
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'modulesArtifact',
            moduleId: this.selectedModule
        }).$promise.then((data) => {
                    this.artifactsCount = data.pagingData.length;
                    this.artifactsGridOptions.setGridData(data.pagingData);
                });
    }

    _getDependencies() {

        let defaultPagination = {
            pageNum: 1,
            numOfRows: 25,
            direction: "asc",
            orderBy: "id"
        };
        this.buildsDao.getData(defaultPagination, {
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'modulesDependency',
            moduleId: this.selectedModule
        }).$promise.then((data) => {
                    this.dependenciesCount = data.pagingData.length;
                    this.dependenciesGridOptions.setGridData(data.pagingData);
                });

    }

    _getArtifactsDiff() {
        return this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'artifactDiff',
            moduleId: this.selectedModule,

            otherNumber: this.selectedBuildNumber.buildNumber,
            otherDate: this.selectedBuildNumber.time,

            pageNum: 1,
            numOfRows: 25,
            direction: "asc",
            orderBy: "name",
        }).$promise.then((data) => {
                    this.artifactsCount = data.pagingData.length;
                    this.artifactsGridOptions.setGridData(data.pagingData);
                });
        ;
    }

    _getDependenciesDiff() {

        this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'dependencyDiff',
            moduleId: this.selectedModule,
            otherNumber: this.selectedBuildNumber.buildNumber,
            otherDate: this.selectedBuildNumber.time,

            pageNum: 1,
            numOfRows: 25,
            direction: "asc",
            orderBy: "id"
        }).$promise.then((data) => {
                    this.dependenciesCount = data.pagingData.length;//data.totalItems ? data.totalItems : 0;
                    this.dependenciesGridOptions.setGridData(data.pagingData);
                });

    }

    _getComparableBuildNumbers() {

        this.buildsDao.getDataArray({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'prevBuild'
        }).$promise.then((data) => {
                    this.comparableBuildNumbers = data;
                })

    }


    _createGrids() {

        this.modulesGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getModulesColumns())
                .setRowTemplate('default');

        this.modulesGridOptions.onSelectionChange = (data) => {
            this.selectModule(data.entity);
        }

        this.artifactsGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getArtifactsColumns())
                .setRowTemplate('default')

        this.dependenciesGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getDependenciesColumns())
                .setRowTemplate('default');

        this._getModulesData();
    }

    _getModulesColumns() {
        let cellTemplate = '<div ng-click="grid.appScope.jfPublishedModules.selectModule(row.entity)" class="ui-grid-cell-contents"><a href="">{{row.entity.moduleId}}</a></div>';

        return [
            {
                name: "Module ID",
                displayName: "Module ID",
                field: "moduleId",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate: cellTemplate,
                width: '60%'
            },
            {
                name: "Number Of Artifacts",
                displayName: "Number Of Artifacts",
                field: "numOfArtifacts",
                width: '20%'
            },
            {
                name: "Number Of Dependencies",
                displayName: "Number Of Dependencies",
                field: "numOfDependencies",
                width: '20%'
            }

        ]
    }

    _getArtifactsColumns() {

        let typeCellTemplate = '<div class="ui-grid-cell-contents status-{{(row.entity.status).toLowerCase()}}">{{row.entity.type}}</div>';

        return [
            {
                name: "Artifact Name",
                displayName: "Artifact Name",
                field: "name",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                width: '40%',
                cellTemplate: this.commonGridColumns.downloadableColumn('status-{{(row.entity.status).toLowerCase()}}'),
                customActions: [{
                    icon: 'icon icon-view',
                    tooltip: 'View',
                    callback: row => this.viewCodeArtifact(row),
                    visibleWhen: row => _.findWhere(row.actions, {name: "View"})
                }],
                actions: {
                    download: {
                        callback: row => this.downloadArtifact(row),
                        visibleWhen: row => _.findWhere(row.actions, {name: "Download"})
                    }
                }
            },
            {
                name: "Type",
                displayName: "Type",
                field: "type",
                cellTemplate: typeCellTemplate,
                headerCellTemplate: headerCellGroupingTemplate,
                grouped: true,
                width: '10%'
            },
            {
                name: "Repo Path",
                displayName: "Repo Path",
                field: "repoPath",
                width: '50%',
                cellTemplate: this.commonGridColumns.repoPathColumn('status-{{(row.entity.status).toLowerCase()}}'),
                customActions: [{
                    icon: 'icon icon-show-in-tree',
                    tooltip: 'Show In Tree',
                    callback: row => this.showArtifactInTree(row),
                    visibleWhen: row => _.findWhere(row.actions, {name: "ShowInTree"})
                }]
            }

        ];
    }

    _getDependenciesColumns() {

        let typeCellTemplate = '<div class="ui-grid-cell-contents status-{{(row.entity.status).toLowerCase()}}">{{row.entity.type}}</div>';
        let scopeCellTemplate = '<div class="ui-grid-cell-contents status-{{(row.entity.status).toLowerCase()}}">{{row.entity.scope}}</div>';

        return [
            {
                name: "Dependency ID",
                displayName: "Dependency ID",
                field: "name",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                width: '30%',
                cellTemplate: this.commonGridColumns.downloadableColumn('status-{{(row.entity.status).toLowerCase()}}'),
                actions: {
                    download: {
                        callback: row => this.downloadArtifact(row),
                        visibleWhen: row => _.findWhere(row.actions, {name: "Download"})
                    }
                }
            },
            {
                name: "Scope",
                displayName: "Scope",
                field: "scope",
                cellTemplate: scopeCellTemplate,
                headerCellTemplate: headerCellGroupingTemplate,
                grouped: true,
                width: '10%'
            },
            {
                name: "Type",
                displayName: "Type",
                field: "type",
                cellTemplate: typeCellTemplate,
                headerCellTemplate: headerCellGroupingTemplate,
                grouped: true,
                width: '10%'
            },
            {
                name: "Repo Path",
                displayName: "Repo Path",
                field: "repoPath",
                width: '50%',
                cellTemplate: this.commonGridColumns.repoPathColumn('status-{{(row.entity.status).toLowerCase()}}'),
                customActions: [{
                    icon: 'icon icon-show-in-tree',
                    tooltip: 'Show In Tree',
                    callback: row => this.showArtifactInTree(row),
                    visibleWhen: row => _.findWhere(row.actions, {name: "ShowInTree"})
                }]
            }
        ];
    }

    _installWatchers() {
        this.$scope.$watch('jfPublishedModules.selectedBuildNumber', (val) => {
            if (val.length) {
                this.getSubData();
            }
        });
        this.$scope.$watch('jfPublishedModules.compare', (val) => {
            if (val !== undefined) {
                this.getSubData();
            }
        });
    }

    backToModules() {
        this.$state.go('builds.info', {
            buildName: this.$stateParams.buildName,
            buildNumber: this.$stateParams.buildNumber,
            startTime: this.$stateParams.startTime,
            tab: this.$stateParams.tab,
            moduleID: ''
        });
    }

}


export function jfPublishedModules() {
    return {
        restrict: 'EA',
        controller: jfPublishedModulesController,
        controllerAs: 'jfPublishedModules',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/builds_info/build_tabs/jf_published_modules.html'
    }
}