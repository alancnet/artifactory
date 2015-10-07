import EVENTS from '../../../../constants/artifacts_events.constants';
import KEYS       from '../../../../constants/keys.constants';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

class jfPropertiesController {
    constructor($q, $scope, ArtifactoryGridFactory, ArtifactPropertyDao, ArtifactoryEventBus, ArtifactoryModal,
                PredefineDao, RepoPropertySetDao, ArtifactoryNotifications, $timeout, User) {

        this.propertyGridOption = {};
        this.$timeout = $timeout;
        this.$q = $q;
        this.user = User;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactPropertyDao = ArtifactPropertyDao.getInstance();
        this.predefineDao = PredefineDao.getInstance();
        this.repoPropertySetDao = RepoPropertySetDao.getInstance();
        this.modal = ArtifactoryModal;
        this.$scope = $scope;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.propertyTypeKeys = KEYS.PROPERTY_TYPE;
        this.propertyType = 'Property';
        this.propertiesOptions = [];
        this.repoPropertyRecursive = {recursive: false};
        this.TOOLTIP = TOOLTIP.artifacts.browse;
        this._createGrid();
        this._getPropertiesData();
        this._createModalScope();

        ArtifactoryEventBus.registerOnScope($scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            this.currentNode = node;
            this.clearFields();
            this._getPropertiesData();
        });

