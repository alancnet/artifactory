import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

// Injectables:
let $q, $scope, $state, $stateParams, ArtifactoryGridFactory, PropertySetsDao, PropertyFormModal, Property, PropertySet, uiGridConstants, ArtifactoryModal;

export class AdminConfigurationPropertySetFormController {
    constructor(_$stateParams_, _$scope_, _PropertySetsDao_, _$state_, _ArtifactoryGridFactory_, _PropertyFormModal_, ArtifactoryState, _$q_, _Property_, _PropertySet_, _uiGridConstants_, _ArtifactoryModal_) {
        $scope = _$scope_;
    	$state = _$state_;
        $stateParams = _$stateParams_;
        Property = _Property_;
        PropertySet = _PropertySet_;
        ArtifactoryModal = _ArtifactoryModal_;

    	this.isNew = !$stateParams.propertySetName;
    	PropertySetsDao = _PropertySetsDao_;
        PropertyFormModal = _PropertyFormModal_;
		ArtifactoryGridFactory = _ArtifactoryGridFactory_;
        $q = _$q_;
        uiGridConstants = _uiGridConstants_;

        this.TOOLTIP = TOOLTIP.admin.configuration.propertySetsForm;
        this._createGrid();
        this._initPropertySet();
        ArtifactoryState.setState('prevState', $state.current);
    }

    _initPropertySet() {
        let promise;
        if (this.isNew) {
            promise = $q.when();
        }
        else {
            promise = PropertySetsDao.get({name: $stateParams.propertySetName}).$promise;
        }
        promise.then((propertySet) => {
            this.propertySet = new PropertySet(propertySet);
            this.gridOptions.setGridData(this.propertySet.properties)
        });
    }

    _createGrid() {
        this.gridOptions = ArtifactoryGridFactory.getGridInstance($scope)
                .setColumns(this.getColumns())
                .setRowTemplate('default')
                .setMultiSelect()
                .setButtons(this._getActions())
                .setBatchActions(this._getBatchActions());
    }

    save() {
		let whenSaved = this.isNew ? PropertySetsDao.save(this.propertySet) : PropertySetsDao.update(this.propertySet);
        whenSaved.$promise.then(() => this._end());
    }

	cancel() {
        this._end();
    }

    _end() {
        $state.go('^.property_sets');
    }

    editProperty(property) {
        // (Adam) Don't take the actual property object because it's different after filtering the GRID
        // Instead, we find the property in the original propertySet
        property = this.propertySet.getPropertyByName(property.name);
        this._launchPropertyEditor(property, false);
    }

    newProperty(e) {
        e.preventDefault();
        let property = new Property();
        this._launchPropertyEditor(property, true);
    }

    _launchPropertyEditor(property, isNew) {
        new PropertyFormModal(this.propertySet, property, isNew).launch()
        .then(() => {
            if (isNew) {
                this.propertySet.addProperty(property);
            }
            // (Adam) Must reset the data, because of the filter
            this.gridOptions.setGridData(this.propertySet.properties);
        });
    }

    _doDeleteProperty(property) {
        this.propertySet.removeProperty(property.name);
    }

    deleteProperty(property) {
        ArtifactoryModal.confirm(`Are you sure you want to delete the property '${property.name}?'`)
            .then(() => {
                this._doDeleteProperty(property);
                this.gridOptions.setGridData(this.propertySet.properties);
            });
    }

    deleteSelectedProperties() {
        let selectedRows = this.gridOptions.api.selection.getSelectedGridRows();
        ArtifactoryModal.confirm(`Are you sure you want to delete ${selectedRows.length} properties?`)
            .then(() => {
                selectedRows.forEach((row) => this._doDeleteProperty(row.entity));
                this.gridOptions.setGridData(this.propertySet.properties);
            });
    }

    getColumns() {
        return [
            {
                field: "name",
                name: "Property Name",
                displayName: "Property Name",
                sort: {
                    direction: uiGridConstants.ASC
                },
                cellTemplate: `
                    <div class="ui-grid-cell-contents">
                        <a  href=""
                            ng-click="grid.appScope.PropertySetForm.editProperty(row.entity)"
                            class="text-center ui-grid-cell-contents">{{row.entity.name}}</a>
                    </div>`
            },
            {
                name: 'Value Type',
                displayName: 'Value Type',
                field: "propertyType",
                cellTemplate: `<div class="ui-grid-cell-contents">{{ row.entity.getDisplayType() }}</div>`
            },
            {
                field: "predefinedValues",
                name: "Predefined Values",
                displayName: "Predefined Values",
                cellTemplate: `
                    <div style="padding-left: 10px;  white-space: nowrap; overflow-x: auto;">
                        <div class="item" ng-repeat="value in row.entity.predefinedValues">
                            {{value.value}}<span ng-if="value.defaultValue"> (default)</span>
                        </div>
                    </div>
                `
            }
        ]
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: propertySet => this.deleteProperty(propertySet)
            }
        ];
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedProperties()
            },
        ]
    }
}

