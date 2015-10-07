describe('Unit: http sso dao', function () {

    var httpSsoDao;
    var RESOURCE;
    var server;
    var ssoMock = {
        "httpSsoProxied": true,
        "noAutoUserCreation": false,
        "remoteUserRequestVariable": "REMOTE_USER"
    };


    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        httpSsoDao = $injector.get('HttpSsoDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('passwordEncryptionDao should return a resource object', function () {
        expect(httpSsoDao.name).toBe('Resource')
    });

    it('should send a get request when query for proxies', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.HTTPSSO).respond(200);
        httpSsoDao.get();
        server.flush();
    });

    it('should send a get request when query for proxies', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.HTTPSSO, ssoMock).respond(200);
        httpSsoDao.update(ssoMock);
        server.flush();
    });

});
