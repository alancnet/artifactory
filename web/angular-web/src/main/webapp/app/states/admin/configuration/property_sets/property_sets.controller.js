let $timeout, Property, PropertySet, uiGridConstants;
export class AdminConfigurationPropertySetsController {

    constructor($scope, PropertySetsDao, ArtifactoryGridFactory, _$timeout_, _Property_, _PropertySet_, ArtifactoryModal, _uiGridConstants_) {
        $timeout = _$timeout_;
        this.propertySetsDao = PropertySetsDao;
        this.$scope = $scope;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.modal = ArtifactoryModal;
        Property = _Property_;
        PropertySet = _PropertySet_;
        uiGridConstants = _uiGridConstants_;
        this._createGrid();
        this._initPropertySets();
    }

    _initPropertySets() {
        this.propertySetsDao.query().$promise.then((propertySets)=> {
            this.propertySets = propertySets.map((propertySet) => new PropertySet(propertySet));
            this.gridOptions.setGridData(this.propertySets)
        });
    }

    _createGrid() {
        this.gridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getColumns())
                .setRowTemplate('default')
                .setMultiSelect()
                .setButtons(this._getActions())
                .setBatchActions(this._getBatchActions());
    }


    deletePropertySet(propertySet) {
        this.modal.confirm(`Are you sure you want to delete the property set '${propertySet.name}?'`)
            .then(() => {
                let json = {propertySetNames:[propertySet.name]};
                this.propertySetsDao.delete(json).$promise
                    .then(()=>this._initPropertySets());
            });
    }

    deleteSelectedPropertySets() {
        //Get All selected users
        let selectedRows = this.gridOptions.api.selection.getSelectedGridRows();
        this.modal.confirm(`Are you sure you want to delete ${selectedRows.length} property sets?`)
            .then(() => {
                //Create an array of the selected propertySet names
                let names = selectedRows.map(row => row.entity.name);
                //Delete bulk of property sets
                this.propertySetsDao.delete({propertySetNames: names}).$promise
                        .then(()=>this._initPropertySets());
            });
    }

    getColumns() {
        return [
            {
                field: "name",
                name: "Property Set Name",
                displayName: "Property Set Name",
                sort: {
                    direction: uiGridConstants.ASC
                },
                cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="^.property_sets.edit({propertySetName: row.entity.name})" class="text-center ui-grid-cell-contents">{{row.entity.name}}</a></div>'
            },
            {
                field: "propertiesCount",
                name: "Properties Count",
                displayName: "Properties Count"
            }
        ]
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: propertySet => this.deletePropertySet(propertySet)
            }
        ];
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedPropertySets()
            },
        ]
    }

}
