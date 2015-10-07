describe('unit test:generalConfig dao', function () {

    var generalConfigDao;
    var RESOURCE;
    var server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        generalConfigDao = $injector.get('GeneralConfigDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('send deleteLogo request with generalConfig dao', function () {
        server.expectDELETE(RESOURCE.API_URL + RESOURCE.GENERAL_CONFIG + "/logo").respond(200);
        generalConfigDao.deleteLogo();
        server.flush();
    });

});
