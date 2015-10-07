import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class CrowdIntegrationController {
    constructor($scope, CrowdIntegrationDao, ArtifactoryGridFactory, commonGridColumns, uiGridConstants) {
        this.crowdIntegrationDao = CrowdIntegrationDao;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.$scope = $scope;
        this.crowdGroupsOptions = null;
        this.crowd = {};
        this.groupsData = [];
        this.commonGridColumns = commonGridColumns;
        this.uiGridConstants = uiGridConstants;
        this._createGrid();
        this.crowdGroupsError = null;
        this.batchActions = this._getBatchActions();
        this.initCrowd();
        this.usernameFilter = '';
        this.filterType = 'User Name';
        this.TOOLTIP = TOOLTIP.admin.security.crowd_integration;
    }

    importCrowd(row) {
        row.importIntoArtifactory = true;
        this.crowdIntegrationDao.import({
            action: 'import'
        }, [row]).$promise.then((data)=> {
                    this.initCrowd();
                });
    }

    importCrowds() {
        let selectedCrowds = this.crowdGroupsOptions.api.selection.getSelectedRows();
        selectedCrowds.forEach(row=> {
            row.importIntoArtifactory = true
        });
        this.crowdIntegrationDao.import({
            action: 'import'
        }, selectedCrowds).$promise
            .then((data)=> {
                    this.initCrowd();
            });
    }

    _getBatchActions() {
        return [
            {
                icon: 'icon icon-import',
                name: 'Import',
                callback: () => this.importCrowds()
            }
        ]
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-import',
                tooltip: 'Import',
                callback: row => this.importCrowd(row)
            }
        ]
    }


    initCrowd() {
        this.loadFromServer().then(() => {
            if (!this.initialized) {
                this.initialized = true;
            }
            else this.getCrowdGroups()

        });
    }

    loadFromServer(retainEnabled = false) {
        return this.crowdIntegrationDao.get().$promise.then((data)=> {
            // Keep enabled
            if (retainEnabled && this.crowd) data.enableIntegration = this.crowd.enableIntegration;
            this.crowd = data;
        });
    }

    getCrowdGroups() {
        if (this.crowd.enableIntegration && this.crowd.serverUrl && this.crowd.applicationName) {
            this.crowdIntegrationDao.refresh({name: this.usernameFilter}, {
                serverUrl: this.crowd.serverUrl,
                applicationName: this.crowd.applicationName,
                enableIntegration: this.crowd.enableIntegration,
                noAutoUserCreation: this.crowd.noAutoUserCreation,
                useDefaultProxy: this.crowd.useDefaultProxy,
                directAuthentication: this.crowd.directAuthentication,
                sessionValidationInterval: this.crowd.sessionValidationInterval,
                password: this.crowd.password
            }).$promise
                .then((result)=> {
                        this.groupsData = result.data.crowdGroupModels;
                        this.crowdGroupsOptions.setGridData(this.groupsData);
                })
                .catch((result)=> {
                        console.log("exception")
                    this.crowdGroupsError = result.data.error;
                    this.crowdGroupsOptions.setGridData([]);
                })
        }
        else {
            this.crowdGroupsError = null;
            if (this.crowdGroupsOptions) this.crowdGroupsOptions.setGridData([]);
        }
    }

    changeFilter() {
        this.usernameFilter = '';
        this.getCrowdGroups();
    }
    isFilteringBy(filterType) {
        return this.filterType == filterType;
    }

    _createGrid() {
        this.crowdGroupsOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getColumns())
            .setRowTemplate('default')
            .setMultiSelect()
            .setBatchActions(this._getBatchActions())
            .setButtons(this._getActions())
            .setGridData(this.groupsData)
    }

    _getColumns() {
        return [
            {
                name: 'Group Name',
                displayName: 'Group Name',
                field: 'groupName',
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                width: '45%'
            },
            {
                name: 'Description',
                displayName: 'Description',
                field: 'description',
                width: '45%'
            },
            {
                name: 'Synced',
                displayName: 'Synced',
                field: 'existsInArtifactory',
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.existsInArtifactory'),
                width: '10%'
            }/*,
            {
                name: 'Import',
                field: 'importIntoArtifactory'
            }*/
        ]
    }

    saveCrowd() {
        this.crowdIntegrationDao.update(this.crowd)
                .$promise.then(() => this.getCrowdGroups());
    }

    testCrowd() {
        this.crowd.action = 'test';
        this.crowdIntegrationDao.test({
            action: 'test',
            serverUrl: this.crowd.serverUrl,
            applicationName: this.crowd.applicationName,
            enableIntegration: this.crowd.enableIntegration,
            noAutoUserCreation: this.crowd.noAutoUserCreation,
            useDefaultProxy: this.crowd.useDefaultProxy,
            directAuthentication: this.crowd.directAuthentication,
            sessionValidationInterval: this.crowd.sessionValidationInterval,
            password: this.crowd.password

        }).$promise.then((data)=>{});
    }
}