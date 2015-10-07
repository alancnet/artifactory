describe('unit test:crowd integration dao', function () {

    var crowdIntegrationDao;
    var RESOURCE;
    var server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (CrowdIntegrationDao, _RESOURCE_, _$httpBackend_) {
        crowdIntegrationDao = CrowdIntegrationDao;
        RESOURCE = _RESOURCE_;
        server = _$httpBackend_;
    }));

    it('artifactBuildsDao should return a resource object', function () {
        expect(crowdIntegrationDao.name).toBe('Resource');
    });

    it('query should send the correct GET request', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.CROWD + '/test').respond({});
        crowdIntegrationDao.test({
            "enableIntegration": true,
            "serverUrl": "http://localhost:8095/crowd",
            "applicationName": "artifactory",
            "password": "password",
            "sessionValidationInterval": 0,
            "useDefaultProxy": false,
            "noAutoUserCreation": true,
            "directAuthentication": false
        });
    });

    it('refresh data should send the correct GET request', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.CROWD + '/refresh/chenk').respond({});
        crowdIntegrationDao.refresh({
            "enableIntegration": true,
            "serverUrl": "http://localhost:8095/crowd",
            "applicationName": "artifactory",
            "password": "password",
            "sessionValidationInterval": 0,
            "useDefaultProxy": false,
            "noAutoUserCreation": true,
            "directAuthentication": false
        });
    });
});