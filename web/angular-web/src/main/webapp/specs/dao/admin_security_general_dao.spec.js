describe('Unit: admin security general dao', function () {

    var adminSecurityGeneralDao;
    var RESOURCE;
    var server;

    var securityConfigMock = {

        "anonAccessEnabled": false,
        "anonAccessToBuildInfosDisabled": "true",
        "hideUnauthorizedResources": "false",
        "passwordSettings": {
            "encryptionPolicy": "SUPPORTED"
        }
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        adminSecurityGeneralDao = $injector.get('AdminSecurityGeneralDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));


    it('passwordEncryptionDao should return a resource object', function () {
        //expect(adminSecurityGeneralDao.name).toBe('Resource')
    });

    it('sould send an update request with the updated object', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.SECURITY_CONFIG, securityConfigMock).respond(200);
        adminSecurityGeneralDao.update(securityConfigMock);
        server.flush();
    });

    it('hit a server with a get request', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.SECURITY_CONFIG).respond(200);
        adminSecurityGeneralDao.get();
        server.flush();
    });

    it('should post new security config mock', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.SECURITY_CONFIG, securityConfigMock).respond(200);
        var SecurityGeneraData = new adminSecurityGeneralDao(securityConfigMock);
        SecurityGeneraData.$save();
        server.flush();
    });


})
;
