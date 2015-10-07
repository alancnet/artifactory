describe('unit test: export dao', function () {

    var ExportDao;
    var RESOURCE;
    var server;


    var exportMock = {
        path: '/home/jfrog/export',
        action:'system',
        excludeContent: false,
        excludeMetadata: false,
        excludeBuilds: false,
        m2: false,
        createArchive: false,
        verbose: false
    };


    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        ExportDao = $injector.get('ExportDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('ExportDao should return a resource object', function () {
        expect(ExportDao.name).toBe('Resource');
    });

    it('send an export request', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.EXPORT+"/system" , exportMock).respond(200);
        ExportDao.save(exportMock);
        server.flush();
    });
});