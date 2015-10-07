describe('unit test:SecurityDescriptorDao', function () {

    var securityDescriptorDao;
    var RESOURCE;
    var server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        securityDescriptorDao = $injector.get('SecurityDescriptorDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('configDescriptorDao should return a resource object', function () {
        expect(securityDescriptorDao.name).toBe('Resource');
    });

    it('SecurityDescriptorDao send an PUT request', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.SECURITY_DESCRIPTOR).respond(200);
        securityDescriptorDao.update();
        server.flush();
    });
});