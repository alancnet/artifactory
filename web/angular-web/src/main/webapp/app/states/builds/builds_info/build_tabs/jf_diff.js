let headerCellGroupingTemplate = require("raw!../../../../ui_components/artifactory_grid/templates/headerCellTemplate.html");
let headerCellDefaultTemplate = require("raw!../../../../ui_components/artifactory_grid/templates/headerCellDefaultTemplate.html");
import EVENTS     from '../../../../constants/artifacts_events.constants';
class jfDiffController {
    constructor($scope, $stateParams, $state, $window, BuildsDao, ArtifactoryGridFactory, uiGridConstants,
            commonGridColumns, ArtifactoryEventBus, artifactoryDownload, ArtifactActionsDao, ArtifactoryStorage) {
        this.$stateParams = $stateParams;
        this.$scope = $scope;
        this.$window = $window;
        this.$state = $state;
        this.uiGridConstants = uiGridConstants;
        this.commonGridColumns = commonGridColumns;
        this.artifactoryStorage = ArtifactoryStorage;
        this.artifactActionsDao = ArtifactActionsDao;
        this.buildsDao = BuildsDao;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.artifactsGridOptions = {};
        this.dependenciesGridOptions = {};
        this.envVarsGridOptions = {};
        this.download = artifactoryDownload;
        this.artifactoryGridFactory = ArtifactoryGridFactory;


        this.comparableBuildNumbers = [];

        this.selectedBuildNumber = '';
        this.excludeInternalDeps = false;

        this._createGrids();

        this._getComparableBuildNumbers();

    }

    _getCellTemplate(field) {
        return '<div class="ui-grid-cell-contents status-{{(row.entity.status).toLowerCase()}}">{{row.entity.' + field + '}}</div>';
    }

    _addCellTemplates(colDef) {
        for (let i = 0; i < colDef.length; i++) {
            let col = colDef[i];
            if (!col.cellTemplate) {
                col.cellTemplate = this._getCellTemplate(col.field);
            }
        }
        return colDef;
    }

    _getComparableBuildNumbers() {

        this.buildsDao.getDataArray({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'prevBuild'
        }).$promise.then((data) => {
                    data = _.sortBy(data, (r)=>-r.buildNumber);
                    this.comparableBuildNumbers = data;
                    //            this.comparableBuildNumbers.unshift({buildNumber:''});
                })

    }

    getDiffData() {
        if (this.selectedBuildNumber.buildNumber) {
            this.buildsDao.getData({
                name: this.$stateParams.buildName,
                number: this.$stateParams.buildNumber,
                time: this.$stateParams.startTime,
                action: 'buildDiff',
                otherNumber: this.selectedBuildNumber.buildNumber,
                otherDate: this.selectedBuildNumber.time,
                exDep: this.excludeInternalDeps
            }).$promise.then((data) => {
                        if (data) {
                            this.variablesLength = data.props.length ? data.props.length : 0;
                            this.dependenciesLength = data.props.length ? data.dependencies.length : 0;
                            this.artifactsLength = data.artifacts.length ? data.artifacts.length : 0;

                            if (data.artifacts) {
                                this.artifactsGridOptions.setGridData(data.artifacts || []);
                            }
                            if (data.dependencies) {
                                this.dependenciesGridOptions.setGridData(data.dependencies || []);
                            }
                            if (data.props) {
                                this.envVarsGridOptions.setGridData(data.props || []);
                            }
                        }
                    });
        }
    }

    showInTree(row) {
        let browser = this.artifactoryStorage.getItem('BROWSER') || 'tree';
        if (browser === 'stash') browser = 'tree';
        let artifactPath = row.repoKey + "/" + (row.path);
        let archivePath = '';
        this.$state.go('artifacts.browsers.path', {
            "tab": "General",
            "browser": browser,
            "artifact": artifactPath
        });
        this.artifactoryEventBus.dispatch(EVENTS.TREE_NODE_OPEN, path);
    }


    downloadArtifact(row) {
        this.download(row.downloadLink);
    }