        /**
         *  config selectize inputs
         *  **/
        this.propertySetMultiValuesConfig = {
            sortField: 'text',
            maxItems: null,
            plugins: ['remove_button']
        }
        this.propertySetSingleValueConfig = {
            sortField: 'text',
            maxItems: 1
        }
        this.propertySetAnyValueConfig = {
            sortField: 'text',
            maxItems: 1,
            create: true,
            createOnBlur: true,
            persist: true
        }
    }

    /**
     * delete Selected properties by batch
     * **/
    deleteSelectedProperties(recursive) {
        let self = this;
        let selectedProperties = this.propertyGridOption.api.selection.getSelectedRows();
        let confirmMessage = 'Are you sure you wish to delete ' + selectedProperties.length;
        confirmMessage += selectedProperties.length > 1 ? ' properties?' : ' property?';

        this.modal.confirm(confirmMessage)
            .then(() => {
                let propertiesToDelete = selectedProperties.map(property => {
                    return {
                        name: property.name,
                        path: self.currentNode.data.path,
                        repoKey: self.currentNode.data.repoKey,
                        recursive: recursive
                    }
                });
                this.artifactPropertyDao.deleteBatch({properties:propertiesToDelete}).$promise.then(()=> {
                    this._getPropertiesData();
                })
            });
    }

    /**
     * delete single proerty
     * ***/
    deleteSingleProperty(row, recursive) {

        let json ={properties:[
            {
                name: row.name,
                path: this.currentNode.data.path,
                repoKey: this.currentNode.data.repoKey,
                recursive: recursive
            }
        ]
    }

        this.modal.confirm('Are you sure you wish to delete this property?')
            .then(() => {
                this.artifactPropertyDao.deleteBatch(json).$promise.then(()=> {
                        this._getPropertiesData();
                    })
            });
    }

    clearFields() {
        if (this.repoPropertySetSelected) {
            if (this.repoPropertySetSelected.parent) {
                delete this.repoPropertySetSelected.parent;
            }
            if (this.repoPropertySetSelected.property) {
                delete this.repoPropertySetSelected.property;
            }
            if (this.repoPropertySetSelected.value) {
                delete this.repoPropertySetSelected.value;
            }
        }
    }

    isSelected(propertyType) {
        return this.propertyType == propertyType;
    }

    setProperty(propertyType) {
        this.propertyType = propertyType;
    }

    /**
     * add Property Set to list
     * **/
    addPropertySet() {
        if (this.repoPropertySetSelected) {
            this._savePropertySetValues(this.repoPropertySetSelected);
        }
        this.repoPropertySetSelected = '';
        this.propertyValuesOptions = [];
    }

    /**
     * add single property to list
     * **/
    addProperty() {

        let objProperty = this._createNewRepoObject(this.repoPropertySelected.name)
        delete objProperty.text;
        delete objProperty.value;
        this._savePropertyValues(objProperty);

        this.repoPropertySelected.name = '';
        this.repoPropertySelected.value = '';
    }

    /**
     * pouplited values to input propertyValuesOptions
     *
     * **/
    getPropertySetValues() {
        if (this.repoPropertySetSelected) {
            this.predefineDao.get({
                name: this.repoPropertySetSelected.parent.name + "." + this.repoPropertySetSelected.property.name,
                path: this.currentNode.data.path,
                repoKey: this.currentNode.data.repoKey,
                recursive: this.recursive
            }).$promise.then((predefineValues)=> {

                    this._getPropertySetPreDefinedValues(predefineValues);
                });
        }
    }

    isCurrentPropertyType(type) {
        if (!this.repoPropertySetSelected && type === 'ANY_VALUE') {
            return true;
        }
        else if (this.repoPropertySetSelected) {
            if(!this.repoPropertySetSelected.propertyType && type === 'ANY_VALUE') {
                return true;
            }
            return this.propertyTypeKeys[this.repoPropertySetSelected.propertyType] === this.propertyTypeKeys[type]
        }
    }

    setModalData(selectedProperty, predefineValues) {
        this.modalScope.property = selectedProperty;
        this.modalScope.property.predefineValues = predefineValues ? predefineValues.predefinedValues : null;
        this.modalScope.property.selectedValues = [];
        this.modalScope.property.modalTitle = "Add New '" + selectedProperty.property.name + "' Property";
        this.modalScope.save = (property) =>this._savePropertySetValues(property);
        this._propertyFormModal();
    }


    editSelectedProperty(row) {

        let selectedProperty = row;

        this.artifactPropertyDao.get({
            name: selectedProperty.name,
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey

        }).$promise.then((currentProperty)=> {
                    //console.log('currentProperty=',currentProperty);
                this.modalScope.property = currentProperty;
                this.modalScope.selectizeConfig = {
                    create: currentProperty.propertyType === 'ANY_VALUE',
                    maxItems: 1
                };
//                this.modalScope.property.multiValue = currentProperty.propertyType && currentProperty.propertyType === 'MULTI_SELECT';
                this.modalScope.property.modalTitle = "Edit '" + selectedProperty.name + "' Property";
                this.modalScope.property.name = selectedProperty.name;
                this.modalScope.save = (property) =>this._updatePropertySetValues(property)
                this._propertyFormModal();

            });
    }

    /**
     * build defulat template proerty
     * **/
    _createNewRepoObject(repoName) {
        return {
            multiValue: false,
            property: {name: repoName},
            text: repoName,
            value: repoName
        }
    }

    /**
     * popluted grid data and property Set list name
     * **/
    _getPropertiesData() {
        this.user.canAnnotate(this.currentNode.data.repoKey, this.currentNode.data.path).then((response) => {
            this.canAnnotate = response.data;
        });
        this.artifactPropertyDao.query({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey
        }).$promise.then((properties) => {
                this.properties = properties.artifactProperties ? properties.artifactProperties.map(this._formatToArray) : [];
                this.propertyGridOption.setGridData(this.properties);

                this._getPopertySetData();
            });
    }

    _getPopertySetData() {
        this.repoPropertySetDao.query({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey
        }).$promise.then((_propertyOptionList)=> {
                let propertyOptionList = [];
                _propertyOptionList.forEach((propertyOption)=> {
                    propertyOption.value = propertyOption.property.name;
                    propertyOption.text = propertyOption.property.name;
                    propertyOptionList.push(propertyOption);
                });
                this.propertiesOptions = propertyOptionList;

            });
    }

    _createModalScope() {
        this.modalScope = this.$scope.$new();
        this.modalScope.closeModal = () => this.modalInstance.close();
    }

    _createGrid() {
        this.propertyGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getColumns())
                .setRowTemplate('default')
                .setMultiSelect()
                .setButtons(this._getActions())
                .setBatchActions(this._getBatchActions());
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: row => this.deleteSingleProperty(row, false),
                visibleWhen: () => this.canAnnotate
            },
            {
                icon: 'icon icon-delete-versions',
                tooltip: 'Delete Recursively',
                callback: row => this.deleteSingleProperty(row, true),
                visibleWhen: () => this.canAnnotate && (this.currentNode.data.type == 'folder' || this.currentNode.data.type == 'repository')
            }
        ];
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedProperties(false),
                visibleWhen: () => this.canAnnotate
            },
            {
                icon: 'delete-recursive',
                name: 'Delete Recursively',
                callback: () => this.deleteSelectedProperties(true),
                visibleWhen: () => this.canAnnotate && (this.currentNode.data.type == 'folder' || this.currentNode.data.type == 'repository')
            }
        ]
    }

    _propertyFormModal() {
        this.modalInstance = this.modal.launchModal("property_modal", this.modalScope, (this.modalScope.property.propertyType != 'MULTI_SELECT' ? 'sm' : 'lg'));
    }

    _savePropertyValues(property) {
        property.selectedValues = [];
        property.selectedValues.push(this.repoPropertySelected.value);
        this.artifactPropertyDao.save({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey,
            recursive: this.repoPropertyRecursive.recursive
        }, property).$promise.then(()=> {
                this._getPropertiesData();
            });
    }

    _savePropertySetValues(property) {

        if (property.propertyType==='MULTI_SELECT') {
            this._addValuesToMulti(property, this.repoPropertySetSelected.value);
        }
        else {
            property.selectedValues = this.repoPropertySetSelected.value;
        }

        if (!property.multiValue && !_.isArray(property.selectedValues)) {
            let selectedValuesToArray = angular.copy(property.selectedValues);
            property.selectedValues = [];
            property.selectedValues.push(selectedValuesToArray);

        }

        this.artifactPropertyDao.save({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey,
            recursive: this.repoPropertyRecursive.recursive
        }, property).$promise.then(()=> {
                    this._getPropertiesData();
                });
    }

    _addValuesToMulti(property, addedValues) {
        //console.log(property);
        let theProperty = _.findWhere(this.properties, {name: property.parent.name+'.'+property.property.name});
        if (theProperty) {
            property.selectedValues = theProperty.value.concat(addedValues);
        }
        else {
            property.selectedValues = addedValues;
        }
    }


    _updatePropertySetValues(property) {
        if (!property.multiValue && !_.isArray(property.selectedValues)) {
            let selectedValuesToArray = angular.copy(property.selectedValues);
            property.selectedValues = [];
            property.selectedValues.push(selectedValuesToArray);

        }
        //console.log(property);
        this.artifactPropertyDao.update({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey,
            recursive: this.repoPropertyRecursive.recursive
        }, property).$promise.then(()=> {
                    this._getPropertiesData();
                    this.modalInstance.close();
                });
    }

    _getPropertySetPreDefinedValues(predefineValues) {
        this.propertyValuesOptions = [];
        predefineValues.predefinedValues.forEach((preValue)=> {
            this.propertyValuesOptions.push(this._createNewRepoObject(preValue));
            this.repoPropertySetSelected.value = [];
        });
        this.repoPropertySetSelected.value = predefineValues.selectedValues;
    }

    _getColumns() {

        let cellTemplate = '<div class="grid-items-container"><div class="item" ng-if="row.entity.value.length>1" ng-repeat="col in row.entity.value track by $index">{{col}}</div>' +
            '<div class="ui-grid-cell-contents" ng-if="row.entity.value.length==1" >{{row.entity.value[0]}}</div></div>';

        let keyCellTemplate = '<div ng-if="!grid.appScope.jfProperties.canAnnotate" class="ui-grid-cell-contents">{{row.entity.name}}</div>' +
                '<div ng-if="grid.appScope.jfProperties.canAnnotate" class="ui-grid-cell-contents"><a href="" ng-click="grid.appScope.jfProperties.editSelectedProperty(row.entity)">{{row.entity.name}}</a></div>'

        return [
            {
                name: "Property",
                displayName: "Property",
                field: "name",
                cellTemplate: keyCellTemplate
            },
            {
                name: "Value(s)",
                displayName: "Value(s)",
                field: "value",
                cellTemplate: cellTemplate
            }
        ]
    }

    _formatToArray(list) {
        return {name: list.name, value: _.trimRight(list.value.toString(), ';').split(';')};
    }


}
export function jfProperties() {
    return {
        restrict: 'EA',
        controller: jfPropertiesController,
        scope: {
            currentNode: '='
        },
        controllerAs: 'jfProperties',
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_properties.html'
    }
}