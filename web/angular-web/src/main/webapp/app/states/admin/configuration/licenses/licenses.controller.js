import API from '../../../../constants/api.constants';

export class AdminConfigurationLicensesController {

    constructor($scope, $window, ArtifactoryModal, LicensesDao, ArtifactoryGridFactory, ArtifactoryState, uiGridConstants) {
        this.$window = $window;
        this.gridOption = {};
        this.uiGridConstants = uiGridConstants;
        this.licensesDao = LicensesDao;
        this.$scope=$scope;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.modal = ArtifactoryModal;

        this._createGrid();
        this._initLicenses();


        //clear 'prevState' in ArtifactoryState, used to return from license form to another state (Builds->Licenses)
        ArtifactoryState.setState('prevState', undefined);
    }

    _initLicenses() {
        this.licensesDao.getLicense().$promise.then((licenses)=> {
            this.licenses = licenses;
            this.gridOption.setGridData(this.licenses.data)
        });
    }

    _createGrid() {
        this.gridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getCloumns())
                .setRowTemplate('default')
                .setMultiSelect()
                .setButtons(this._getActions())
                .setBatchActions(this._getBatchActions());
    }

    deleteLicense(license) {
        let json = {licenseskeys: [license.name]}
        this.modal.confirm(`Are you sure you want to delete ${license.name}?`)
                .then(()=> {
                    this.licensesDao.delete(json).$promise.then(()=>this.updateListTable());
                });
    }

    deleteSelectedLicenses() {
        //Get All selected licenses
        let selectedRows = this.gridOption.api.selection.getSelectedGridRows();
        //Create an array of the selected licenses keys
        let names = _.map(selectedRows, (row) => {return row.entity.name});
        //Create Json for the bulk request
        let json = {licenseskeys: names};
        //console.log('Bulk delete....');
        //Ask for confirmation before delete and if confirmed then delete bulk of licenses
        this.modal.confirm(`Are you sure you want to delete ${selectedRows.length} licenses?`)
                .then(()=> {
                    this.licensesDao.delete(json).$promise.then(() => this.updateListTable());
                });
    }

    updateListTable() {
        this.licensesDao.getLicense().$promise.then((licenses)=> {
            this.licenses = licenses;
            this.gridOption.setGridData(this.licenses.data)
            if (this.modalInstance) {
                this.closeModal();
            }
        });
    }

    setStatus(row) {
        if (row.approved) {
            row.approved = false;
            row.status = "Unapproved";
        }
        else {
            row.approved = true;
            row.status = "Approved";
        }
        this.licensesDao.update(row).$promise.then(()=>this.updateListTable());
    }

    exportLicenses() {
        this.$window.open(`${API.API_URL}/licenseexport`, '_self', '');
    }

    getCloumns() {
        return [
            {
                name: "License Key",
                displayName: "License Key",
                field: "name",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="^.licenses.edit({licenseName: row.entity.name})" class="text-center ui-grid-cell-contents">{{row.entity.name}}</a></div>',
                width: '15%'
            },
            {
                name: 'Name',
                displayName: 'Name',
                field: "longName",
                cellTemplate: '<div class="ui-grid-cell-contents"><span>{{row.entity.longName}}</span></div>',
                width: '45%'

            },
            {
                name: "URL",
                displayName: "URL",
                field: "url",
                cellTemplate: '<div class="ui-grid-cell-contents" ><a href="{{row.entity.url}}" target="_blank">{{row.entity.url}}</a></div>',
                width: '30%'
            },
            {
                name: "Status",
                displayName: "Status",
                field: "status",
                cellTemplate: '<div class="ui-grid-cell-contents"><a href="" ng-click="grid.appScope.AdminConfigurationLicenses.setStatus(row.entity)" ng-class="{\'license-approved\': row.entity.approved, \'license-unapproved\': !row.entity.approved}">{{row.entity.status}}</a></div>',
                width: '10%'
            }
        ]
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: license => this.deleteLicense(license)
            }
        ];
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedLicenses()
            },
        ]
    }


}