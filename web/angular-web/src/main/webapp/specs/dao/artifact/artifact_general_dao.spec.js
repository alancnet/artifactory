describe('unit test:artifact general dao', function () {

    var artifactGeneralDao;
    var RESOURCE;
    var generalTabDataMock = {"type": "repository", "repoKey": "libs-release-local", "path": ""}

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactGeneralDao, _RESOURCE_, $httpBackend) {
        artifactGeneralDao = ArtifactGeneralDao;
        RESOURCE = _RESOURCE_;
        server = $httpBackend;
    }));

    afterEach(function() {
        server.flush();
    });

    it('fetch should send a put request to server', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.ARTIFACT_GENERAL).respond(200);
        artifactGeneralDao.fetch(generalTabDataMock);
    });

    it('bintray should send a post request to server', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.ARTIFACT_GENERAL_BINTRAY + '?$no_spinner=true&sha1=asdf').respond(200);
        artifactGeneralDao.bintray({sha1: 'asdf'});
    });    
});