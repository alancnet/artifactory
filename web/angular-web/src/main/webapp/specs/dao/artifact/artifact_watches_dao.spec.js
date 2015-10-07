describe('unit test:artifact watches dao', function () {

    var artifactWatchesDao;
    var RESOURCE;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactWatchesDao, _RESOURCE_, $httpBackend) {
        artifactWatchesDao = ArtifactWatchesDao;
        RESOURCE = _RESOURCE_;
        server = $httpBackend;
    }));

    afterEach(function() {
        server.flush();
    });

    it('query should send a GET request to server', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.ARTIFACT_WATCHES+ '?path=file.bin&repoKey=libs-release-local').respond(200);
        artifactWatchesDao.query({"repoKey": "libs-release-local", "path": "file.bin"});
    });
    it('delete should send a DELETE request to server', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.ARTIFACT_WATCHES + '/remove').respond(200);
        artifactWatchesDao.delete({watches:[{"repoKey": "libs-release-local", "path": "file.bin", name: 'name'}]});
    });
    it('status should send a GET request to server', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.ARTIFACT_WATCHES + '/status?path=file.bin&repoKey=libs-release-local').respond(200);
        artifactWatchesDao.status({"repoKey": "libs-release-local", "path": "file.bin"});
    });
});