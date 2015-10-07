describe('unit test:blackDuck dao', function () {

    var blackDuck;
    var RESOURCE;
    var server;
    var blackDuckMock = {
        enableIntegration: true, "serverUri": "https://jfrogcc.blackducksoftware.com/"
        , "username": "jfrog", "password": "Msmpn6wpAfFDZ7jxteAMyDP", "connectionTimeoutMillis": 20000
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        blackDuck = $injector.get('BlackDuckDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('groupDao should return a resource object', function () {
        expect(blackDuck.name).toBe('Resource');
    });

    it('blackDuckDao send a put request to serve', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.BLACK_DUCK).respond(200);
        blackDuck.update(blackDuckMock);
        server.flush();
    });

    it('blackDuckDao send a post request to serve', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.BLACK_DUCK).respond(200);
        var obj = new blackDuck(blackDuckMock);
        obj.$save();
        server.flush();
    });

    it('blackDuckDao send a get request t', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.BLACK_DUCK).respond(200);
        blackDuck.get();
        server.flush();
    });

});