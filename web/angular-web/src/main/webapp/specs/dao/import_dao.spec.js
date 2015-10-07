describe('unit test: import dao', function () {

    var ImportDao;
    var RESOURCE;
    var server;


    var importMock = {
        action:'system',
        path: '/home/jfrog/import',
        excludeContent: false,
        excludeMetadata: false,
        verbose: false
    };


    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        ImportDao = $injector.get('ImportDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('ImportDao should return a resource object', function () {
        expect(ImportDao.name).toBe('Resource');
    });

    it('send an import request', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.IMPORT+"/system" , importMock).respond(200);
        ImportDao.save(importMock);
        server.flush();
    });
});