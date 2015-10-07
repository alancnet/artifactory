describe('Unit: mail dao', function () {

    var mailDao;
    var RESOURCE;
    var server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        mailDao = $injector.get('MailDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('mailDao should return a resource object', function () {
        expect(mailDao.name).toBe('Resource');
    });

    it('send an update request to serve', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.MAIL).respond(200);
        mailDao.update();
        server.flush();
    })

});