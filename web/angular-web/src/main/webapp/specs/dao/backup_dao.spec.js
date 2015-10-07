describe('unit test:backup dao', function () {

    var backupDao;
    var RESOURCE;
    var server;
    var groupMock = {
        "key": "hhhh",
        "enabled": true,
        "cronExp": "hhhh"
    };
    var queryParams = "?hhhh&&cronExp=hhhh&enabled=true";


    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        backupDao = $injector.get('BackupDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('groupDao should return a resource object', function () {
        expect(backupDao.name).toBe('Resource');
    });

    it('send an update request group dao', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.BACKUP + '/hhhh' , groupMock).respond(200);
        backupDao.update(groupMock);
        server.flush();
    });

    it('send an save request  with group dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.BACKUP, groupMock).respond(200);
        backupDao.save(groupMock);
        server.flush();
    });

    it('send a delete request group dao', function () {
        server.expectDELETE(RESOURCE.API_URL + RESOURCE.BACKUP + '/hhhh').respond(200);
        backupDao.delete({key: 'hhhh'});
        server.flush();
    });

});