    _createGrids() {

        this.artifactsGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getArtifactsColumns())
                .setRowTemplate('default');
        this.dependenciesGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getDependenciesColumns())
                .setRowTemplate('default');
        this.envVarsGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getEnvVarsColumns())
                .setRowTemplate('default');

    }

    _getArtifactsColumns() {

        return this._addCellTemplates([
            {
                name: "Name (Current Build)",
                field: "name",
                cellTemplate: this.commonGridColumns.downloadableColumn('status-{{(row.entity.status).toLowerCase()}}'),
                width: '20%',
                actions: {
                    download: {
                        callback: row => this.downloadArtifact(row),
                        visibleWhen: row => _.findWhere(row.actions, {name: "Download"})
                    }
                }
            },
            {
                name: "Name",
                field: "prevName",
                headerCellTemplate: headerCellDefaultTemplate.replace('{{ col.displayName CUSTOM_FILTERS }}','Name (Build #{{grid.appScope.jfDiff.selectedBuildNumber.buildNumber}})'),
                width: '20%'
            },
            {
                name: "Status",
                field: "status",
                headerCellTemplate: headerCellGroupingTemplate,
                sort: {direction: this.uiGridConstants.DESC},
                grouped: true,
                width: '10%'
            },
            {
                name: "Module",
                field: "moduleName",
                headerCellTemplate: headerCellGroupingTemplate,
                grouped: true,
                width: '20%'
            },
            {
                name: "Repo path",
                field: "downloadLink",
                cellTemplate: this.commonGridColumns.repoPathColumn('status-{{(row.entity.status).toLowerCase()}}'),
                width: '30%',
                customActions: [{
                    icon: 'icon icon-show-in-tree',
                    tooltip: 'Show In Tree',
                    callback: row => this.showInTree(row),
                    visibleWhen: row => _.findWhere(row.actions, {name: "ShowInTree"})
                }]
            }
        ]);
    }

    _getDependenciesColumns() {
        return this._addCellTemplates([
            {
                name: "Dependency ID (Current Build)",
                displayName: "Dependency ID (Current Build)",
                field: "name",
                cellTemplate: this.commonGridColumns.downloadableColumn('status-{{(row.entity.status).toLowerCase()}}'),
                width: '20%',
                actions: {
                    download: {
                        callback: row => this.downloadArtifact(row),
                        visibleWhen: row => _.findWhere(row.actions, {name: "Download"})
                    }
                }
            },
            {
                name: "Id",
                field: "prevName",
                headerCellTemplate: headerCellDefaultTemplate.replace('{{ col.displayName CUSTOM_FILTERS }}','Id (Build #{{grid.appScope.jfDiff.selectedBuildNumber.buildNumber}})'),
                width: '20%'
            },
            {
                name: "Status",
                field: "status",
                headerCellTemplate: headerCellGroupingTemplate,
                sort: {direction: this.uiGridConstants.DESC},
                grouped: true,
                width: '10%'
            },
            {
                name: "Module",
                field: "moduleName",
                headerCellTemplate: headerCellGroupingTemplate,
                grouped: true,
                width: '20%'
            },
            {
                name: "Repo path",
                field: "downloadLink",
                cellTemplate: this.commonGridColumns.repoPathColumn('status-{{(row.entity.status).toLowerCase()}}'),
                width: '30%',
                customActions: [{
                    icon: 'icon icon-show-in-tree',
                    tooltip: 'Show In Tree',
                    callback: row => this.showInTree(row),
                    visibleWhen: row => _.findWhere(row.actions, {name: "ShowInTree"})
                }]
            }
        ])
    }

    _getEnvVarsColumns() {
        return this._addCellTemplates([
            {
                name: "Current Key",
                field: 'key',
                cellTemplate: '<div class="ui-grid-cell-contents status-{{(row.entity.status).toLowerCase()}}">{{row.entity.key}}</div>',
                width: '20%'
            },
            {
                name: "Current Value",
                field: 'value',
                cellTemplate: '<div class="ui-grid-cell-contents status-{{(row.entity.status).toLowerCase()}}">{{row.entity.value}}</div>',
                width: '20%'
            },
            {
                name: "Prev Key",
                field: 'prevKey',
                cellTemplate: '<div class="ui-grid-cell-contents status-{{(row.entity.status).toLowerCase()}}">{{row.entity.prevKey}}</div>',
                headerCellTemplate: headerCellDefaultTemplate.replace('{{ col.displayName CUSTOM_FILTERS }}','Build#{{grid.appScope.jfDiff.selectedBuildNumber.buildNumber}} Key'),
                width: '20%'
            },
            {
                name: "Prev Value",
                field: 'prevValue',
                cellTemplate: '<div class="ui-grid-cell-contents status-{{(row.entity.status).toLowerCase()}}">{{row.entity.prevValue}}</div>',
                headerCellTemplate: headerCellDefaultTemplate.replace('{{ col.displayName CUSTOM_FILTERS }}','Build#{{grid.appScope.jfDiff.selectedBuildNumber.buildNumber}} Value'),
                width: '20%'
            },
            {
                name: "Status",
                field: "status",
                sort: {direction: this.uiGridConstants.DESC},
                headerCellTemplate: headerCellGroupingTemplate,
                grouped: true,
                width: '20%'
            }
        ])
    }
}

export function jfDiff() {
    return {
        restrict: 'EA',
        controller: jfDiffController,
        controllerAs: 'jfDiff',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/builds_info/build_tabs/jf_diff.html'
    }
}