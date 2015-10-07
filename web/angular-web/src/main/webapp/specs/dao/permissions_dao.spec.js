describe('unit test:permissions dao', function () {

    var permissionsDao;
    var RESOURCE;
    var server;

    var settingsParams = {

    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        permissionsDao = $injector.get('PermissionsDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('permissionsDao should return a resource object', function () {
        expect(permissionsDao.name).toBe('Resource');
    });
    it('permissionsDao should return a resource object', function () {
        server.expectGET(RESOURCE.API_URL +RESOURCE.TARGET_PERMISSIONS +'/crud/Anything').respond(200);
        permissionsDao.getPermission({name: 'Anything'});
        server.flush();
    });
});