describe('unit test:indexer dao', function () {

    var indexerDao;
    var RESOURCE;
    var server;
    var indexerMock = {
        "enabled": true,
        "cronExp":"0 23 5 * * ?",
        "excludedRepos":["libs-release-local"]
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        indexerDao = $injector.get('IndexerDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('licensesDao should return a resource object', function () {
        expect(indexerDao.name).toBe('Resource');
    });

    it('send an update request with indexer dao', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.INDEXER).respond(200);
        indexerDao.update(indexerMock);
        server.flush();
    });

    it('should send a post request to run the indexer ', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.INDEXER).respond(200);
        indexerDao.run();
        server.flush();
    });

    it('should send a get request to return an indexer', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.INDEXER).respond(200);
        indexerDao.get();
        server.flush();
    })
});