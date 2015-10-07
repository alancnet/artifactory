describe('unit test:filteredResource dao', function () {

    var filteredResourceDao;
    var RESOURCE;
    var server;
    var filteredResourceMock = {
        repoKey: 'libs-release-local',
        path: 'aopalliance/aopalliance/1.0/aopalliance-1.0.jar'
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        filteredResourceDao = $injector.get('FilteredResourceDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('filteredResourceDao should return a resource object', function () {
        expect(filteredResourceDao.name).toBe('Resource');
    });

    it('send a fix request to filteredResourceDao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.FILTERED_RESOURCE + '?setFiltered=true').respond(200);
        filteredResourceDao.setFiltered({setFiltered: true}, filteredResourceMock);
        server.flush();
    });

});