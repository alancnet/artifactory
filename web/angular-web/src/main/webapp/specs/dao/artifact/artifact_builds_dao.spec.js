describe('unit test:artifact builds dao', function () {

    var artifactBuildsDao;
    var RESOURCE;
    var server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactBuildsDao, _RESOURCE_, _$httpBackend_) {
        artifactBuildsDao = ArtifactBuildsDao.getInstance();
        RESOURCE = _RESOURCE_;
        server = _$httpBackend_;
    }));

    it('artifactBuildsDao should return a resource object', function () {
        expect(artifactBuildsDao.name).toBe('Resource');
    });

    it('query should send the correct GET request', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.ARTIFACT_BUILDS + '?repoKey=libs-release-local&path=c/d.bin').respond({});
        artifactBuildsDao.query({"repoKey": "libs-release-local", "path": "c/d.bin"});
    });

    it('getJSON should send the correct GET request', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.ARTIFACT_BUILDS + '/json/123').respond({});
        artifactBuildsDao.getJson({buildId: '123'});
    });
});