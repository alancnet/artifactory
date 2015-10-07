describe('unit test:artifact property dao', function () {

    var artifactPropertyDao;
    var RESOURCE;
    var propertyNameParams = {name: 'name'};
    var propertyArtifactParams = {"repoKey": "libs-release-local", "path": "file.bin"};
    var propertyData = {key: 'value'};

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactPropertyDao, _RESOURCE_, $httpBackend) {
        artifactPropertyDao = ArtifactPropertyDao.getInstance();
        RESOURCE = _RESOURCE_;
        server = $httpBackend;
    }));

    afterEach(function() {
        server.flush();
    });

    it('get should send a GET request to server', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.ARTIFACT_PROPERTIES + '/name').respond(200);
        artifactPropertyDao.get(propertyNameParams);
    });
    it('query should send a POST request to server', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.ARTIFACT_PROPERTIES).respond(200);
        artifactPropertyDao.query();
    });
    it('update should send a PUT request to server', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.ARTIFACT_PROPERTIES + '?path=file.bin&repoKey=libs-release-local').respond(200);
        artifactPropertyDao.update(propertyArtifactParams, propertyData);
    });
    it('delete should send a DELETE request to server', function () {
        server.expectDELETE(RESOURCE.API_URL + RESOURCE.ARTIFACT_PROPERTIES + '/name').respond(200);
        artifactPropertyDao.delete(propertyNameParams);
    });
});