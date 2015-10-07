
class jfIssuesController {
    constructor($scope, $stateParams, BuildsDao, ArtifactoryGridFactory, uiGridConstants, commonGridColumns) {
        this.$stateParams = $stateParams;
        this.$scope = $scope;
        this.uiGridConstants=uiGridConstants;
        this.buildsDao = BuildsDao;
        this.gridOptions = {};
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.commonGridColumns = commonGridColumns;

        this.noData = false;

        this._createGrid();
        this._getIssuesData();
    }

    _getIssuesData() {
        this.buildsDao.getDataArray({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'buildIssues'
        }).$promise.then((data) => {
                if (data.length) {
                    this.gridOptions.setGridData(data);
                }
                else {
                    this.noData = true;
                }

            }).catch(() => {
                this.noData = true;
                this.gridOptions.setGridData([]);
        });
    }

    _createGrid() {

        this.gridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getColumns())
            .setRowTemplate('default')
            .setButtons(this._getActions());

    }

    _getColumns() {
        let cellTemplate = '<div class="ui-grid-cell-contents"><a target="_blank" ng-href="{{row.entity.url}}">{{row.entity.key}}</a></div>';

        return [
            {
                name: "Key",
                displayName: "Key",
                field: "key",
                cellTemplate: cellTemplate,
            },
            {
                name: "Summary",
                displayName: "Summary",
                field: "summary"
            },
            {
                name: "Previous Build",
                displayName: "Previous Build",
                field: "aggregated",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.aggregated'),
                sort: {
                    direction: this.uiGridConstants.ASC
                }
            }

        ]
    }

    _getActions() {
        return [
        ];
    }

}


export function jfIssues() {
    return {
        restrict: 'EA',
        controller: jfIssuesController,
        controllerAs: 'jfIssues',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/builds_info/build_tabs/jf_issues.html'
    }
}