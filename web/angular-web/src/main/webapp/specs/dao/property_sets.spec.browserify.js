import {PropertySetMock, PropertyMock} from '../../mocks/property_sets_mocks.browserify.js';
describe('unit test:propertysets', () => {
    let propertysetsDao;
    let PropertySet;
    let Property;
    let RESOURCE;
    let server;
    let propertyMock = PropertyMock({name: 'testprop'});
    let propertySetsMock = [PropertySetMock(), PropertySetMock()];
    let propertySetMock = PropertySetMock({
        name: 'test',
        properties: [propertyMock]
    });
    let propertyObject;
    let propertySetObject;

    let value1 = {value: 'one', defaultValue: false};
    let defaultValue1 = {value: 'two', defaultValue: true};
    let value2 = {value: 'three', defaultValue: false};
    let defaultValue2 = {value: 'four', defaultValue: true};

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(($injector) => {
        propertysetsDao = $injector.get('PropertySetsDao');
        PropertySet = $injector.get('PropertySet');
        Property = $injector.get('Property');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    beforeEach(() => {
        propertySetObject = new PropertySet(propertySetMock);
        propertyObject = new Property(propertyMock);
    });

    describe('PropertySetsDao', () => {
        it('propertysetsDao should return a resource object', () => {
            expect(propertysetsDao.name).toBe('Resource');
        });

        it('should query all property sets', () => {
            server.expectGET(RESOURCE.API_URL + RESOURCE.PROPERTY_SETS).respond(propertySetsMock);
            propertysetsDao.query();
            server.flush();
        });

        it('should get a single property set', () => {
            server.expectGET(RESOURCE.API_URL + RESOURCE.PROPERTY_SETS + '/' + propertySetMock.name).respond(propertySetMock);
            propertysetsDao.get({name: propertySetMock.name});
            server.flush();
        });

        it('should save an existing propertyset data object', () => {
            server.expectPUT(RESOURCE.API_URL + RESOURCE.PROPERTY_SETS + '/' + propertySetMock.name, propertySetMock).respond(200);
            var propertysetDataObject = new propertysetsDao(propertySetMock);
            propertysetDataObject.$update();
            server.flush();
        });

        it('should post a new propertyset data object', () => {
            server.expectPOST(RESOURCE.API_URL + RESOURCE.PROPERTY_SETS, propertySetMock).respond(200);
            var propertysetDataObject = new propertysetsDao(propertySetMock);
            propertysetDataObject.$save();
            server.flush();
        });
    });
    describe('PropertySet', () => {
        describe('constructor', () => {
            it('should create property set with default values', () => {
                let propertySet = new PropertySet();
                expect(propertySet.properties).toEqual([]);
            });
            it('should create property set with given values', () => {
                let propertySet = new PropertySet(propertySetMock);
                expect(propertySet.name).toEqual(propertySetMock.name);
                let properties = propertySetMock.properties.map((prop) => new Property(prop));
                expect(propertySet.properties).toEqual(properties);
            });
        });
        describe('getPropertyByName', () => {
            it('should return property', () => {
                expect(propertySetObject.getPropertyByName('testprop')).toEqual(propertyObject);
            });
            it('should return undefined if not found', () => {
                expect(propertySetObject.getPropertyByName('not found')).not.toBeDefined();
            });
        });
        describe('addProperty', () => {
            it('should add a property to the end of the properties list', () => {
                let newProperty = new Property();
                propertySetObject.addProperty(newProperty);
                expect(propertySetObject.properties[1]).toEqual(newProperty);
            });
        });
        describe('removeProperty', () => {
            it('should remove a property by name', () => {
                propertySetObject.removeProperty('testprop');
                expect(propertySetObject.properties).toEqual([]);
            });
            it('should not do anything if property is not found', () => {
                propertySetObject.removeProperty('not found');
                expect(propertySetObject.properties).toEqual([propertyObject]);
            });
        });
    });
    describe('Property', () => {
        describe('Property.propertyTypes', () => {
            it('should be defined', () => {
                expect(Property.propertyTypes).toBeDefined();
            });
            it('should be an array', () => {
                expect(angular.isArray(Property.propertyTypes)).toBeTruthy();
            });
            it('should have elements with text and value', () => {
                let keys = Object.keys(Property.propertyTypes[0]);
                expect(keys).toEqual(['value', 'text']);
            });
        });
        describe('constructor', () => {
            it('should create property with default values', () => {
                let property = new Property();
                expect(property.propertyType).toEqual('ANY_VALUE');
                expect(property.predefinedValues).toEqual([]);
            });
            it('should create property with given values', () => {
                let property = new Property(propertyMock);
                expect(property.name).toEqual(propertyMock.name);
                expect(property.propertyType).toEqual(propertyMock.propertyType);
                expect(property.predefinedValues).toEqual(propertyMock.predefinedValues);
            });
        });
        describe('getDisplayType', () => {
            it('should return Any Value', () => {
                propertyObject.propertyType = 'ANY_VALUE';
                expect(propertyObject.getDisplayType()).toEqual('Any Value');
            });
            it('should return Single Select', () => {
                propertyObject.propertyType = 'SINGLE_SELECT';
                expect(propertyObject.getDisplayType()).toEqual('Single Select');
            });
            it('should return Multi Select', () => {
                propertyObject.propertyType = 'MULTI_SELECT';
                expect(propertyObject.getDisplayType()).toEqual('Multi Select');
            });
        });
        describe('getDefaultValues', () => {
            it('should return the array of default values', () => {
                let property = new Property({ predefinedValues:[
                    value1,
                    defaultValue1,
                    value2,
                    defaultValue2
                ]});
                expect(property.getDefaultValues()).toEqual([defaultValue1, defaultValue2]);
            });
            it('should return an empty array if no default values', () => {
                let property = new Property({predefinedValues:[{value: 'one'}]});
                expect(property.getDefaultValues()).toEqual([]);
            });
            it('should return an empty array if no predefined values', () => {
                let property = new Property();
                expect(property.getDefaultValues()).toEqual([]);
            });
        });
        describe('getPredefinedValue', () => {
            it('should return a predefined value by name', () => {
                let property = new Property({ predefinedValues:[
                    value1,
                    value2
                ]});
                expect(property.getPredefinedValue('one')).toEqual(value1);
            });
            it('should return undefined if not found', () => {
                let property = new Property({ predefinedValues:[
                    value1,
                    value2
                ]});
                expect(property.getPredefinedValue('not found')).not.toBeDefined();
            });
        });
        describe('addPredefinedValue', () => {
            it('should allow to add a predefined value, and set it to default = false', () => {
                let property = new Property({ predefinedValues:[
                    value1
                ]});
                property.addPredefinedValue('three');
                expect(property.predefinedValues).toEqual([value1, value2]);
            });
        });
    });
});