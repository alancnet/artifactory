describe('unit test:ldap groups dao', function () {

    var ldapGroupsDao;
    var RESOURCE;
    var server;
    var ldapMock = {
        "name": "il-users",
        "groupBaseDn": "ou=frogs",
        "groupNameAttribute": "cn",
        "groupMemberAttribute": "memberOf",
        "subTree": true,
        "filter": "description",
        "descriptionAttribute": "description",
        "enabledLdap": "frogs",
        "strategy": "DYNAMIC",
        "enabled": true
    }

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        ldapGroupsDao = $injector.get('LdapGroupsDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('ldapGroupsDao should return a resource object', function () {
        expect(ldapGroupsDao.name).toBe('Resource');
    });

    it('send a get (query) request with ldap groups dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS).respond(200);
        ldapGroupsDao.query();
        server.flush();
    });

    it('send a get request with ldap groups dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS + "/il-users").respond(200);
        ldapGroupsDao.get({name:'il-users'});
        server.flush();
    });

    it('send a POST request to ldap groups dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS, ldapMock).respond(200);
        ldapGroupsDao.save(ldapMock);
        server.flush();
    });

    it('send an update request ldap groups dao', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS + "/il-users").respond(200);
        ldapGroupsDao.update({name:'il-users'});
        server.flush();
    });

    it('send delete request with ldap groups dao', function () {
        server.expectDELETE(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS + "/il-users").respond(200);
        ldapGroupsDao.delete({name:'il-users'});
        server.flush();
    });

    it('send a refresh request to ldap groups dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS + "/il-users/refresh").respond(200);
        ldapGroupsDao.refresh({name:'il-users'});
        server.flush();
    });

    it('send an import request to ldap groups dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS + "/il-users/import").respond(200);
        ldapGroupsDao.import({name:'il-users'});
        server.flush();
    });

});