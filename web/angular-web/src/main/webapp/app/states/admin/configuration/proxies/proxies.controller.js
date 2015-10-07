export class AdminConfigurationProxiesController {

    constructor($scope, ProxiesDao, ArtifactoryGridFactory, ArtifactoryModal, $q, uiGridConstants, commonGridColumns) {
        this.gridOptions = {};
        this.commonGridColumns = commonGridColumns;
        this.uiGridConstants = uiGridConstants;
        this.proxiesDao = ProxiesDao;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.modal = ArtifactoryModal;
        this.$scope=$scope;
        this.$q = $q;

        this._createGrid();
        this._initProxies();
    }

    _initProxies() {
        this.proxiesDao.get().$promise.then((proxies)=> {
            //console.log(proxies);

            this.gridOptions.setGridData(proxies)
        });
    }

    _createGrid() {
        this.gridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getColumns())
                .setMultiSelect()
                .setButtons(this._getButtons())
                .setBatchActions(this._getBatchActions())
                .setRowTemplate('default');
    }

    deleteSelectedProxies() {
        let selectedRows = this.gridOptions.api.selection.getSelectedRows();
        this.modal.confirm(`Are you sure you want to delete ${selectedRows.length} proxies?`)
            .then(() => {
                    //Create an array of the selected propertySet names
                    let keys = _.map(selectedRows, (row) => {return row.key;});
                    //Create Json for the bulk request
                    let json = {'proxyKeys': keys};
                    //console.log('Bulk delete....');
                    //Delete bulk of property set
                    this.proxiesDao.delete(json).$promise
                            .then(()=>this._initProxies());
            })
            .then(() => this._initProxies());
    }

    deleteProxy(key) {
        this.modal.confirm(`Are you sure you want to delete the proxy '${key}'?`)
            .then(() => this._doDeleteProxy(key))
            .then(() => this._initProxies());
    }

    _doDeleteProxy(key) {

        let json = {proxyKeys:[key]}
        //console.log(json);
        return this.proxiesDao.delete(json).$promise;
    }

    _getColumns() {
        return [
            {
                field: "key",
                name: "Key",
                displayName: "Key",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="^.proxies.edit({proxyKey: row.entity.key})">{{ COL_FIELD }}</a></div>',
                width: '30%'
            },
            {
                field: "host",
                name: "Host",
                displayName: "Host",
                width: '45%'
            },
            {
                field: "port",
                name: "Port",
                displayName: "Port",
                width: '10%'
            },
            {
                field: "defaultProxy",
                name: "Default Proxy",
                displayName: "Default Proxy",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.defaultProxy'),
                width: '15%'
            }
        ]
    }
    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedProxies()
            }
        ]
    }

    _getButtons() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: row => this.deleteProxy(row.key)
            }

        ];
    }

}