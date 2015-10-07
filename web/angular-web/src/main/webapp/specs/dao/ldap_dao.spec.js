describe('unit test:ldap dao', function () {

    var ldapDao;
    var RESOURCE;
    var server;
    var ldapMock = {
        "key": "frogs",
        "enabled": true,
        "ldapUrl": "ldap://win2012:389/dc=jfrog,dc=local",
        "search": {
            "searchFilter": "sAMAccountName={0}",
            "searchBase": "ou=il,ou=frogs|ou=us,ou=frogs",
            "searchSubTree": true,
            "managerDn": "cn=Administrator,cn=Users,dc=jfrog,dc=local",
            "managerPassword": "Win20132013"
        },
        "autoCreateUser": true,
        "emailAttribute": "mail"
    }

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        ldapDao = $injector.get('LdapDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('ldapDao should return a resource object', function () {
        expect(ldapDao.name).toBe('Resource');
    });

    it('send a query request with ldap dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.LDAP).respond(200);
        ldapDao.query();
        server.flush();
    });

    it('send a get a single ldap setting request with ldap dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.LDAP + "/frogs").respond(200);
        ldapDao.get({key:'frogs'});
        server.flush();
    });

    it('send a create request to ldap dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.LDAP, ldapMock).respond(200);
        ldapDao.save(ldapMock);
        server.flush();
    });

    it('send an update request ldap dao', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.LDAP + "/frogs").respond(200);
        ldapDao.update(ldapMock);
        server.flush();
    });

    it('send delete request with ldap dao', function () {
        server.expectDELETE(RESOURCE.API_URL + RESOURCE.LDAP + "/frogs").respond(200);
        ldapDao.delete({key:'frogs'});
        server.flush();
    });

    it('send a test request to ldap dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.LDAP + "/test/frogs").respond(200);
        ldapDao.test({key:'frogs'});
        server.flush();
    });
});