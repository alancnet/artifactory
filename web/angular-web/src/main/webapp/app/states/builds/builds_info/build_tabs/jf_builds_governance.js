import DICTIONARY from './../../constants/builds.constants';

let headerCellGroupingTemplate = require("raw!../../../../ui_components/artifactory_grid/templates/headerCellTemplate.html");

class jfBuildsGovernanceController {
    constructor($scope, $state, $stateParams, $window, uiGridConstants, BuildsDao, ArtifactoryGridFactory,
            ArtifactoryNotifications, ArtifactoryStorage) {
        this.$scope = $scope;
        this.$state = $state;
        this.$stateParams = $stateParams;
        this.$window = $window;
        this.uiGridConstants = uiGridConstants;
        this.buildsDao = BuildsDao;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.artifactoryStorage = ArtifactoryStorage;
        this.DICTIONARY = DICTIONARY.governance.codeCenterApp;
        this.componentsGridOptions = {};
        this.vulnerabilitiesGridOptions = {};
        this.extendedGridOptions = {};

        this.allData = {};
        this.gotData = false;
        this.noData = true;
        this.filteredData = {};
        this.vulnerabilitiesData = [];
        this.includePublished = true;
        this.scopesOptions = {};
        this.scopesOptionsCopy = {};

        this._createGrids();

        this._getGovernanceData();

    }

    showInTree(row) {
        let browser = this.artifactoryStorage.getItem('BROWSER') || 'tree';
        if (browser === 'stash') browser = 'tree';
        let path = row.repoKey + '/' + row.path;
        this.$state.go('artifacts.browsers.path', {
            tab: "General",
            artifact: path,
            browser: browser
        });
    }

    showRequest(row) {
        this.$window.open(row.requestLink, "_blank", "");
    }

    updateRequest(row) {

        let objToSend = {
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            components: [row]
        };

        this.buildsDao.updateGovernanceRequest(objToSend).$promise.then((data)=> {
            //console.log(data);
        });

    }


    onIncludeByScopes() {
        if (this.includeByScopes) {
            angular.copy(this.scopesOptionsCopy, this.scopesOptions);
        }
        else {
            angular.copy(this.scopesOptions, this.scopesOptionsCopy);
            for (let key in this.scopesOptions) {
                this.scopesOptions[key] = true;
            }
        }
        this._filterData();

    }

    onScopeOptionChange() {
        this._filterData();
    }

    _filterData() {

        this.vulnerabilitiesData = [];

        let relevantData = this.allData.components;

        if (this.includePublished) {
            relevantData = relevantData.concat(this.allData.publishedArtifacts);
        }

        if (this.includeByScopes) {
            this.filteredData = _.filter(relevantData, (component) => {
                {
                    let ret = false;
                    if (component.scopes.length) {
                        for (let key in this.scopesOptions) {
                            if (this.scopesOptions[key] && (component.scopes.indexOf(key) !== -1)) {
                                ret = true;
                                break;
                            }
                        }
                    }
                    else {
                        ret = true;
                    }
                    return ret;
                }
            });
        }
        else {
            this.filteredData = relevantData;
        }

        for (let index in this.filteredData) {
            let vulnerabilities = this.filteredData[index].vulnerabilities;
            if (vulnerabilities.length) {
                vulnerabilities = _.map(vulnerabilities, (v)=> {
                    v.artifactId = this.filteredData[index].componentId;
                    return v;
                });
                this.vulnerabilitiesData = this.vulnerabilitiesData.concat(vulnerabilities);
            }
        }

        this.componentsGridOptions.setGridData(this.filteredData);

        this.vulnerabilitiesGridOptions.setGridData(this.vulnerabilitiesData);

        this.componentsSummary = this._getSummaries(this.filteredData, 'status');
        this.vulnerabilitiesSummary = this._getSummaries(this.vulnerabilitiesData, 'severity');
    }


