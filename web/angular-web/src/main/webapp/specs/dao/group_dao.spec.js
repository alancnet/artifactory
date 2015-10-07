describe('unit test:groups dao', function () {

    var groupsDao;
    var RESOURCE;
    var server;
    var groupMock = {
        "name": "idan",
        "description": "idan group bla bla",
        "autoJoin": false,
        "realm": "artifactory",
        "groupName": "idan",
        "newUserDefault": false
    };
    var queryParams = "?autoJoin=false&description=idan+group+bla+bla&groupName=idan&newUserDefault=false&realm=artifactory";

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        groupsDao = $injector.get('GroupsDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('groupDao should return a resource object', function () {
        expect(groupsDao.name).toBe('Resource');
    });

    it('send an update request group dao', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.GROUPS + "/idan", groupMock).respond(200);
        groupsDao.update(groupMock);
        server.flush();
    });

    it('send delete request with group dao', function () {

        server.expectPOST(RESOURCE.API_URL + RESOURCE.GROUPS + "/groupsDelete", groupMock).respond(200);
        groupsDao.delete(groupMock);
        server.flush();
    });

    it('send an save request  with group dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.GROUPS, groupMock).respond(200);
        groupsDao.save(groupMock);
        server.flush();
    });

});