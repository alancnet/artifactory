describe('unit test:checksums dao', function () {

    var checksumsDao;
    var RESOURCE;
    var server;
    var checksumsMock = {
        repoKey: 'libs-release-local',
        path: 'aopalliance/aopalliance/1.0/aopalliance-1.0.jar'
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        checksumsDao = $injector.get('ChecksumsDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('checksumsDao should return a resource object', function () {
        expect(checksumsDao.name).toBe('Resource');
    });

    it('send a fix request to checksums dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.CHECKSUMS + '/fix').respond(200);
        checksumsDao.fix({}, checksumsMock);
        server.flush();
    });

});