describe('unit test:builds dao', function () {

    var buildsDao;
    var RESOURCE;
    var server;
    var buildsMock = {
        pagingData: [
            {name: 'asdf', lastBuildTime: '12.12.14'},
            {name: 'alf', lastBuildTime: '12.11.13'},
            {name: 'gradle', lastBuildTime: '12.10.12'},
        ],
        totalItems: 30
    }

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        buildsDao = $injector.get('BuildsDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('buildsDao should return a resource object', function () {
        expect(buildsDao.name).toBe('Resource');
    });

    it('send a get request with builds dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.BUILDS).respond(200);
        buildsDao.get();
        server.flush();
    });
    it('send a getHistory request with builds dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.BUILDS + '/buildname').respond(200);
        buildsDao.getData({name: 'buildname'});
        server.flush();
    });

});