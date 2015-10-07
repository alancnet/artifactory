import DICTIONARY from './../../constants/builds.constants';

class jfEnvironmentController {
    constructor($scope, BuildsDao, ArtifactoryGridFactory, $stateParams, uiGridConstants) {
        this.$scope = $scope;
        this.$stateParams = $stateParams;
        this.buildsDao = BuildsDao;
        this.uiGridConstants = uiGridConstants;
        this.environmentGridOptions = {};
        this.systemGridOptions = {};
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.DICTIONARY = DICTIONARY.generalInfo;
        this._getEnvironmentData();
        this._createGrids();
    }

    _getEnvironmentData() {
        this._getEnvVars();
        this._getSysVars();
    }

    _getEnvVars() {
        return this.buildsDao.getData({
//            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'buildProps',
            subAction: 'env',
            orderBy: 'key',
            numOfRows: 25,
            pageNum: 1,
            direction: 'asc'
        }).$promise.then((data) => {
                if (data.pagingData) this.environmentGridOptions.setGridData(data.pagingData);
            });
    }

    _getSysVars() {
        return this.buildsDao.getData({
//            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'buildProps',
            subAction: 'system',
            orderBy: 'key',
            numOfRows: 25,
            pageNum: 1,
            direction: 'asc'
        }).$promise.then((data) => {
                if (data.pagingData) this.systemGridOptions.setGridData(data.pagingData);
            });
    }

    _createGrids() {

        this.environmentGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getColumns())
            .setRowTemplate('default')
            .setGridData([]);

        this.systemGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getColumns())
            .setRowTemplate('default')
            .setGridData([]);
    }

    _getColumns() {
        return [
            {
                name: "Key",
                displayName: "Key",
                field: "key",
                sort: {
                    direction: this.uiGridConstants.ASC
                }
            },
            {
                name: "Value",
                displayName: "Value",
                field: "value"
            }
        ]
    }
}

export function jfEnvironment() {
    return {
        restrict: 'EA',
        controller: jfEnvironmentController,
        controllerAs: 'jfEnvironment',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/builds_info/build_tabs/jf_environment.html'
    }
}