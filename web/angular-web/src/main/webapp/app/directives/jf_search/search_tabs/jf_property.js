import EVENTS     from '../../../constants/artifacts_events.constants';
import KEYS       from '../../../constants/keys.constants';
import TOOLTIP    from '../../../constants/artifact_tooltip.constant';

class jfPropertyController {

    constructor($scope, $compile, $state,$stateParams, ArtifactoryEventBus, ArtifactSearchDao, ArtifactoryGridFactory, ArtifactoryFeatures) {
        this.artifactSearchDao = ArtifactSearchDao;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$state = $state;
        this.$scope = $scope;
        this.propertyTypeKeys = KEYS.PROPERTY_TYPE;
        this.propertyGridOption = {};
        this.gridItems = this.query.properties || [];
        this.propertyType = ($stateParams.searchParams && $stateParams.searchParams.propertyType) ? $stateParams.searchParams.propertyType : 'Property';
        this.propertiesOptions = [];
        this.propertyValuesOptions = [];
        this.query.search = "property";
        this.propertySetMultiValuesConfig = {
            sortField: 'text',
            maxItems: null,
            plugins: ['remove_button']
        };
        this.propertySetSingleValueConfig = {
            sortField: 'text',
            maxItems: 1
        };
        this.TOOLTIP = TOOLTIP.artifacts.search.propertySearch;

        // If we're not allowed to see the property search:
        if (ArtifactoryFeatures.isDisabled('properties')) {
            $state.go('.', {searchType: 'quick'});
            return;
        }

        this.getProperties();

        this._createGrid();
    }

    _createGrid() {
        this.propertyGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getCloumns())
                .setRowTemplate('default')
                .setGridData( this.gridItems || [])
                .setButtons(this._getActions())
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: row => this.deleteSingleProperty(row, false)
            }
        ];
    }

    deleteSingleProperty(row) {
        _.remove(this.gridItems, {key: row.key, values: row.values});
        this.propertyGridOption.setGridData(this.gridItems);
    }

    addPropertySet() {
        let values = [];
        if (!_.isArray(this.selectedPropertySetValue)) {
            values.push(this.selectedPropertySetValue);
        }

        this.gridItems.push({
            key: this.selectedPropertySet.key,
            values: _.isArray(this.selectedPropertySetValue) ? this.selectedPropertySetValue : values
        });

        this.propertyGridOption.setGridData(this.gridItems);
        this.selectedPropertySetValue = '';
        this.selectedPropertySet = '';

    }

    addProperty() {
        let values = [];
        values.push(this.selectedPropertyValue);
        this.gridItems.push({
            key: this.selectedProperty,
            values: values
        });

        this.propertyGridOption.setGridData(this.gridItems);

        this.selectedPropertyValue = '';
        this.selectedProperty = '';
    }


    getProperties() {
        this.artifactSearchDao.get({search: "property", action: "keyvalue"}).$promise.then((_propeties)=> {
            this.propertiesOptions = _propeties.data;
        });
    }

    clearFields() {
        if (this.propertyType == 'Property') {
            this.selectedPropertyValue = '';
            this.selectedProperty = '';
        }
        else {
            this.selectedPropertySetValue = '';
            this.selectedPropertySet = '';
        }
    }

    isCurrentPropertyType(type) {
        if (!this.selectedPropertySet && type === 'ANY_VALUE') {
            return true;
        }
        else if (this.selectedPropertySet) {

            return this.propertyTypeKeys[this.selectedPropertySet.propertyType] === this.propertyTypeKeys[type]
        }
    }

    setPropertySetValues() {

        let self = this;
        let propertyValues = [];
        self.propertyValuesOptions = [];
        this.propertiesOptions.forEach((property)=> {
            if (property === self.selectedPropertySet) {
                property.values.forEach((value)=> {
                    propertyValues.push({text: value, value: value});
                });
                self.propertyValuesOptions = propertyValues;
            }
        });
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedProperties(false)
            }
        ]
    }

    deleteSelectedProperties() {
        let selectedProperties = this.propertyGridOption.api.selection.getSelectedRows();
        selectedProperties.forEach((property)=> {
            _.remove(this.gridItems, {key: property.key, values: property.values});
        });
        this.propertyGridOption.setGridData(this.gridItems);
    }

    search() {

        this.query.search = "property";
        this.query.properties = this.gridItems;
        this.$state.go('.', {
            'searchType': "property",
            'searchParams': {
                propertyType: this.propertyType,
                selectedRepos: this.query.selectedRepositories
            },
            'params': btoa(JSON.stringify(this.query))
        });


    }

    _getCloumns() {
        let cellTemplate = '<div class="item" ng-if="row.entity.values.length>1" ng-repeat="col in row.entity.values track by $index">{{col}}</div>' +
                '<div class="ui-grid-cell-contents" ng-if="row.entity.values.length==1" >{{row.entity.values[0]}}</div>';
        return [{
            name: 'Property',
            displayName: 'Property',
            field: 'key'
        },
            {
                name: 'Value(s)',
                displayName: 'Value(s)',
                cellTemplate: cellTemplate,
                field: 'values'

            }
        ]
    }

}

export function jfProperty() {
    return {
        scope: {
            query: '='
        },
        restrict: 'EA',
        controller: jfPropertyController,
        controllerAs: 'jfProperty',
        bindToController: true,
        templateUrl: 'directives/jf_search/search_tabs/jf_property.html'
    }
}
