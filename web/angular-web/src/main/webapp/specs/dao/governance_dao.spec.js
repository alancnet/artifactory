describe('unit test:governance dao', function () {

    var governance;
    var RESOURCE;
    var server;

    var governanceParams = {
        "repoKey": 'libs-releases-local',
        "path": 'com/fasterxml/jackson/core/jackson-databind/2.4.5/jackson-databind-2.4.5.jar'
    };

    var componentIdChangeParams = {
        "repoKey":"libs-releases-local",
        "path":"org/glassfish/jersey/media/jersey-media-multipart2/2.0-m11/jersey-media-multipart2-2.0-m11.jar",
        "componentId":"123456",
        "origComponentId":""
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        governanceDao = $injector.get('GovernanceDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('governance should return a resource object', function () {
        expect(governanceDao.name).toBe('Resource');
    });

    it('should fetch data', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.ARTIFACT_GOVERNANCE).respond(200);
        governanceDao.fetch(governanceParams);
        server.flush();
    });

    it('should update component id', function() {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.ARTIFACT_GOVERNANCE).respond(200);
        governanceDao.update(componentIdChangeParams);
        server.flush();
    });
});