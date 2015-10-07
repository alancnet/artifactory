import TOOLTIP from '../../../../constants/artifact_tooltip.constant';
let headerCellGroupingTemplate = require("raw!../../../../ui_components/artifactory_grid/templates/headerCellTemplate.html");


class jfLicensesController {
    constructor($scope, $state, $window, $stateParams, BuildsDao, ArtifactPropertyDao,
            ArtifactoryGridFactory, ArtifactoryModal, ArtifactoryState, artifactoryDownload, commonGridColumns,
            ArtifactActionsDao, ArtifactoryStorage) {
        this.TOOLTIP = TOOLTIP.builds;
        this.$stateParams = $stateParams;
        this.$state = $state;
        this.$scope = $scope;
        this.$window = $window;
        this.buildsDao = BuildsDao;
        this.propsDao = ArtifactPropertyDao.getInstance();
        this.artifactActionsDao = ArtifactActionsDao;
        this.modal = ArtifactoryModal;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactoryStorage = ArtifactoryStorage;
        this.download = artifactoryDownload;
        this.artifactoryState = ArtifactoryState;
        this.gridOptions = {};
        this.commonGridColumns = commonGridColumns;
        this.extendedGridOptions = {};


        this.modalInstance = null;

        this.includePublished = false;

        this.allData = {};

        this.filteredData = {};

        this.scopesOptions = {}
        this.scopesOptionsCopy = {};

        this.showExtendedGrid = false;

        this.canOverride = true;

        this._createGrids();

        this._getLicensesData();

        this.toOverride = [];

    }

    getFilteredData() {
        let data = _.map(angular.copy(this.filteredData),(obj)=>{
            delete(obj.$$hashKey);
            return obj;
        });

        return JSON.stringify({licenses: data});
    }

    downloadArtifact(row) {
        let params = {action: 'download'};
        this.artifactActionsDao.perform(params, {
            path: row.path,
            repoKey: row.repoKey
        }).$promise.then((response) => {
                    this.download(response.data.path)
                });
    }

    showInTree(row) {
        let browser = this.artifactoryStorage.getItem('BROWSER') || 'tree';
        if (browser === 'stash') browser = 'tree';
        let path = row.repoKey + '/' + row.path;
        this.$state.go('artifacts.browsers.path',{
            tab: "General",
            artifact: path,
            browser: browser
        });
    }

    editLicense(row) {
        let prevState = {state: this.$state.current.name, params: angular.copy(this.$stateParams)};
        this.artifactoryState.setState('prevState', prevState);
        this.$state.go('admin.configuration.editLicense',{licenseName: row.license.name});
    }

    changeLicense(row) {
        this._getLicensesPredefineValues(row).then((data) => {
            let modalScope = this.$scope.$new();
            modalScope.saveLicenses = (licenses) => {
                this.propsDao.update({
                        repoKey: row.repoKey,
                        path: row.path
                    },
                    {
                        parent: {name: 'artifactory'},
                        property: {name: 'licenses'},
                        selectedValues: licenses
                    })
                    .$promise.then((res)=>{
                        //console.log(res);
                        this.modalInstance.close();
                        this._getLicensesData();
                    })
            };
            modalScope.closeModal = () => this.modalInstance.close();

            if (this.showExtendedGrid && row.extractedLicense.name !== 'Not Found') {
                modalScope.foundLicense = row.extractedLicense.name;

                if (row.extractedLicense.found && row.extractedLicense.name !== row.license.name)
                    modalScope.foundLicenseClass = row.extractedLicense.approved ? 'license-approved' : 'license-unapproved';
                else if (!row.extractedLicense.found || row.extractedLicense.name === row.license.name)
                    modalScope.foundLicenseClass = 'license-found-same-or-notfound';

                if (row.overridable) {
                    modalScope.overridable = true;
                    modalScope.override = () => {
                        modalScope.selectedLicenses = [row.extractedLicense.name];
                    }
                }
            }
            modalScope.modalTitle = "Edit 'artifactory.licenses' Property";
            modalScope.licenses = data.predefinedValues;
            modalScope.selectedLicenses = data.selectedValues;
            this.modalInstance = this.modal.launchModal('add_license_modal', modalScope);
        });
    }

    autoLink() {
        this.showExtendedGrid = true;
        this._getLicensesData();
    }

    overrideSelected() {
        if (this.toOverride.length) {
            let requestObject = {
                name: this.$stateParams.buildName,
                number: this.$stateParams.buildNumber,
                time: this.$stateParams.startTime,
                licenses: this.toOverride
            };

            this.buildsDao.overrideLicenses(requestObject).$promise.then((data) => {
                this.cancel();
            });
        }
    }

