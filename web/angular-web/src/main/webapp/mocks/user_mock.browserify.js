var faker = require('faker');
class UserMock {
  constructor(data = {}) {
    let defaults = {
      name: faker.name.firstName(),
      admin: true,
      profileUpdatable: true,
      internalPasswordDisabled: false,
      canManage: true,
      canDeploy: true,
      preventAnonAccessBuild: true
    };
    data = angular.extend(defaults, data);

    angular.copy(data, this);
  }

  mockUserMethods() {
    inject((User, $q) => {
      let user = new User(this);
      spyOn(User, 'getCurrent').and.returnValue(user);
      spyOn(User, 'loadUser').and.returnValue($q.when(user));
    });
  }
  getUserObj() {
    let user;
    inject((User) => {
      user = new User(this);
    });    
    return user;
  }

  static regularUser(data) {
    data = data || {};
    data.admin = false;
    return new UserMock(data);
  };
  static guest() {
    return new UserMock({name: 'anonymous', admin: false, canManage: false, canDeploy: false, preventAnonAccessBuild: true});
  };
  static mockCurrentUser() {
    inject(($httpBackend, RESOURCE) => {
      $httpBackend.whenGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(this.guest());
    });
  }
}

export default UserMock;