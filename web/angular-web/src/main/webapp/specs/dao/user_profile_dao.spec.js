describe('unit test:user profile dao', function () {

    var userProfileDao;
    var RESOURCE;
    var server;
    var userParams =  {
        "name": "idan",
        "email": "idanaim@gmail.com",
        "admin": false,
        "profileUpdatable": true,
        "internalPasswordDisabled": false
    };

    var userParms = "?admin=false&email=idanaim@gmail.com&internalPasswordDisabled=false&profileUpdatable=true";

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        userProfileDao = $injector.get('UserProfileDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('userProfileDao should return a resource object', function () {
        expect(userProfileDao.name).toBe('Resource');
    });

    it('should fetch data', function() {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.USER_PROFILE).respond(200);
        userProfileDao.fetch({password: 'password'});
        server.flush();
    });

    it('should update data', function() {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.USER_PROFILE).respond(200);
        userProfileDao.update({password: 'password'});
        server.flush();
    });

});