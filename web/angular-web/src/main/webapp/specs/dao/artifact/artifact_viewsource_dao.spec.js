describe('unit test:tree browser tab dao', function () {

    var viewSourceDao;
    var RESOURCE;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactViewSourceDao, _RESOURCE_, $httpBackend) {
        viewSourceDao = ArtifactViewSourceDao.getInstance();
        RESOURCE = _RESOURCE_;
        server = $httpBackend;
    }));
    afterEach(function() {
        server.flush();
    });

    it('fetch should send a POST request to server', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.ARTIFACT_VIEW_SOURCE).respond(200);
        viewSourceDao.fetch({"type": "file", "repoKey": "libs-release-local", "path": ""});
    })
});