var faker = require('faker');
export function PropertyMock(data = {}) {
  let defaults = {
    name: faker.name.firstName(),
    propertyType: "ANY_VALUE",
    predefinedValues: [
      {value: 'value1', defaultValue: false},
      {value: 'value2', defaultValue: true}
    ]
  };
  data = angular.extend(defaults, data);

  return data;
}

export function PropertySetMock(data = {}) {
  let defaults = {
    name: faker.name.firstName(),
    properties: [new PropertyMock(), new PropertyMock()]
  };
  data = angular.extend(defaults, data);

  return data;
}

