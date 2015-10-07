describe('Unit: register Pro Dao', function () {

    var registerProDao;
    var RESOURCE;
    var server;
    var licenseMock = {"key" : "1234567890abcdefg"};

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        registerProDao = $injector.get('RegisterProDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('passwordEncryptionDao should return a resource object', function () {
        expect(registerProDao.name).toBe('Resource')
    });

    it('should send a get request to the server', function () {
        server.expectGET(RESOURCE.API_URL+RESOURCE.REGISTER_PRO).respond(200);
        registerProDao.get();
        server.flush();
    });

    it('should send a get request to the server', function () {
        server.expectGET(RESOURCE.API_URL+RESOURCE.REGISTER_PRO).respond(200);
        registerProDao.get();
        server.flush();
    });

    it('should update the license by sending a put request', function () {
        server.expectPUT(RESOURCE.API_URL+RESOURCE.REGISTER_PRO, licenseMock).respond(200);
        registerProDao.update(licenseMock);
        server.flush();
    });

});