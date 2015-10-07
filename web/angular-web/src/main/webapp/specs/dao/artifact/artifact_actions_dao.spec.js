describe('unit test:artifact actions dao', function () {

    var artifactActionsDao;
    var RESOURCE;
    var server;
    var artifactData = {"repoKey": "libs-release-local", "path": "c/d.bin"};
    var artifactParams = {};

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactActionsDao, _RESOURCE_, _$httpBackend_) {
        artifactActionsDao = ArtifactActionsDao;
        RESOURCE = _RESOURCE_;
        server = _$httpBackend_;
    }));
    afterEach(function() {
        server.flush();
    });

    //// Generic perform function:
    it('perform should send the correct POST request', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.ARTIFACT_ACTIONS + '/someAction?searchKey=searchValue').respond(200);
        artifactParams.action = 'someAction';
        artifactParams.searchKey = 'searchValue';
        artifactActionsDao.perform(artifactParams, artifactData);
    });
    it('perform should send the correct POST request for dry run', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.ARTIFACT_ACTIONS + '/someAction?searchKey=searchValue').respond(200);
        artifactParams.action = 'someAction';
        artifactParams.searchKey = 'searchValue';
        artifactActionsDao.dryRun(artifactParams, artifactData);
    });
});