    _getGovernanceData() {
        this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'buildGovernance'
        }).$promise.then((data) => {
//                    console.log(data);

                    this.gotData = true;
                    if (data.feedbackMsg && data.feedbackMsg.warn) {
                        this.artifactoryNotifications.create({error: data.feedbackMsg.warn});
                    }

                    _.map(data.scopes, (scope)=> {
                        this.scopesOptions[scope] = true;
                    });
                    angular.copy(this.scopesOptions, this.scopesOptionsCopy);
                    this.allData = data;

                    this._filterData();

                    this.codeCenterApp = data.applicationInfo;

                }).catch((err)=> {
                    console.log('err',err);
                    this.noData = true;
                    if (err.data && err.data.error) {
                        this.artifactoryNotifications.create({error: err.data.error});
                    }
                });


    }

    _getSummaries(data, field) {
        let sums = {'Total': 0};
        for (let i in data) {
            let record = data[i];
            if (sums[record[field]]) {
                sums[record[field]]++;
            }
            else {
                sums[record[field]] = 1;
            }
            sums['Total']++;
        }
        return sums;
    }

    _createGrids() {
        this.componentsGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getComponentsColumns())
                .setRowTemplate('default')
        this.vulnerabilitiesGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getVulnerabilitiesColumns())
                .setRowTemplate('default')
    }

    _getComponentsColumns() {

        let idCellTemplate = '<div ng-if="row.entity.status !== \'Stale\'" class="ui-grid-cell-contents"><a href="" ng-click="grid.appScope.jfBuildsGovernance.showRequest(row.entity)">{{row.entity.componentName}}</a></div>' +
            '<div ng-if="row.entity.status === \'Stale\'" class="ui-grid-cell-contents"><a href="" ng-click="grid.appScope.jfBuildsGovernance.showRequest(row.entity)">{{row.entity.componentName}}</a> <span class="not-in-build">(Not in build)<span></span></div>';

        let scopesCellTemplate = '<div class="ui-grid-cell-contents">{{row.entity.scopes.join(", ")}}</div>';
        let statusCellTemplate = '<div class="ui-grid-cell-contents"' +
                'ng-class="{\'status-pending\': row.entity.status===\'Pending\',' +
                '\'status-approved\': row.entity.status===\'Approved\'}"' +
                '>{{row.entity.status}}</div>';

        return [
            {
                displayName: "Artifact ID",
                field: "componentName",
                width: '20%',
                cellTemplate: idCellTemplate,
                customActions: [{
                    icon: 'icon icon-blackduck',
                    tooltip: 'Go To Blackduck',
                    callback: row => this.showRequest(row),
                    visibleWhen: row => _.contains(row.actions, 'ShowRequest')
                }]
            },
            {
                name: "Licenses",
                field: "license",
                headerCellTemplate: headerCellGroupingTemplate,
                grouped: true,
                width: '15%'
            },
            {
                name: "Status",
                field: "status",
                cellTemplate: statusCellTemplate,
                headerCellTemplate: headerCellGroupingTemplate,
                sort: {direction: this.uiGridConstants.ASC},
                grouped: true,
                width: '10%'
            },
            {
                name: "Scopes",
                field: "scopes",
                cellTemplate: scopesCellTemplate,
                headerCellTemplate: headerCellGroupingTemplate,
                grouped: true,
                width: '15%'
            },
            {
                name: "Repo Path",
                field: "path",
                width: '40%',
                customActions: [
                    {
                        icon: 'icon icon-show-in-tree',
                        tooltip: 'Show In Tree',
                        callback: row => this.showInTree(row),
                        visibleWhen: row => _.contains(row.actions, 'ShowInTree') && row.repoKey != ""
                    }, {
                        icon: 'icon icon-refresh',
                        tooltip: 'Update Request',
                        callback: row => this.updateRequest(row),
                        visibleWhen: row => _.contains(row.actions, 'UpdateRequest')
                    }
                ]
            }
        ];
    }

    _getVulnerabilitiesColumns() {

        let vulnerabilityCellTemplate = '<div class="ui-grid-cell-contents"><a target="_blank" ng-href="{{row.entity.link}}">{{row.entity.name}}</a></div>';

        return [
            {
                displayName: "Artifact ID",
                field: "artifactId",
                headerCellTemplate: headerCellGroupingTemplate,
                sort: {direction: this.uiGridConstants.ASC},
                grouped: true,
                width: '25%'
            },
            {
                name: "Vulnerability",
                cellTemplate: vulnerabilityCellTemplate,
                field: "name",
                width: '10%'
            },
            {
                name: "Severity",
                field: "severity",
                headerCellTemplate: headerCellGroupingTemplate,
                grouped: true,
                width: '10%'
            },
            {
                name: "Description",
                field: "description",
                width: '55%'
            }
        ];
    }

    hasAppInfo() {
        return this.codeCenterApp && Object.keys(this.codeCenterApp).length > 0;
    }
}


export function jfBuildsGovernance() {
    return {

        restrict: 'EA',
        controller: jfBuildsGovernanceController,
        controllerAs: 'jfBuildsGovernance',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/builds_info/build_tabs/jf_builds_governance.html'
    }
}