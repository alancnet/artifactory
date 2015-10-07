export function PropertySetsDao(ArtifactoryDaoFactory, RESOURCE) {
    return ArtifactoryDaoFactory()
    	.setPath(RESOURCE.PROPERTY_SETS + "/:action/:name")
        .setCustomActions({
            'get': {
                params: {name: '@name'}
            },
            'update': {
                params: {name: '@name'}
            },
            'delete': {
                method: 'POST',
                params: {action: 'deletePropertySet'}
            },
                'query': {
                    isArray: true,
                    params: {name: '@name', isRepoForm: '@isRepoForm'}
                }
            })
    	.getInstance();
}


export class Property {
    constructor(data) {
        data = data || {};
        data.propertyType = data.propertyType || "ANY_VALUE";

        data.predefinedValues = data.predefinedValues || [];
        angular.extend(this, data);
    }
    getDisplayType() {
        let type = Property.propertyTypesMap[this.propertyType];
        return type ? type.text : null;
    }
    getDefaultValues() {
        return _.where(this.predefinedValues, {defaultValue: true});
    }

    getPredefinedValue(value) {
        return _.findWhere(this.predefinedValues, {value: value});
    }
    addPredefinedValue(newValue) {
        this.predefinedValues.push({value: newValue, defaultValue: false});
    }
}

// Create an array and map of types for easy access
let anyValue = {value: 'ANY_VALUE', text: 'Any Value'};
let singleSelect = {value: 'SINGLE_SELECT', text: 'Single Select'};
let multiSelect = {value: 'MULTI_SELECT', text: 'Multi Select'};

Property.propertyTypes = [anyValue, singleSelect, multiSelect];
Property.propertyTypesMap = {
    ANY_VALUE: anyValue,
    SINGLE_SELECT: singleSelect,
    MULTI_SELECT: multiSelect
};

export function PropertyFactory() {
    return Property;
}

export class PropertySet {
    constructor(data) {
        data = data || {};
        data.properties = data.properties || [];
        angular.extend(this, data);
        this.properties = this.properties.map((property) => new Property(property));
    }
    getPropertyByName(propertyName) {
        return _.findWhere(this.properties, {name: propertyName});
    }
    addProperty(property) {
        this.properties.push(property);
    }
    removeProperty(propertyName) {
        _.remove(this.properties, {name: propertyName});
    }
}
export function PropertySetFactory() {
    return PropertySet;
}
