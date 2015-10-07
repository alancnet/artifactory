describe('Unit: password encryption dao', function () {

    var passwordEncryptionDao;
    var RESOURCE;
    var server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        passwordEncryptionDao = $injector.get('PasswordsEncryptionDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));


    it('passwordEncryptionDao should return a resource object', function () {
        expect(passwordEncryptionDao.name).toBe('Resource')
    });

    it('send an encrypt request to the server', function () {

        server.expectPOST(RESOURCE.API_URL + RESOURCE.CRYPTO + "/encrypt")
                .respond(200);

        passwordEncryptionDao.encrypt();
        server.flush();
    });

    it('send an decrypt request to the server', function () {

        server.expectPOST(RESOURCE.API_URL + RESOURCE.CRYPTO + "/decrypt")
                .respond(200);

        passwordEncryptionDao.decrypt();
        server.flush();
    });


});
