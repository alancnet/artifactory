describe('unit test:bintray dao', function () {

    var bintrayDao;
    var RESOURCE;
    var server;
    var bintrayMock = {
        "userName": "chenk",
        "apiKey": "165af2caacac2a636038ac7609eb7215170d946d",
        "fileUploadLimit": 0,
        "bintrayAuth": "chenk:165af2caacac2a636038ac7609eb7215170d946d"
    };


    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        bintrayDao = $injector.get('BintrayDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('bintrayDao should return a resource object', function () {
        expect(bintrayDao.name).toBe('Resource');
    });

    it('bintrayDao send an put request', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.BINTRAY_SETTING).respond(200);
        bintrayDao.update(bintrayMock);
        server.flush();
    });
    it('bintrayDao send an post request', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.BINTRAY_SETTING).respond(200);
        var obj = new bintrayDao(bintrayMock);
        obj.$save();
        server.flush();
    });

    it('bintrayDao send an get request', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.BINTRAY_SETTING).respond(200);
        bintrayDao.get();
        server.flush();
    });

});