    cancel() {
        this.showExtendedGrid = false;
        this._getLicensesData();
    }

    onIncludePublish() {
        this._filterData();
    }

    onIncludeByScopes() {
        if (this.includeByScopes) {
            angular.copy(this.scopesOptionsCopy,this.scopesOptions);
        }
        else {
            angular.copy(this.scopesOptions,this.scopesOptionsCopy);
            for (let key in this.scopesOptions) {
                this.scopesOptions[key] = true;
            }
        }
        this._filterData();
    }

    onScopeOptionChange() {
        this._filterData();
    }

    _getLicensesPredefineValues(row) {
        return this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            autoFind: this.showExtendedGrid,
            id: row.id,
            repoKey: row.repoKey,
            path: row.path,
            action: 'changeLicenses'
        }).$promise;
    }

    _getLicensesData() {
        this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            autoFind: this.showExtendedGrid,
            action: 'buildLicenses'
        }).$promise.then((data) => {
                //console.log(data);
                _.map(data.scopes, (scope)=> {
                    this.scopesOptions[scope] = true;
                });
                angular.copy(this.scopesOptions,this.scopesOptionsCopy);
                this.allData = data;
                this._filterData();
            });
    }

    _filterData() {
        let relevantData = this.allData.licenses;

        if (this.includePublished) {
            relevantData = relevantData.concat(this.allData.publishModules);
        }

        this.filteredData = _.filter(relevantData,(license) => {
            if (!this.includeByScopes) return true;
            else {
                let ret = false;
                for (let key in this.scopesOptions) {
                    if (this.scopesOptions[key] && license.scopeNames.includes(key)) {
                        ret = true;
                        break;
                    }
                }
                return ret;
            }
        });
        if (this.showExtendedGrid) {
            this.extendedGridOptions.setGridData(this.filteredData);
        }
        else {
            this.gridOptions.setGridData(this.filteredData);
        }

        this._calculateSummary();
    }

    _calculateSummary() {
        this.summary = {
            notApproved: _.filter(this.filteredData, (license) => {return license.license.found && !license.license.approved}).length,
            notFound: _.filter(this.filteredData, (license) => {return license.license.notFound}).length,
            unknown: _.filter(this.filteredData, (license) => {return license.license.unknown}).length,
            neutral: _.filter(this.filteredData, (license) => {return license.license.notSearched}).length,
            approved: _.filter(this.filteredData, (license) => {return license.license.approved}).length,
        };

        this.summary.ok = this.summary.notApproved === 0 && this.summary.notFound === 0 && this.summary.unknown === 0;
    }

    _createGrids() {
        this.gridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getColumns())
            .setRowTemplate('default')
        this.extendedGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getExtendedColumns())
            .setRowTemplate('default')
    }

    _getColumns() {
        let nameCellTemplate = '<div ng-if="row.entity.repoKey" class="ui-grid-cell-contents">{{row.entity.id}}</div>' +
                               '<div ng-if="!row.entity.repoKey" class="ui-grid-cell-contents">{{row.entity.id}}</div>';

        let licenseCellTemplate = '<div class="ui-grid-cell-contents"' +
                'ng-class="{\'license-unapproved\': row.entity.license.found && !row.entity.license.approved,' +
                '\'license-approved\': row.entity.license.approved,'+
                '\'license-notfound\': row.entity.license.notFound,' +
                '\'license-neutral\': row.entity.license.notSearched}"'+
                '><div ng-if="row.groupHeader">{{row.entity[\'license.name\']}}</div>' +
                '<div ng-if="!row.groupHeader">' +
                '<span ng-if="row.entity.actions.indexOf(\'ChangeLicense\') !== -1"><a href="" ng-click="grid.appScope.jfLicenses.changeLicense(row.entity)">{{row.entity.license.name}}</a></span>' +
                '<span ng-if="row.entity.actions.indexOf(\'ChangeLicense\') === -1">{{row.entity.license.name}}</span>' +
                '</div></div>';
        return [
            {
                name: "Artifact ID",
                displayName: "Artifact ID",
                field: "id",
                cellTemplate: nameCellTemplate,
                width: '25%',
                actions: {
                    download: {
                        callback: row => this.downloadArtifact(row),
                        visibleWhen: row => row.actions.indexOf('Download') !== -1
                    }
                }
            },
            {
                name: "Scopes",
                displayName: "Scopes",
                field: "scopeNames",
                headerCellTemplate: headerCellGroupingTemplate,
                grouped: true,
                width: '15%'
            },
            {
                name: "Repo Path",
                displayName: "Repo Path",
                cellTemplate: this.commonGridColumns.repoPathColumn(),
                field: "path",
                width: '50%',
                customActions: [{
                    icon: 'icon icon-show-in-tree',
                    tooltip: 'Show In Tree',
                    callback: row => this.showInTree(row),
                    visibleWhen: row => row.actions.indexOf('ShowInTree') !== -1
                }]
            },
            {
                name: "License",
                displayName: "License",
                field: "license.name",
                headerCellTemplate: headerCellGroupingTemplate,
                cellTemplate: licenseCellTemplate,
                grouped: true,
                width: '10%'
            }
        ];
    }

    _getExtendedColumns() {
        let nameCellTemplate = '<div ng-if="row.entity.repoKey" class="ui-grid-cell-contents">{{row.entity.id}}</div>' +
                '<div ng-if="!row.entity.repoKey" class="ui-grid-cell-contents">{{row.entity.id}}</div>';

        let licenseCellTemplate = '<div class="ui-grid-cell-contents"' +
                'ng-class="{\'license-unapproved\': row.entity.license.found && !row.entity.license.approved,' +
                '\'license-approved\': row.entity.license.approved,'+
                '\'license-notfound\': row.entity.license.notFound}"'+
                '><div ng-if="row.groupHeader">{{row.entity[\'license.name\']}}</div>' +
                '<div ng-if="!row.groupHeader">' +
                '<span ng-if="row.entity.actions.indexOf(\'ChangeLicense\') !== -1"><a href="" ng-click="grid.appScope.jfLicenses.changeLicense(row.entity)">{{row.entity.license.name}}</a></span>' +
                '<span ng-if="row.entity.actions.indexOf(\'ChangeLicense\') === -1">{{row.entity.license.name}}</span>' +
                '</div></div>';

        let foundLicenseCellTemplate = '<div class="ui-grid-cell-contents"' +
            'ng-class="{\'license-approved\': row.entity.extractedLicense.found && row.entity.extractedLicense.name !== row.entity.license.name && row.entity.extractedLicense.approved,' +
                '\'license-unapproved\': row.entity.extractedLicense.found && row.entity.extractedLicense.name !== row.entity.license.name && !row.entity.extractedLicense.approved,' +
                '\'license-found-same-or-notfound\': !row.entity.extractedLicense.found || row.entity.extractedLicense.name === row.entity.license.name}"' +
                '>{{row.entity.extractedLicense.name}}</div>';

        return [
            {
                name: "Artifact ID",
                displayName: "Artifact ID",
                field: "id",
                cellTemplate: nameCellTemplate,
                width: '25%',
                actions: {
                    download: {
                        callback: row => this.downloadArtifact(row),
                        visibleWhen: row => row.actions.indexOf('Download') !== -1
                    }
                }
            },
            {
                name: "Scopes",
                field: "scopeNames",
                headerCellTemplate: headerCellGroupingTemplate,
                grouped: true
            },
            {
                name: "Repo Path",
                cellTemplate: this.commonGridColumns.repoPathColumn(),
                field: "path",
                customActions: [{
                    icon: 'icon icon-show-in-tree',
                    tooltip: 'Show In Tree',
                    callback: row => this.showInTree(row),
                    visibleWhen: row => row.actions.indexOf('ShowInTree') !== -1
                }]
            },
            {
                name: "License",
                field: "license.name",
                headerCellTemplate: headerCellGroupingTemplate,
                cellTemplate: licenseCellTemplate,
                grouped: true
            },
            {
                name: "Found Licenses",
                field: "extractedLicense.name",
                cellTemplate: foundLicenseCellTemplate
            },
            {
                name: "Override",
                field: "selected",
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.selected', 'grid.appScope.jfLicenses.setSelected(row.entity)', '!row.entity.overridable')
            }

        ];
    }

    setSelected(row) {
        if (row.selected) {
            this.toOverride.push(row);
        }
        else {
            let index = this.toOverride.indexOf(row);
            if (index != -1) {
                this.toOverride.splice(index,1);
            }
        }
    }

    hasScopesOptions() {
        return Object.keys(this.scopesOptions).length > 0;
    }
}

export function jfLicenses() {
    return {
        restrict: 'EA',
        controller: jfLicensesController,
        controllerAs: 'jfLicenses',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/builds_info/build_tabs/jf_licenses.html'
    }
}