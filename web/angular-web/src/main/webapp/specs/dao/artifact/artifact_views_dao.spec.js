describe('unit test:pom view tab dao', function () {

    var artifactViewsDao;
    var RESOURCE;
    var pomViewTabDataMock = {
        "view":"pom",
        "path": "DecodedBase64/DecodedBase64/DecodedBase64/DecodedBase64-DecodedBase64.pom",
        "repoKey": "ext-releases-local"
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactViewsDao, _RESOURCE_, $httpBackend) {
        artifactViewsDao = ArtifactViewsDao;
        RESOURCE = _RESOURCE_;
        server = $httpBackend;
    }));

    afterEach(function () {
        server.flush();
    });

    it('fetch should send a put request to serve', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.VIEWS+"/pom").respond(200);
        artifactViewsDao.fetch(pomViewTabDataMock);
    });
});