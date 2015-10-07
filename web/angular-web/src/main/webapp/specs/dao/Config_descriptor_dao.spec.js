describe('unit test:configDescriptorDao', function () {

    var configDescriptorDao;
    var RESOURCE;
    var server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        configDescriptorDao = $injector.get('ConfigDescriptorDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('configDescriptorDao should return a resource object', function () {
        expect(configDescriptorDao.name).toBe('Resource');
    });

    it('configDescriptorDao send an PUT request', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.CONFIG_DESCRIPTOR).respond(200);
        configDescriptorDao.update();
        server.flush();
    });
});