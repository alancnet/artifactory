import EVENTS from '../../../../constants/artifacts_events.constants';
let headerCellGroupingTemplate = require("raw!../../../../ui_components/artifactory_grid/templates/headerCellTemplate.html");

class jfBuildsController {
    constructor($state, ArtifactoryGridFactory, ArtifactBuildsDao, $scope, ArtifactoryEventBus, ArtifactoryModal, uiGridConstants) {
        this.uiGridConstants = uiGridConstants;
        this.producedByGridOptions = {};
        this.usedByGridOptions = {};
        this.$state = $state;
        this.buildsDao = ArtifactBuildsDao.getInstance();
        this.$scope = $scope;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.modal = ArtifactoryModal;
        this.mode = 'ProducedBy';
        this.builds = {};
        this._registerEvents();
        this._createGrids();
        this._getBuildData();
    }

    downloadJson(build) {
        this.buildsDao.getJson({
            buildNumber: build.number,
            buildName: build.name,
            startTime: build.started
        })
            .$promise.then((result) => {
                this.modal.launchCodeModal('Build #' + build.number, result.json,
                    {name: "javascript", json: true});
            });
    }

    setMode(mode) {
        this.mode = mode;
    }

    isSelected(mode) {
        return this.mode === mode;
    }


    _getBuildData() {
        // if the node does not have a path the build cannot be loaded
        // this may occur in navigation to a node that does not have a path (repo node)
        if (!this.currentNode.data.path) {
            return;
        }

        this.buildsDao.query({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey
        }).$promise.then((builds) => {
                    this.builds = builds;
                this.producedByGridOptions.setGridData(builds.producedBy);
                this.usedByGridOptions.setGridData(builds.usedBy);
            });
    }

    _registerEvents() {
        let self = this;

        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode !== node) {
                this.currentNode = node;
                self._getBuildData();
            }
        });
    }

    _createGrids() {
        this.producedByGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getProducedByColumns())
            .setRowTemplate('default')
            .setButtons(this._getActions());
        this.usedByGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getUsedByColumns())
            .setRowTemplate('default')
            .setButtons(this._getActions());
    }

    _getProducedByColumns() {
        let columns = this._getCommonColumns();
        columns.splice(4, 0, {
                displayName: 'Started At',
                name: 'Started At',
                cellTemplate: '<div class="ui-grid-cell-contents">{{ row.entity.startedString }}</a>',
                field: "started",
                type: 'number'
            });
        return columns;
    }

    _getCommonColumns() {
        return [{
            displayName: 'Project Name',
            sort: {
                direction: this.uiGridConstants.ASC
            },
            name: 'Project Name',
            headerCellTemplate: headerCellGroupingTemplate,
            grouped: true,
            field: "name"
        }, {
            displayName: 'Build ID',
            name: 'Build ID',
            field: "number",
            cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="builds.info({buildName:row.entity.name,buildNumber:row.entity.number,tab:\'general\',startTime:row.entity.started})" >{{row.entity.number}}</a></div>'
        }, {
            name: 'Module ID',
            displayName: 'Module ID',
            headerCellTemplate: headerCellGroupingTemplate,
            grouped: true,
            field: "moduleID",
            cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="builds.info({buildName:row.entity.name,buildNumber:row.entity.number,tab:\'published\',startTime:row.entity.started,moduleID:row.entity.moduleID})" >{{row.entity.moduleID}}</a></div>'
        }, {
            displayName: 'CI Server',
            name: 'CI Server',
            headerCellTemplate: headerCellGroupingTemplate,
            grouped: true,
            field: "ciUrl",
            cellTemplate: '<div class="ui-grid-cell-contents"><a ng-href="{{row.entity.ciUrl}}" target="_blank">{{row.entity.ciUrl}}</a></div>'
        }];
    }

    _getUsedByColumns() {
        let columns = this._getCommonColumns();
        columns.splice(3, 0, {
            displayName: 'Scope',
            name: 'Scope',
            headerCellTemplate: headerCellGroupingTemplate,
            grouped: true,
            field: "scope"
        });
        return columns;
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-view',
                tooltip: 'View Build JSON',
                callback: row => this.downloadJson(row)
            }

        ];
    }
}

export function jfBuilds() {
    return {
        restrict: 'EA',
        controller: jfBuildsController,
        controllerAs: 'jfBuilds',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_builds.html'
    }
}

