describe('unit test:maintenance dao', function () {

    var maintenanceDao;
    var RESOURCE;
    var server;

    var settingsParams = {
        cleanUnusedCachedCron: '',
        cleanVirtualRepoCron: '*/5 * * * *',
        garbageCollectorCron: '* */5 * * *',
        quotaControl: true,
        storageLimit: '95',
        storageWarning: '85'
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        maintenanceDao = $injector.get('MaintenanceDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('maintenanceDao should return a resource object', function () {
        expect(maintenanceDao.name).toBe('Resource');
    });

    it('should get settings', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.MAINTENANCE).respond(200);
        maintenanceDao.get();
        server.flush();
    });

    it('should save settings', function() {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.MAINTENANCE).respond(200);
        maintenanceDao.update(settingsParams);
        server.flush();
    });

    it('should run garbage collection', function() {
        var moduleName = 'garbageCollection';
        server.expectPOST(RESOURCE.API_URL + RESOURCE.MAINTENANCE + '/' + moduleName).respond(200);
        maintenanceDao.perform({module: moduleName});
        server.flush();
    });

    it('should clean unused  cache', function() {
        var moduleName = 'cleanUnusedCache';
        server.expectPOST(RESOURCE.API_URL + RESOURCE.MAINTENANCE + '/' + moduleName).respond(200);
        maintenanceDao.perform({module: moduleName});
        server.flush();
    });

    it('should clean Virtual Repositories', function() {
        var moduleName = 'cleanVirtualRepo';
        server.expectPOST(RESOURCE.API_URL + RESOURCE.MAINTENANCE + '/' + moduleName).respond(200);
        maintenanceDao.perform({module: moduleName});
        server.flush();
    });

    it('should prune Unreferenced Data', function() {
        var moduleName = 'prune';
        server.expectPOST(RESOURCE.API_URL + RESOURCE.MAINTENANCE + '/' + moduleName).respond(200);
        maintenanceDao.perform({module: moduleName});
        server.flush();
    });

    it('should compress internal data', function() {
        var moduleName = 'compress';
        server.expectPOST(RESOURCE.API_URL + RESOURCE.MAINTENANCE + '/' + moduleName).respond(200);
        maintenanceDao.perform({module: moduleName});
        server.flush();
    });

});