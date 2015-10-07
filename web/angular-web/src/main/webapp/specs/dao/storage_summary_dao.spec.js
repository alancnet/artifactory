describe('Unit: Storage Summary  dao', function () {

    var storageSummaryDao;
    var RESOURCE;
    var server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        storageSummaryDao = $injector.get('StorageSummaryDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('storageSummaryDao should return a resource object', function () {
        expect(storageSummaryDao.name).toBe('Resource');
    });

    it('storageSummaryDao send an Get request ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.STORAGE_SUMMARY).respond(200);
        storageSummaryDao.get();
        server.flush();
    });

});