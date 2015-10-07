describe('unit test:licenses dao', function () {

    var licensesDao;
    var RESOURCE;
    var server;
    var licenseMock = {
        "name": "AFL-3.0",
        "longName": "The Academic Free License 3.0"
        ,
        "url": "http://www.opensource.org/licenses/afl-3.0.php"
        ,
        "regexp": "((.*)(academic)(.*)|(AFL)+(.*))(3)(.*)",
        "approved": true,
        "status": "Approved",
        "comments": "my comments"
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        licensesDao = $injector.get('LicensesDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('licensesDao should return a resource object', function () {
        expect(licensesDao.name).toBe('Resource');
    });

    it('send an update request with licenses dao', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.LICENSES + '/crud/' + licenseMock.name).respond(200);
        licensesDao.update(licenseMock);
        server.flush();
    });

    it('should post a new license data object', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.LICENSES, licenseMock).respond(200);
        var licenseDataObject = new licensesDao(licenseMock);
        licenseDataObject.$save();
        server.flush();
    });
});